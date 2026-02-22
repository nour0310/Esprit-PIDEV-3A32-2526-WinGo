package Controlles;

import Services.DemandeCommercantCRUD;
import Services.DemandeCommercantCRUD.DemandePojo;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Contrôleur du panneau admin pour gérer les demandes "Devenir Commerçant".
 *
 * Utilisation depuis AccueilFX ou un contrôleur admin :
 *
 *   AdminDemandesController admin = new AdminDemandesController();
 *   admin.setOverlayContainer(overlayContainer);
 *   admin.showAdminPanel();
 */
public class AdminDemandesController {

    private final DemandeCommercantCRUD demandeCRUD = new DemandeCommercantCRUD();
    private StackPane overlayContainer;

    public void setOverlayContainer(StackPane container) {
        this.overlayContainer = container;
    }

    // ── Point d'entrée public ─────────────────────────────────
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
        panel.setPrefWidth(720); panel.setMaxWidth(720); panel.setMaxHeight(600);
        panel.setStyle(
                "-fx-background-color: #0F172A;" +
                        "-fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(239,68,68,0.35), 50, 0, 0, 0);"
        );

        // ── HEADER ────────────────────────────────────────────
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-padding: 18 24; -fx-background-color: linear-gradient(to right, #DC2626, #EF4444); -fx-background-radius: 20 20 0 0;");
        Label title = new Label("🛡  Administration — Demandes Commerçants");
        title.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 15px;");
        Region hsp = new Region(); HBox.setHgrow(hsp, Priority.ALWAYS);
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: rgba(255,255,255,0.18); -fx-text-fill: white; -fx-background-radius: 999; -fx-font-weight: 900; -fx-min-width: 30; -fx-min-height: 30; -fx-cursor: hand; -fx-padding: 0;");
        closeBtn.setOnAction(e -> overlayContainer.getChildren().remove(overlay));
        header.getChildren().addAll(title, hsp, closeBtn);

        // ── CONTENT ───────────────────────────────────────────
        VBox content = new VBox(12);
        content.setStyle("-fx-padding: 20;");

        try {
            List<DemandePojo> demandes = demandeCRUD.getDemandesEnAttente();

            if (demandes.isEmpty()) {
                Label empty = new Label("✅  Aucune demande en attente.");
                empty.setStyle("-fx-text-fill: #64748B; -fx-font-size: 14px; -fx-padding: 30 0;");
                VBox emptyBox = new VBox(empty); emptyBox.setAlignment(Pos.CENTER);
                content.getChildren().add(emptyBox);
            } else {
                Label countLbl = new Label(demandes.size() + " demande(s) en attente");
                countLbl.setStyle("-fx-text-fill: #FFBD00; -fx-font-weight: 900; -fx-font-size: 12px; -fx-background-color: rgba(255,189,0,0.12); -fx-background-radius: 6; -fx-padding: 6 12;");

                VBox listBox = new VBox(8); listBox.setStyle("-fx-padding: 0 0 10 0;");
                for (DemandePojo d : demandes) listBox.getChildren().add(buildDemandeCard(d, listBox));

                ScrollPane scroll = new ScrollPane(listBox);
                scroll.setFitToWidth(true); scroll.setMaxHeight(450);
                scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
                content.getChildren().addAll(countLbl, scroll);
            }
        } catch (SQLException e) {
            Label err = new Label("❌ Erreur chargement : " + e.getMessage());
            err.setStyle("-fx-text-fill: #F87171; -fx-font-size: 12px;");
            content.getChildren().add(err);
        }

        panel.getChildren().addAll(header, content);
        return panel;
    }

    // ── Carte d'une demande ───────────────────────────────────
    private VBox buildDemandeCard(DemandePojo d, VBox listBox) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #1E293B; -fx-background-radius: 14; -fx-padding: 16 18; -fx-border-color: rgba(255,255,255,0.07); -fx-border-radius: 14; -fx-border-width: 1;");

        Label nomLbl = new Label("👤  " + d.nom);
        nomLbl.setStyle("-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 14px;");

        HBox meta = new HBox(20);
        meta.getChildren().addAll(metaItem("📧", d.email), metaItem("📞", d.tel), metaItem("🏷", d.type));

        Label dateLbl = new Label("📅  " + d.date);
        dateLbl.setStyle("-fx-text-fill: #475569; -fx-font-size: 11px;");

