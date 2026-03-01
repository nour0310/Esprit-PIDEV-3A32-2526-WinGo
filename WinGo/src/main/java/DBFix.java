import java.sql.*;

public class DBFix {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/wingo";
        String user = "root";
        String pwd = "";

        try (Connection cnx = DriverManager.getConnection(url, user, pwd)) {
            System.out.println("Checking PROFIL table structure...");
            
            // 1. Inspect
            DatabaseMetaData md = cnx.getMetaData();
            ResultSet rs = md.getColumns(null, null, "profil", "id");
            if (rs.next()) {
                String isAuto = rs.getString("IS_AUTOINCREMENT");
                System.out.println("ID is_autoincrement: " + isAuto);
            } else {
                System.out.println("Column ID not found in PROFIL table.");
            }

            // 2. Force Fix
            try (Statement st = cnx.createStatement()) {
                System.out.println("Attempting: ALTER TABLE profil MODIFY COLUMN id INT AUTO_INCREMENT PRIMARY KEY");
                st.executeUpdate("ALTER TABLE profil MODIFY COLUMN id INT AUTO_INCREMENT PRIMARY KEY");
                System.out.println("Success!");
            } catch (SQLException e) {
                System.err.println("Failed primary key variant, trying simple modify...");
                try (Statement st = cnx.createStatement()) {
                    st.executeUpdate("ALTER TABLE profil MODIFY COLUMN id INT AUTO_INCREMENT");
                    System.out.println("Success (simple modify)!");
                } catch (SQLException e2) {
                    System.err.println("Both attempts failed: " + e2.getMessage());
                    
                    // Last resort: If it's empty or the user is okay with it, we could drop but let's not.
                    // Let's try to find if it has a primary key at all.
                    ResultSet pkRs = md.getPrimaryKeys(null, null, "profil");
                    if (!pkRs.next()) {
                        System.out.println("No primary key found on PROFIL. Adding one...");
                        try (Statement st = cnx.createStatement()) {
                            st.executeUpdate("ALTER TABLE profil ADD PRIMARY KEY (id)");
                            st.executeUpdate("ALTER TABLE profil MODIFY id INT AUTO_INCREMENT");
                            System.out.println("Primary key added and set to auto_increment.");
                        } catch (SQLException e3) {
                             System.err.println("Final attempt failed: " + e3.getMessage());
                        }
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }
}
