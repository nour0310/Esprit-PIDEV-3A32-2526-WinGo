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
                "event_type, season, capacity, available_places, status, image_event) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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

            int rowsInserted = ps.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("✅ Event added successfully!");
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

                list.add(e);
            }
            System.out.println("📊 Found " + list.size() + " events");
        } catch (SQLException e) {
            System.err.println("❌ Error fetching events: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    // READ ONE
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
                return e;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error fetching event: " + e.getMessage());
        }
        return null;
    }

    // UPDATE
    public void modifier(Event event) {
        String req = "UPDATE event SET title=?, description=?, date_event=?, start_time=?, " +
                "location=?, event_type=?, season=?, capacity=?, available_places=?, " +
                "status=?, image_event=? WHERE id_event=?";
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
            ps.setInt(12, event.getId_event());

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Event updated successfully!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error updating event: " + e.getMessage());
            e.printStackTrace();
        }
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