        String motifCourt = d.motivation != null && d.motivation.length() > 150 ? d.motivation.substring(0,150)+"…" : d.motivation;
        Label motivLbl = new Label("💬  " + motifCourt);
        motivLbl.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px;"); motivLbl.setWrapText(true);

        Label feedbackLbl = new Label("");
        feedbackLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: 800;"); feedbackLbl.setWrapText(true);

        Button approveBtn = new Button("✅  Approuver");
        approveBtn.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; -fx-font-weight: 900; -fx-background-radius: 8; -fx-padding: 9 20; -fx-cursor: hand;");

        Button refuseBtn = new Button("❌  Refuser");
        refuseBtn.setStyle("-fx-background-color: #DC2626; -fx-text-fill: white; -fx-font-weight: 900; -fx-background-radius: 8; -fx-padding: 9 20; -fx-cursor: hand;");

        approveBtn.setOnAction(e -> {
            try {
                demandeCRUD.approuver(d.id, d.idUtilisateur);
                listBox.getChildren().remove(card);
                feedbackLbl.setText("✅ Approuvé — " + d.nom + " est maintenant Commerçant.");
                feedbackLbl.setStyle("-fx-text-fill: #4ADE80; -fx-font-size: 12px; -fx-font-weight: 800;");
                // Afficher le feedback temporairement avant la suppression de la carte
                VBox feedbackCard = new VBox(feedbackLbl);
                feedbackCard.setStyle("-fx-padding: 10 14; -fx-background-color: rgba(74,222,128,0.1); -fx-background-radius: 8;");
                listBox.getChildren().add(0, feedbackCard);
            } catch (Exception ex) {
                feedbackLbl.setText("❌ Erreur : " + ex.getMessage());
                feedbackLbl.setStyle("-fx-text-fill: #F87171; -fx-font-size: 12px;");
                if (!card.getChildren().contains(feedbackLbl)) card.getChildren().add(feedbackLbl);
            }
        });

        refuseBtn.setOnAction(e -> {
            // Afficher un champ de commentaire inline
            approveBtn.setDisable(true); refuseBtn.setDisable(true);
            TextField commentField = new TextField();
            commentField.setPromptText("Raison du refus (optionnel)...");
            commentField.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-border-color: rgba(255,255,255,0.12); -fx-border-radius: 8; -fx-text-fill: white; -fx-padding: 8 10; -fx-prompt-text-fill: rgba(255,255,255,0.3);");
            Button confirmRefuse = new Button("Confirmer le refus");
            confirmRefuse.setStyle("-fx-background-color: #991B1B; -fx-text-fill: white; -fx-font-weight: 900; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");
            Button cancelRefuse = new Button("Annuler");
            cancelRefuse.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: #94A3B8; -fx-background-radius: 8; -fx-padding: 8 16; -fx-cursor: hand;");

            confirmRefuse.setOnAction(ev -> {
                try {
                    demandeCRUD.refuser(d.id, commentField.getText());
                    listBox.getChildren().remove(card);
                    VBox feedbackCard = new VBox(new Label("🚫 Refusé — " + d.nom));
                    feedbackCard.getChildren().get(0).setStyle("-fx-text-fill: #F87171; -fx-font-weight: 800; -fx-font-size: 12px;");
                    feedbackCard.setStyle("-fx-padding: 10 14; -fx-background-color: rgba(248,113,113,0.1); -fx-background-radius: 8;");
                    listBox.getChildren().add(0, feedbackCard);
                } catch (Exception ex) {
                    feedbackLbl.setText("❌ " + ex.getMessage());
                    feedbackLbl.setStyle("-fx-text-fill: #F87171;");
                    if (!card.getChildren().contains(feedbackLbl)) card.getChildren().add(feedbackLbl);
                }
            });

            cancelRefuse.setOnAction(ev -> {
                card.getChildren().removeAll(commentField, confirmRefuse, cancelRefuse);
                approveBtn.setDisable(false); refuseBtn.setDisable(false);
            });

            card.getChildren().addAll(commentField, new HBox(8, confirmRefuse, cancelRefuse));
        });

        HBox actions = new HBox(12, approveBtn, refuseBtn);
        actions.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().addAll(nomLbl, meta, dateLbl, motivLbl, actions);
        return card;
    }

    // ── Helper ────────────────────────────────────────────────
    private Label metaItem(String icon, String value) {
        Label l = new Label(icon + "  " + (value != null ? value : "—"));
        l.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px; -fx-font-weight: 700;");
        return l;
    }
}