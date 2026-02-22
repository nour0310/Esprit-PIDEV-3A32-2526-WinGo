package Services;

import Entites.Event;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventCRUD {
    private Connection cnx;

    public EventCRUD() {
        cnx = MyBD.getInstance().getConn();
    }

    // CREATE
    public void ajouter(Event event) {
        String req = "INSERT INTO event (title, description, date_event, start_time, location, " +
                "event_type, season, capacity, available_places, status, image_event, price) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDescription());
            ps.setDate(3, event.getDate_event());
            ps.setTime(4, event.getStart_time());
            ps.setString(5, event.getLocation());
            ps.setString(6, event.getEvent_type());
            ps.setString(7, event.getSeason());
            ps.setInt(8, event.getCapacity());
            ps.setInt(9, event.getAvailable_places());
            ps.setString(10, event.getStatus());
            ps.setString(11, event.getImage_event());
            ps.setDouble(12, event.getPrice());

            int rowsInserted = ps.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("✅ Event added successfully! Price: $" + event.getPrice());
            }
        } catch (SQLException e) {
            System.err.println("❌ Error adding event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // READ ALL
    public List<Event> afficher() {
        List<Event> list = new ArrayList<>();
        String req = "SELECT * FROM event";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                Event e = new Event();
                e.setId_event(rs.getInt("id_event"));
                e.setTitle(rs.getString("title"));
                e.setDescription(rs.getString("description"));
                e.setDate_event(rs.getDate("date_event"));
                e.setStart_time(rs.getTime("start_time"));
                e.setLocation(rs.getString("location"));
                e.setEvent_type(rs.getString("event_type"));
                e.setSeason(rs.getString("season"));
                e.setCapacity(rs.getInt("capacity"));
                e.setAvailable_places(rs.getInt("available_places"));
                e.setStatus(rs.getString("status"));
                e.setImage_event(rs.getString("image_event"));

                // Get price - IMPORTANT: This must match your database column name
                try {
                    double price = rs.getDouble("price");
                    e.setPrice(price);
                    System.out.println("📊 Event: " + e.getTitle() + ", Price from DB: $" + price);
                } catch (SQLException ex) {
                    System.out.println("⚠️ Price column not found in database: " + ex.getMessage());
                    e.setPrice(0.0);
                }

                list.add(e);
            }
            System.out.println("📋 Found " + list.size() + " events");
        } catch (SQLException e) {
            System.err.println("❌ Error fetching events: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    // UPDATE
    public void modifier(Event event) {
        String req = "UPDATE event SET title=?, description=?, date_event=?, start_time=?, " +
                "location=?, event_type=?, season=?, capacity=?, available_places=?, " +
                "status=?, image_event=?, price=? WHERE id_event=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDescription());
            ps.setDate(3, event.getDate_event());
            ps.setTime(4, event.getStart_time());
            ps.setString(5, event.getLocation());
            ps.setString(6, event.getEvent_type());
            ps.setString(7, event.getSeason());
            ps.setInt(8, event.getCapacity());
            ps.setInt(9, event.getAvailable_places());
            ps.setString(10, event.getStatus());
            ps.setString(11, event.getImage_event());
            ps.setDouble(12, event.getPrice());
            ps.setInt(13, event.getId_event());

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Event updated successfully! New price: $" + event.getPrice());
            }
        } catch (SQLException e) {
            System.err.println("❌ Error updating event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // GET BY ID
    public Event getById(int id) {
        String req = "SELECT * FROM event WHERE id_event = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Event e = new Event();
                e.setId_event(rs.getInt("id_event"));
                e.setTitle(rs.getString("title"));
                e.setDescription(rs.getString("description"));
                e.setDate_event(rs.getDate("date_event"));
                e.setStart_time(rs.getTime("start_time"));
                e.setLocation(rs.getString("location"));
                e.setEvent_type(rs.getString("event_type"));
                e.setSeason(rs.getString("season"));
                e.setCapacity(rs.getInt("capacity"));
                e.setAvailable_places(rs.getInt("available_places"));
                e.setStatus(rs.getString("status"));
                e.setImage_event(rs.getString("image_event"));
                e.setPrice(rs.getDouble("price"));
                return e;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching event: " + e.getMessage());
        }
        return null;
    }

    // SEARCH
    public List<Event> rechercherEvents(String term) {
        List<Event> list = new ArrayList<>();
        String req = "SELECT * FROM event WHERE title LIKE ? OR location LIKE ? " +
                "OR event_type LIKE ? OR season LIKE ? OR status LIKE ? OR description LIKE ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            String searchTerm = "%" + term + "%";
            ps.setString(1, searchTerm);
            ps.setString(2, searchTerm);
            ps.setString(3, searchTerm);
            ps.setString(4, searchTerm);
            ps.setString(5, searchTerm);
            ps.setString(6, searchTerm);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Event e = new Event();
                e.setId_event(rs.getInt("id_event"));
                e.setTitle(rs.getString("title"));
                e.setDescription(rs.getString("description"));
                e.setDate_event(rs.getDate("date_event"));
                e.setStart_time(rs.getTime("start_time"));
                e.setLocation(rs.getString("location"));
                e.setEvent_type(rs.getString("event_type"));
                e.setSeason(rs.getString("season"));
                e.setCapacity(rs.getInt("capacity"));
                e.setAvailable_places(rs.getInt("available_places"));
                e.setStatus(rs.getString("status"));
                e.setImage_event(rs.getString("image_event"));
                e.setPrice(rs.getDouble("price"));
                list.add(e);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error searching events: " + e.getMessage());
        }
        return list;
    }

    // DELETE
    public void supprimer(int id) {
        String req = "DELETE FROM event WHERE id_event=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);

            int rowsDeleted = ps.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("✅ Event deleted successfully!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error deleting event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}