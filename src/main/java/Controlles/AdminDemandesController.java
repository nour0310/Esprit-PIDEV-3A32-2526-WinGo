package Controlles;

import Utils.MyBD;
import Utils.Session;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panneau admin pour approuver / refuser les demandes "Devenir Commerçant".
 *
 * Utilise UNIQUEMENT la colonne type de la table utilisateur :
 *   EN_ATTENTE → attente de décision admin
 *   COMMERCANT → approuvé
 *   CLIENT     → refusé (on remet CLIENT)
 *
 * Utilisation :
 *   AdminDemandesController admin = new AdminDemandesController();
 *   admin.setOverlayContainer(overlayContainer);
 *   admin.showAdminPanel();
 */
public class AdminDemandesController {

    private StackPane overlayContainer;

    public void setOverlayContainer(StackPane container) {
        this.overlayContainer = container;
    }

    // ── Point d'entrée ────────────────────────────────────────
    public void showAdminPanel() {
        if (overlayContainer == null) return;

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.7);");
        overlay.setAlignment(Pos.CENTER);
        overlay.setOnMouseClicked(e -> { if (e.getTarget() == overlay) overlayContainer.getChildren().remove(overlay); });

        VBox panel = buildPanel(overlay);
        panel.setOnMouseClicked(javafx.event.Event::consume);
        overlay.getChildren().add(panel);
        overlayContainer.getChildren().add(overlay);
    }

    // ── Construction du panneau ───────────────────────────────
    private VBox buildPanel(StackPane overlay) {
        VBox panel = new VBox(0);
        panel.setPrefWidth(700); panel.setMaxWidth(700); panel.setMaxHeight(580);
        panel.setStyle(
                "-fx-background-color: #0F172A;" +
                        "-fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(239,68,68,0.35), 50, 0, 0, 0);"
        );

        // HEADER
        HBox header = new HBox(); header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-padding: 18 24; -fx-background-color: linear-gradient(to right, #DC2626, #EF4444); -fx-background-radius: 20 20 0 0;");
        Label title = new Label("🛡  Administration — Demandes Commerçants");
        title.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 15px;");
        Region hsp = new Region(); HBox.setHgrow(hsp, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: rgba(255,255,255,0.18); -fx-text-fill: white; -fx-background-radius: 999; -fx-font-weight: 900; -fx-min-width: 30; -fx-min-height: 30; -fx-cursor: hand; -fx-padding: 0;");
        closeBtn.setOnAction(e -> overlayContainer.getChildren().remove(overlay));
        header.getChildren().addAll(title, hsp, closeBtn);

        // CONTENT
        VBox content = new VBox(12); content.setStyle("-fx-padding: 20;");

        List<int[]> demandes = new ArrayList<>(); // [id, index]
        List<String[]> infos = new ArrayList<>();  // [email, nom, prenom]

        try (Statement st = MyBD.getInstance().getConn().createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id, email, nom, prenom FROM utilisateur WHERE type='EN_ATTENTE' ORDER BY id ASC")) {
            while (rs.next()) {
                demandes.add(new int[]{rs.getInt("id")});
                infos.add(new String[]{rs.getString("email"), rs.getString("nom"), rs.getString("prenom")});
            }
        } catch (SQLException e) {
            Label err = new Label("❌ Erreur : " + e.getMessage());
            err.setStyle("-fx-text-fill: #F87171; -fx-font-size: 12px;");
            content.getChildren().add(err);
            panel.getChildren().addAll(header, content);
            return panel;
        }

        if (demandes.isEmpty()) {
            Label empty = new Label("✅  Aucune demande en attente.");
            empty.setStyle("-fx-text-fill: #64748B; -fx-font-size: 14px; -fx-padding: 30 0;");
            VBox eb = new VBox(empty); eb.setAlignment(Pos.CENTER);
            content.getChildren().add(eb);
        } else {
            Label countLbl = new Label(demandes.size() + " demande(s) en attente");
            countLbl.setStyle("-fx-text-fill: #FFBD00; -fx-font-weight: 900; -fx-font-size: 12px; -fx-background-color: rgba(255,189,0,0.12); -fx-background-radius: 6; -fx-padding: 6 12;");

            VBox listBox = new VBox(8); listBox.setStyle("-fx-padding: 0 0 10 0;");
            for (int i = 0; i < demandes.size(); i++) {
                int idUser = demandes.get(i)[0];
                String email = infos.get(i)[0];
                String nom   = infos.get(i)[1];
                String prenom= infos.get(i)[2];
                listBox.getChildren().add(buildCard(idUser, email, nom, prenom, listBox));
            }

            ScrollPane scroll = new ScrollPane(listBox);
            scroll.setFitToWidth(true); scroll.setMaxHeight(450);
            scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
            content.getChildren().addAll(countLbl, scroll);
        }

        panel.getChildren().addAll(header, content);
        return panel;
    }

    // ── Carte d'un utilisateur EN_ATTENTE ─────────────────────
    private VBox buildCard(int idUser, String email, String nom, String prenom, VBox listBox) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #1E293B; -fx-background-radius: 14; -fx-padding: 16 18; -fx-border-color: rgba(255,255,255,0.07); -fx-border-radius: 14; -fx-border-width: 1;");

        Label nomLbl = new Label("👤  " + prenom + " " + nom);
        nomLbl.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 14px;");

        Label emailLbl = new Label("📧  " + email);
        emailLbl.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px; -fx-font-weight: 700;");

        Label feedbackLbl = new Label("");
        feedbackLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: 800;"); feedbackLbl.setWrapText(true);

        Button approveBtn = new Button("✅  Approuver");
        approveBtn.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; -fx-font-weight: 900; -fx-background-radius: 8; -fx-padding: 9 20; -fx-cursor: hand;");

        Button refuseBtn = new Button("❌  Refuser");
        refuseBtn.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-font-weight: 900; -fx-background-radius: 8; -fx-padding: 9 20; -fx-cursor: hand;");

        // APPROUVER → type = COMMERCANT
        approveBtn.setOnAction(e -> {
            try (PreparedStatement ps = MyBD.getInstance().getConn().prepareStatement(
                    "UPDATE utilisateur SET type='COMMERCANT' WHERE id=?")) {
                ps.setInt(1, idUser);
                ps.executeUpdate();
                listBox.getChildren().remove(card);
                // Feedback temporaire
                Label ok = new Label("✅  " + prenom + " " + nom + " est maintenant Commerçant.");
                ok.setStyle("-fx-text-fill: #4ADE80; -fx-font-weight: 800; -fx-font-size: 12px; -fx-padding: 6 12; -fx-background-color: rgba(74,222,128,0.1); -fx-background-radius: 8;");
                listBox.getChildren().add(0, ok);
            } catch (Exception ex) {
                feedbackLbl.setText("❌ " + ex.getMessage());
                feedbackLbl.setStyle("-fx-text-fill: #F87171;");
                if (!card.getChildren().contains(feedbackLbl)) card.getChildren().add(feedbackLbl);
            }
        });

        // REFUSER → type = CLIENT (on remet CLIENT)
        refuseBtn.setOnAction(e -> {
            approveBtn.setDisable(true); refuseBtn.setDisable(true);

            Button confirmBtn = new Button("Confirmer le refus");
            confirmBtn.setStyle("-fx-background-color: #991B1B; -fx-text-fill: white; -fx-font-weight: 900; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
            Button cancelBtn = new Button("Annuler");
            cancelBtn.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: #94A3B8; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");

            confirmBtn.setOnAction(ev -> {
                try (PreparedStatement ps = MyBD.getInstance().getConn().prepareStatement(
                        "UPDATE utilisateur SET type='CLIENT' WHERE id=?")) {
                    ps.setInt(1, idUser);
                    ps.executeUpdate();
                    listBox.getChildren().remove(card);
                    Label ko = new Label("🚫  Demande de " + prenom + " " + nom + " refusée.");
                    ko.setStyle("-fx-text-fill: #F87171; -fx-font-weight: 800; -fx-font-size: 12px; -fx-padding: 6 12; -fx-background-color: rgba(248,113,113,0.1); -fx-background-radius: 8;");
                    listBox.getChildren().add(0, ko);
                } catch (Exception ex) {
                    feedbackLbl.setText("❌ " + ex.getMessage());
                    if (!card.getChildren().contains(feedbackLbl)) card.getChildren().add(feedbackLbl);
                }
            });

            cancelBtn.setOnAction(ev -> {
                card.getChildren().removeAll(confirmBtn, cancelBtn);
                approveBtn.setDisable(false); refuseBtn.setDisable(false);
            });

            card.getChildren().addAll(new HBox(8, confirmBtn, cancelBtn));
        });

        HBox actions = new HBox(12, approveBtn, refuseBtn); actions.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().addAll(nomLbl, emailLbl, actions);
        return card;
    }
}