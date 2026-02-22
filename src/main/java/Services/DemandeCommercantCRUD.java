package Services;

import Utils.MyBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la table demande_commercant.
 * Cycle : soumission (EN_ATTENTE) → APPROUVE ou REFUSE par un admin.
 */
public class DemandeCommercantCRUD {

    private Connection getConn() {
        return MyBD.getInstance().getConn();
    }

    // ─────────────────────────────────────────────────────────────
    //  CLIENT : soumettre une demande
    // ─────────────────────────────────────────────────────────────

    /**
     * Insère une nouvelle demande.
     * Lance une SQLException si une demande EN_ATTENTE ou APPROUVEE existe déjà.
     */
    public void soumettre(int idUtilisateur, String nom, String tel,
                          String type, String motivation) throws SQLException {

        // Vérifier s'il existe déjà une demande active pour cet utilisateur
        String checkSql = "SELECT statut FROM demande_commercant " +
                "WHERE id_utilisateur = ? ORDER BY date_demande DESC LIMIT 1";
        try (PreparedStatement ps = getConn().prepareStatement(checkSql)) {
            ps.setInt(1, idUtilisateur);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String statut = rs.getString("statut");
                if ("EN_ATTENTE".equals(statut))
                    throw new SQLException("Une demande est déjà en attente de validation.");
                if ("APPROUVE".equals(statut))
                    throw new SQLException("Votre demande a déjà été approuvée.");
            }
        }

        String sql = "INSERT INTO demande_commercant " +
                "(id_utilisateur, nom_complet, telephone, type_produits, motivation) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            ps.setString(2, nom);
            ps.setString(3, tel);
            ps.setString(4, type);
            ps.setString(5, motivation);
            ps.executeUpdate();
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  CLIENT : consulter son statut
    // ─────────────────────────────────────────────────────────────

    /**
     * @return "EN_ATTENTE" | "APPROUVE" | "REFUSE" | null (aucune demande)
     */
    public String getStatutDemande(int idUtilisateur) throws SQLException {
        String sql = "SELECT statut FROM demande_commercant " +
                "WHERE id_utilisateur = ? ORDER BY date_demande DESC LIMIT 1";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, idUtilisateur);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("statut") : null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  ADMIN : lister les demandes EN_ATTENTE
    // ─────────────────────────────────────────────────────────────

    public List<DemandePojo> getDemandesEnAttente() throws SQLException {
        String sql =
                "SELECT dc.id, dc.id_utilisateur, dc.nom_complet, dc.telephone, " +
                        "       dc.type_produits, dc.motivation, dc.date_demande, u.email " +
                        "FROM demande_commercant dc " +
                        "JOIN utilisateur u ON u.id = dc.id_utilisateur " +
                        "WHERE dc.statut = 'EN_ATTENTE' " +
                        "ORDER BY dc.date_demande ASC";

        List<DemandePojo> list = new ArrayList<>();
        try (Statement st = getConn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new DemandePojo(
                        rs.getInt("id"),
                        rs.getInt("id_utilisateur"),
                        rs.getString("email"),
                        rs.getString("nom_complet"),
                        rs.getString("telephone"),
                        rs.getString("type_produits"),
                        rs.getString("motivation"),
                        rs.getString("date_demande")
                ));
            }
        }
        return list;
    }

    // ─────────────────────────────────────────────────────────────
    //  ADMIN : approuver → change statut + promeut l'utilisateur
    // ─────────────────────────────────────────────────────────────

    public void approuver(int idDemande, int idUtilisateur) throws SQLException {
        Connection conn = getConn();
        boolean autoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            // 1. Mettre à jour la demande
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE demande_commercant SET statut='APPROUVE', date_traitement=NOW() WHERE id=?")) {
                ps.setInt(1, idDemande);
                ps.executeUpdate();
            }
            // 2. Promouvoir l'utilisateur
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE utilisateur SET type='COMMERCANT' WHERE id=?")) {
                ps.setInt(1, idUtilisateur);
                ps.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  ADMIN : refuser
    // ─────────────────────────────────────────────────────────────

    public void refuser(int idDemande, String commentaire) throws SQLException {
        String sql = "UPDATE demande_commercant " +
                "SET statut='REFUSE', date_traitement=NOW(), commentaire_admin=? " +
                "WHERE id=?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, commentaire == null ? "" : commentaire);
            ps.setInt(2, idDemande);
            ps.executeUpdate();
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  POJO
    // ─────────────────────────────────────────────────────────────

    public static class DemandePojo {
        public final int    id;
        public final int    idUtilisateur;
        public final String email;
        public final String nom;
        public final String tel;
        public final String type;
        public final String motivation;
        public final String date;

        public DemandePojo(int id, int idUtilisateur, String email,
                           String nom, String tel, String type,
                           String motivation, String date) {
            this.id            = id;
            this.idUtilisateur = idUtilisateur;
            this.email         = email;
            this.nom           = nom;
            this.tel           = tel;
            this.type          = type;
            this.motivation    = motivation;
            this.date          = date;
        }
    }
}