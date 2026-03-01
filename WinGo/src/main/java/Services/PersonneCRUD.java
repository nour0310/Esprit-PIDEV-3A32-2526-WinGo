package Services;
import Entites.Personne;
import Utils.MyBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonneCRUD implements IntrefaceCRUD<Personne>{

    Connection conn;

    public PersonneCRUD() {
        conn = MyBD.getInstance().getCnx();
    }
    @Override
    public void ajouter(Personne personne) throws SQLException {
        String req=
                "insert into personne (nom, prenom,age) " +
                        "values('" + personne.getNom() + "','"
                        + personne.getPrenom() + "'"
                        +  "," + personne.getAge() + ")";

        Statement st=conn.createStatement();
        st.executeUpdate(req);
       System.out.println("Personne ajoutÃ©e !");
    }

    @Override
    public void modifier(Personne personne) throws SQLException {
        String req="update personne set nom=?,prenom=?,age=? where id=?";

    PreparedStatement pst=conn.prepareStatement(req);
    pst.setString(1, personne.getNom());
    pst.setString(2, personne.getPrenom());
    pst.setInt(3, personne.getAge());
    pst.setInt(4, personne.getId());
    pst.executeUpdate();
        System.out.println("Personne modifiÃ©e");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req="delete from personne where id=?";
        PreparedStatement pst=conn.prepareStatement(req);
        pst.setInt(1, id);
        pst.executeUpdate();
        System.out.println("Personne supprimer");
    }

    @Override
    public List<Personne> afficher() throws SQLException {
        String req ="select * from personne";
        Statement st=conn.createStatement();
        ResultSet rs=st.executeQuery(req);
        List<Personne> listepersonnes=new ArrayList<Personne>();

        while(rs.next()){
            Personne p = new Personne();
            p.setId(rs.getInt("id"));
            p.setNom(rs.getString("nom"));
            p.setPrenom(rs.getString("prenom"));
            p.setAge(rs.getInt("age"));

            listepersonnes.add(p);
        }
        return listepersonnes;
    }
}
