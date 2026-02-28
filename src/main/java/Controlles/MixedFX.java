package Controlles;
import Services.BusinessLogic;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import Entites.Reservation;
import Entites.Transport;
import Services.ReservationCRUD;
import Services.TransportAPI;
import Services.TransportCRUD;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MixedFX {

    @FXML private Button reservationToggleBtn, transportToggleBtn;
    @FXML private FlowPane itemsFlowPane;
    @FXML private GridPane formGrid, detailGrid;
    @FXML private Label formTitle;
    @FXML private Button addBtn, editBtn, deleteBtn, clearBtn;
    @FXML private ScrollPane listScroll, detailScroll;
    @FXML private TextField searchField;
    @FXML private Label totalCountLabel;
    @FXML private VBox formOverlay;
    private javafx.collections.ObservableSet<String> dynamicWishlist = javafx.collections.FXCollections.observableSet();
    private String currentUserId = "NormanHaires";
    private boolean filterByWishlist = false;
    @FXML private Button wishlistFilterBtn;
    @FXML private WebView mapWebView;
    @FXML private Label mapRouteLabel;
    @FXML private ProgressBar mapProgressBar;
    @FXML private Label priceNoteLabel;
    @FXML private ImageView qrCodeView;

    private boolean showingReservations = true;

    private ReservationCRUD reservationService = new ReservationCRUD();
    private TransportCRUD transportService = new TransportCRUD();

    private List<Reservation> reservationList;
    private List<Transport> transportList;

    private Object selectedItem;

    // Persistent form fields
    private TextField resUserField, resExpField, resStatutField;
    private DatePicker resDateField;

    private TextField trTypeField, trCapField, trTarifField, trDepartField, trArriveeField;
    private DatePicker trDateField;

    private final String inputStyle = "-fx-background-color: #F8FAFC; -fx-background-radius: 50; -fx-border-color: #E2E8F0; -fx-border-radius: 50; -fx-padding: 8 15; -fx-text-fill: #1E293B;";

    @FXML
    public void initialize() {
        setupFormFields();
        loadReservations();
        loadTransports();

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterItems(newValue);
        });
        if (qrCodeView == null) System.out.println("❌ ERREUR: qrCodeView n'est pas lié au FXML !");
        else System.out.println("✅ OK: qrCodeView est prêt.");

        if (priceNoteLabel == null) System.out.println("❌ ERREUR: priceNoteLabel n'est pas lié au FXML !");
    }

    private void filterItems(String query) {
        if (showingReservations) {
            if (reservationList == null) return;
            List<Reservation> filtered = reservationList.stream()
                    .filter(r -> r.getUser().toLowerCase().contains(query.toLowerCase()) ||
                            r.getExp().toLowerCase().contains(query.toLowerCase()) ||
                            r.getStatut().toLowerCase().contains(query.toLowerCase()))
                    .toList();
            populateItems(filtered);
        } else {
            if (transportList == null) return;
            List<Transport> filtered = transportList.stream()
                    .filter(t -> t.getType().toLowerCase().contains(query.toLowerCase()) ||
                            t.getCapacite().toLowerCase().contains(query.toLowerCase()) ||
                            t.getDepart().toLowerCase().contains(query.toLowerCase()) ||
                            t.getArrivee().toLowerCase().contains(query.toLowerCase()))
                    .toList();
            populateItems(filtered);
        }
    }

    @FXML
    private void toggleWishlistFilter() {
        filterByWishlist = !filterByWishlist;

        if (filterByWishlist) {
            // Style bouton actif (Rouge)
            wishlistFilterBtn.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #EF4444; -fx-background-radius: 10; -fx-padding: 10; -fx-font-size: 16px;");

            // Filtrer la liste actuelle pour ne garder que les favoris
            if (showingReservations) {
                List<Reservation> filtered = reservationList.stream()
                        .filter(r -> dynamicWishlist.contains("RES_" + r.getId()))
                        .toList();
                populateItems(filtered);
            } else {
                List<Transport> filtered = transportList.stream()
                        .filter(t -> dynamicWishlist.contains("TR_" + t.getId()))
                        .toList();
                populateItems(filtered);
            }
        } else {
            // Style bouton inactif (Gris)
            wishlistFilterBtn.setStyle("-fx-background-color: white; -fx-text-fill: #CBD5E1; -fx-background-radius: 10; -fx-padding: 10; -fx-font-size: 16px;");

            // Recharger la liste complète selon l'onglet actif
            reloadCurrent();
        }
    }

    private void setupFormFields() {
        formGrid.getChildren().clear();
        if (showingReservations) {
            formTitle.setText("Formulaire Réservation");
            resUserField = new TextField(); resUserField.setStyle(inputStyle);
            resExpField = new TextField(); resExpField.setStyle(inputStyle);
            resStatutField = new TextField(); resStatutField.setStyle(inputStyle);
            resDateField = new DatePicker(); resDateField.setStyle(inputStyle);
            formGrid.add(createStyledLabel("User:"), 0, 0); formGrid.add(resUserField, 1, 0);
            formGrid.add(createStyledLabel("Exp:"), 0, 1); formGrid.add(resExpField, 1, 1);
            formGrid.add(createStyledLabel("Statut:"), 0, 2); formGrid.add(resStatutField, 1, 2);
            formGrid.add(createStyledLabel("Date:"), 0, 3); formGrid.add(resDateField, 1, 3);
        } else {
            formTitle.setText("Formulaire Transport");
            trTypeField = new TextField(); trTypeField.setStyle(inputStyle);
            trCapField = new TextField(); trCapField.setStyle(inputStyle);
            trTarifField = new TextField(); trTarifField.setStyle(inputStyle);
            trDepartField = new TextField(); trDepartField.setStyle(inputStyle);
            trArriveeField = new TextField(); trArriveeField.setStyle(inputStyle);
            trDateField = new DatePicker(); trDateField.setStyle(inputStyle);
            formGrid.add(createStyledLabel("Type:"), 0, 0); formGrid.add(trTypeField, 1, 0);
            formGrid.add(createStyledLabel("Capacité:"), 0, 1); formGrid.add(trCapField, 1, 1);
            formGrid.add(createStyledLabel("Tarif:"), 0, 2); formGrid.add(trTarifField, 1, 2);
            formGrid.add(createStyledLabel("Départ:"), 0, 3); formGrid.add(trDepartField, 1, 3);
            formGrid.add(createStyledLabel("Arrivée:"), 0, 4); formGrid.add(trArriveeField, 1, 4);
            formGrid.add(createStyledLabel("Date départ:"), 0, 5); formGrid.add(trDateField, 1, 5);
        }
    }

    private Label createStyledLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #64748B; -fx-font-weight: bold;");
        return label;
    }





    // --- GÉNÉRATION QR CODE ---
    public static Image generateQRCode(String data) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 200, 200);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

            return new Image(new ByteArrayInputStream(pngOutputStream.toByteArray()));
        } catch (Exception e) {
            return null;
        }
    }
    @FXML private void closeFormOverlay() { formOverlay.setVisible(false); }
    @FXML

    private void handleShowAddForm() {
        clearForm();
        formTitle.setText("Nouvelle " + (showingReservations ? "Réservation" : "Transport"));
        addBtn.setVisible(true);
        editBtn.setVisible(false);
        formOverlay.setVisible(true); // Show the popup!
    }

    @FXML
    private void showReservations() {
        showingReservations = true;
        reservationToggleBtn.setStyle("-fx-background-color: #A3B1FF; -fx-text-fill: white; -fx-background-radius: 20;");
        transportToggleBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8;");
        setupFormFields(); loadReservations(); selectedItem = null; searchField.clear();
    }

    @FXML
    private void showTransports() {
        showingReservations = false;
        transportToggleBtn.setStyle("-fx-background-color: #A3B1FF; -fx-text-fill: white; -fx-background-radius: 20;");
        reservationToggleBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8;");
        setupFormFields(); loadTransports(); selectedItem = null; searchField.clear();
    }


    private void loadReservations() {
        try {
            reservationList = reservationService.getAll();
            if (showingReservations) populateItems(reservationList);
            if (showingReservations && totalCountLabel != null) totalCountLabel.setText(String.valueOf(reservationList.size()));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadTransports() {
        try {
            transportList = transportService.getAll();
            if (!showingReservations) populateItems(transportList);
            if (!showingReservations && totalCountLabel != null) totalCountLabel.setText(String.valueOf(transportList.size()));
        } catch (SQLException e) { e.printStackTrace(); }
    }
    private void updateMapDisplay(Transport t) {
        if (mapWebView == null) return;

        // 1. Clean the strings for URL (handle spaces and special characters)
        try {
            String origin = java.net.URLEncoder.encode(t.getDepart(), "UTF-8");
            String destination = java.net.URLEncoder.encode(t.getArrivee(), "UTF-8");

            // 2. Use the official Google Maps search/dir URL
            // 'dir' stands for directions, 'api=1' is the current platform version
            String url = "https://www.google.com/maps/dir/?api=1&origin=" + origin + "&destination=" + destination + "&travelmode=driving";

            javafx.application.Platform.runLater(() -> {
                WebEngine engine = mapWebView.getEngine();

                // Optional: Set a User-Agent to ensure the mobile/web view renders correctly
                engine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

                engine.load(url);

                if (mapRouteLabel != null) {
                    mapRouteLabel.setText(t.getDepart() + " ➔ " + t.getArrivee());
                }
            });
        } catch (java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void populateItems(List<?> items) {
        itemsFlowPane.getChildren().clear();
        int index = 0;

        for (Object obj : items) {
            VBox card = null;
            String uniqueKey = "";

            if (showingReservations && obj instanceof Reservation) {
                Reservation r = (Reservation) obj;
                uniqueKey = "RES_" + r.getId();
                String dateStr = (r.getDate() != null) ? r.getDate().toString().split(" ")[0] : "Pas de date";
                card = createModernCard(r.getUser(), "Statut: " + r.getStatut(), dateStr, null);

            } else if (!showingReservations && obj instanceof Transport) {
                Transport t = (Transport) obj;
                uniqueKey = "TR_" + t.getId();
                card = createModernCard(t.getType(), t.getDepart() + " ➔ " + t.getArrivee(), t.getTarif() + " TND", null);

                VBox textArea = (VBox) card.lookup("#textArea");
                if (textArea != null) {
                    ProgressBar travelProgress = new ProgressBar(0);
                    travelProgress.setPrefWidth(170);
                    travelProgress.setStyle("-fx-accent: #EF4444;");
                    Label apiNote = new Label("⏳ Initialisation GPS...");
                    apiNote.setStyle("-fx-font-size: 10px; -fx-text-fill: #94A3B8;");

                    textArea.getChildren().addAll(new Label("Suivi du trajet:"), travelProgress, apiNote);

                    final int delay = index * 1000;
                    new Thread(() -> {
                        try {
                            Thread.sleep(delay); // Keep your staggered loading logic
                            String infos = Services.TransportAPI.getInfosTrajet(t.getDepart(), t.getArrivee());

                            // Local price calculation for the card initialization
                            float initialPrice = calculateDistanceBasedPrice(t, infos);

                            javafx.application.Platform.runLater(() -> {
                                apiNote.setText("🏁 " + infos);
                                apiNote.setStyle("-fx-text-fill: #22C55E; -fx-font-weight: bold; -fx-font-size: 10px;");

                                // If this card is already selected when the thread finishes, update the main label
                                if (selectedItem == t && priceNoteLabel != null) {
                                    priceNoteLabel.setText("💰 Prix par Distance: " + String.format("%.2f", initialPrice) + " TND");
                                }
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                }
                index++;
            }

            if (card != null) {
                Pane topPane = (Pane) card.getChildren().get(0);
                // Dans populateItems, là où tu ajoutes les boutons (🗑, 👁, ❤)

                Button currencyBtn = new Button("💰"); // Ou une icône de monnaie si tu as une image
                currencyBtn.setTranslateX(50); // Ajuste selon tes autres boutons
                currencyBtn.setTranslateY(10);

// Style CUTE pour le bouton de la carte
                currencyBtn.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #A3B1FF; " +
                        "-fx-background-radius: 50; -fx-border-color: #EEF2FF; " +
                        "-fx-border-width: 2; -fx-font-size: 18px; -fx-padding: 5;");

                currencyBtn.setOnAction(e -> {
                    if (obj instanceof Transport) {
                        showCurrencyPopup((Transport) obj);
                    }
                    e.consume(); // Empêche le clic d'ouvrir la carte complète
                });

                currencyBtn.setOnAction(e -> {
                    if (obj instanceof Transport) {
                        showCurrencyPopup((Transport) obj);
                    }
                    e.consume();
                });
                topPane.getChildren().add(currencyBtn);

                // 1. DELETE BUTTON
                Button cardDelBtn = new Button("🗑");
                cardDelBtn.setTranslateX(130); cardDelBtn.setTranslateY(10);
                cardDelBtn.setStyle("-fx-background-color: #FDA4AF; -fx-text-fill: white; -fx-background-radius: 10;");
                cardDelBtn.setOnAction(e -> { selectedItem = obj; handleDelete(); e.consume(); });
                topPane.getChildren().add(cardDelBtn);

                // 2. DETAILS BUTTON
                Button cardDetailBtn = new Button("👁");
                cardDetailBtn.setStyle("-fx-background-color: #A3B1FF; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-weight: bold;");
                cardDetailBtn.setTranslateX(90); cardDetailBtn.setTranslateY(10);
                cardDetailBtn.setOnAction(e -> { showDetails(obj); e.consume(); });
                topPane.getChildren().add(cardDetailBtn);

                // 3. WISHLIST BUTTON
                Button wishlistBtn = new Button("❤");
                wishlistBtn.setTranslateX(170); wishlistBtn.setTranslateY(-10);
                updateWishlistButtonStyle(wishlistBtn, dynamicWishlist.contains(uniqueKey));

                final String finalKey = uniqueKey;
                wishlistBtn.setOnAction(e -> {
                    if (!dynamicWishlist.contains(finalKey)) dynamicWishlist.add(finalKey);
                    else dynamicWishlist.remove(finalKey);
                    updateWishlistButtonStyle(wishlistBtn, dynamicWishlist.contains(finalKey));
                    e.consume();
                });
                topPane.getChildren().add(wishlistBtn);

                // 4. CLICK HANDLER (Map + Form + UPDATED Price Logic)
                // 4. CLICK HANDLER (Map + Form + UPDATED Price Logic)
                card.setOnMouseClicked(e -> {
                    selectedItem = obj;
                    fillFormFields(obj);
                    populateForm();

                    // --- RESTORE MODIFY POPUP LOGIC ---
                    formTitle.setText("Modifier " + (showingReservations ? "la Réservation" : "le Transport"));
                    addBtn.setVisible(false);    // Hide Add button
                    editBtn.setVisible(true);    // Show Edit button
                    formOverlay.setVisible(true); // Open the popup
                    // ---------------------------------

                    if (obj instanceof Transport) {
                        Transport t = (Transport) obj;
                        updateMapDisplay(t);
                        updatePriceDisplay(t);
                    }

                    if (e.getClickCount() == 2) showDetails(obj);
                });

                itemsFlowPane.getChildren().add(card);
            }
        }
    }
    private void showCurrencyPopup(Transport t) {
        // 1. Appliquer l'effet de flou à l'arrière-plan
        GaussianBlur blur = new GaussianBlur(15);
        itemsFlowPane.getParent().setEffect(blur); // Floute le conteneur principal

        new Thread(() -> {
            try {
                // Calcul du prix réel via l'API
                String infos = Services.TransportAPI.getInfosTrajet(t.getDepart(), t.getArrivee());
                float priceInTND = calculateDistanceBasedPrice(t, infos);

                javafx.application.Platform.runLater(() -> {
                    // 2. Créer la fenêtre Pop-up (Stage)
                    Stage popupStage = new Stage();
                    popupStage.initModality(Modality.APPLICATION_MODAL);
                    popupStage.initStyle(StageStyle.TRANSPARENT); // Pour des coins vraiment arrondis

                    // 3. Conteneur principal (Layout)
                    VBox layout = new VBox(20);
                    layout.setPadding(new Insets(30));
                    layout.setAlignment(Pos.CENTER);

                    // Style CUTE : Fond blanc, coins très arrondis, ombre douce, bordure légère
                    layout.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 30; " +
                            "-fx-border-color: #EEF2FF; -fx-border-radius: 30; -fx-border-width: 2; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 20, 0, 0, 10);");

                    // --- ÉLÉMENTS VISUELS ---

                    // Icône d'en-tête (Optionnelle mais cute)
                    Label iconHeader = new Label("✨");
                    iconHeader.setStyle("-fx-font-size: 40px;");

                    // Titre
                    Label title = new Label("Estimation de votre trajet");
                    title.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-weight: 900; -fx-font-size: 18px; -fx-text-fill: #1E293B;");

                    // Icône de monnaie + Prix
                    HBox priceBox = new HBox(10);
                    priceBox.setAlignment(Pos.CENTER);
                    Label coinIcon = new Label("💰"); coinIcon.setStyle("-fx-font-size: 24px;");
                    Label priceLabel = new Label(String.format("%.3f TND", priceInTND));
                    priceLabel.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 28px; -fx-text-fill: #A3B1FF; -fx-font-weight: 900;"); // Ton bleu signature
                    priceBox.getChildren().addAll(coinIcon, priceLabel);

                    // Sélecteur de devise
                    ComboBox<String> currencies = new ComboBox<>();
                    currencies.getItems().addAll("TND (Dinar)", "EUR (Euro)", "USD (Dollar)", "CAD (Dollar)");
                    currencies.setValue("TND (Dinar)");

                    // Style cute pour le ComboBox
                    currencies.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 50; " +
                            "-fx-border-color: #E2E8F0; -fx-border-radius: 50; -fx-padding: 8 15;");

                    // Logique de conversion en direct
                    currencies.setOnAction(ev -> {
                        float rate = 1.0f; String sym = "TND";
                        switch (currencies.getValue()) {
                            case "EUR (Euro)": rate = 0.30f; sym = "EUR"; break;
                            case "USD (Dollar)": rate = 0.32f; sym = "USD"; break;
                            case "CAD (Dollar)": rate = 0.44f; sym = "CAD"; break;
                        }
                        priceLabel.setText(String.format("%.3f %s", priceInTND * rate, sym));
                    });

                    // Bouton Fermer (Style identique à tes boutons d'action)
                    Button closeBtn = new Button("Fermer");
                    closeBtn.setPrefWidth(150);
                    closeBtn.setStyle("-fx-background-color: #A3B1FF; -fx-text-fill: white; " +
                            "-fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 14px;");

                    // --- GESTION DE LA FERMETURE ---
                    closeBtn.setOnAction(ev -> {
                        popupStage.close();
                        itemsFlowPane.getParent().setEffect(null); // Retirer le flou
                    });

                    // Gérer aussi la fermeture si on clique en dehors (UX cute)
                    layout.setOnMouseClicked(event -> {
                        if (event.getTarget() == layout) { // Si on clique sur le fond vide du popup
                            // On ne fait rien, on attend le bouton fermer
                        }
                    });

                    layout.getChildren().addAll(iconHeader, title, priceBox, currencies, closeBtn);

                    // Scene transparente pour les coins arrondis du layout
                    Scene scene = new Scene(layout);
                    scene.setFill(Color.TRANSPARENT);
                    popupStage.setScene(scene);
                    popupStage.show();
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }
    public float calculateDistanceBasedPrice(Transport t, String apiResponse) {
        try {
            // Extraction du nombre de kilomètres (ex: "150.5 km" -> 150.5)
            String distancePart = apiResponse.split(" km")[0];
            String numericOnly = distancePart.replaceAll("[^0-9.]", "");

            if (numericOnly.isEmpty()) return t.getTarif();
            float distance = Float.parseFloat(numericOnly);

            float prixBase = 0;
            String type = t.getType().toLowerCase();

            // --- TARIFICATION SELON LE TYPE (Standard Tunisie) ---
            if (type.contains("louage")) {
                // Environ 0.080 TND par km (Tarif interurbain standard)
                prixBase = distance * 0.085f;
            }
            else if (type.contains("taxi") || type.contains("privé")) {
                // Compteur : Prise en charge (~0.900) + ~0.500/km (urbain) ou plus (interurbain)
                prixBase = 0.900f + (distance * 0.600f);
            }
            else if (type.contains("bus") || type.contains("train")) {
                // Tarif transport public (très bas)
                prixBase = distance * 0.040f;
            }
            else if (type.contains("avion") || type.contains("luxe")) {
                // Forfait luxe ou vol interne (Tunisair Express par ex)
                prixBase = 100.0f + (distance * 0.200f);
            }
            else {
                // Par défaut si le type est inconnu
                prixBase = distance * 0.100f;
            }

            // --- MAJORATIONS SPÉCIFIQUES ---

            // 1. Majoration de Nuit / Heure de pointe (7h-9h / 17h-19h)
            int heure = t.getDateDepart().getHour();
            if ((heure >= 7 && heure <= 9) || (heure >= 17 && heure <= 19)) {
                prixBase *= 1.25; // +25% (Majorations urbaines classiques)
            }

            // 2. Majoration Bagages (Forfaitaire si distance > 100km)
            if (distance > 100) {
                prixBase += 2.0f; // 2 TND pour les bagages en louage/bus
            }

            return prixBase;

        } catch (Exception e) {
            System.err.println("❌ Erreur calcul prix Tunisie: " + e.getMessage());
            return t.getTarif(); // Retourne le tarif de la base de données en secours
        }
    }
    private void updatePriceDisplay(Transport t) {
        if (priceNoteLabel == null) return;

        priceNoteLabel.setText("⏳ Calcul du tarif " + t.getType() + "...");

        new Thread(() -> {
            try {
                String infos = Services.TransportAPI.getInfosTrajet(t.getDepart(), t.getArrivee());
                float finalPrice = calculateDistanceBasedPrice(t, infos);

                javafx.application.Platform.runLater(() -> {
                    String formatPrix = String.format("%.3f", finalPrice); // 3 décimales pour les Millimes
                    priceNoteLabel.setText("💰 Tarif Estimé (" + t.getType() + "): " + formatPrix + " TND");

                    // Style selon le prix
                    if (finalPrice > 50) {
                        priceNoteLabel.setStyle("-fx-text-fill: #E11D48; -fx-font-weight: bold;"); // Rouge si cher
                    } else {
                        priceNoteLabel.setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;"); // Vert si abordable
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> priceNoteLabel.setText("⚠️ Erreur calcul"));
            }
        }).start();
    }

    private void updateWishlistButtonStyle(Button btn, boolean isFavorite) {
        if (isFavorite) {
            // Vibrant Red for Active Favorited State
            btn.setStyle("-fx-background-color: transparent; " +
                    "-fx-text-fill: #EF4444; " +
                    "-fx-font-size: 22px; " +
                    "-fx-cursor: hand; " +
                    "-fx-padding: 0;");
        } else {
            // Soft Slate Gray for Inactive State
            btn.setStyle("-fx-background-color: transparent; " +
                    "-fx-text-fill: #CBD5E1; " +
                    "-fx-font-size: 22px; " +
                    "-fx-cursor: hand; " +
                    "-fx-padding: 0;");
        }
    }
    private VBox createModernCard(String title, String subtitle, String extraInfo, String imageUrl) {
        VBox card = new VBox();
        card.setPrefSize(210, 320);
        card.setMaxSize(210, 320);
        card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 25; -fx-border-color: #EEF2FF; -fx-border-radius: 25; -fx-border-width: 2;");
        DropShadow shadow = new DropShadow(20, Color.web("#0000000C"));
        shadow.setOffsetY(8);
        card.setEffect(shadow);

        Pane imagePane = new Pane();
        imagePane.setPrefHeight(110);
        String bgImage = (imageUrl != null && !imageUrl.isEmpty()) ? imageUrl : "/assets/testt.jpg";
        imagePane.setStyle("-fx-background-color: #E2E8F0; -fx-background-radius: 23 23 0 0; -fx-background-size: cover; -fx-background-position: center; -fx-background-image: url('" + bgImage + "');");

        VBox textArea = new VBox(8);
        textArea.setId("textArea");
        textArea.setPadding(new Insets(12));
        Label titleLabel = new Label(title); titleLabel.setStyle("-fx-text-fill: #1E293B; -fx-font-weight: 900; -fx-font-size: 15px;");
        Label subtitleLabel = new Label(subtitle); subtitleLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label extraLabel = new Label(extraInfo); extraLabel.setStyle("-fx-text-fill: #A3B1FF; -fx-font-weight: 900; -fx-font-size: 13px;");

        textArea.getChildren().addAll(titleLabel, subtitleLabel, extraLabel);
        card.getChildren().addAll(imagePane, textArea);

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 25; -fx-border-color: #A3B1FF; -fx-border-radius: 25; -fx-border-width: 2; -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 25; -fx-border-color: #EEF2FF; -fx-border-radius: 25; -fx-border-width: 2;"));

        return card;
    }

    private void fillFormFields(Object item) {
        if (showingReservations && item instanceof Reservation r) {
            resUserField.setText(r.getUser());
            resExpField.setText(r.getExp());
            resStatutField.setText(r.getStatut());
            if (r.getDate() != null) {
                resDateField.setValue(r.getDate().toLocalDateTime().toLocalDate());
            }
        } else if (!showingReservations && item instanceof Transport t) {
            trTypeField.setText(t.getType());
            trCapField.setText(t.getCapacite());
            trTarifField.setText(String.valueOf(t.getTarif()));
            trDepartField.setText(t.getDepart());
            trArriveeField.setText(t.getArrivee());
            if (t.getDateDepart() != null) {
                trDateField.setValue(t.getDateDepart().toLocalDate());
            }
        }
    }

    private void populateForm() {
        formGrid.getChildren().clear();
        if (showingReservations) {
            formTitle.setText("Formulaire Réservation");
            formGrid.add(createStyledLabel("User:"), 0, 0); formGrid.add(resUserField, 1, 0);
            formGrid.add(createStyledLabel("Exp:"), 0, 1); formGrid.add(resExpField, 1, 1);
            formGrid.add(createStyledLabel("Statut:"), 0, 2); formGrid.add(resStatutField, 1, 2);
            formGrid.add(createStyledLabel("Date:"), 0, 3); formGrid.add(resDateField, 1, 3);
        } else {
            formTitle.setText("Formulaire Transport");
            formGrid.add(createStyledLabel("Type:"), 0, 0); formGrid.add(trTypeField, 1, 0);
            formGrid.add(createStyledLabel("Capacité:"), 0, 1); formGrid.add(trCapField, 1, 1);
            formGrid.add(createStyledLabel("Tarif:"), 0, 2); formGrid.add(trTarifField, 1, 2);
            formGrid.add(createStyledLabel("Départ:"), 0, 3); formGrid.add(trDepartField, 1, 3);
            formGrid.add(createStyledLabel("Arrivée:"), 0, 4); formGrid.add(trArriveeField, 1, 4);
            formGrid.add(createStyledLabel("Date départ:"), 0, 5); formGrid.add(trDateField, 1, 5);
        }
    }

    @FXML
    private void handleAdd() {
        try {
            if (showingReservations) {
                if (!validateReservation()) return;
                Reservation r = new Reservation();
                r.setUser(resUserField.getText()); r.setExp(resExpField.getText()); r.setStatut(resStatutField.getText());
                r.setDate(java.sql.Timestamp.valueOf(resDateField.getValue().atStartOfDay()));
                reservationService.ajouter(r);
            } else {
                if (!validateTransport()) return;
                Transport t = new Transport();
                t.setType(trTypeField.getText()); t.setCapacite(trCapField.getText()); t.setTarif(Float.parseFloat(trTarifField.getText()));
                t.setDepart(trDepartField.getText()); t.setArrivee(trArriveeField.getText()); t.setDateDepart(trDateField.getValue().atStartOfDay());
                transportService.ajouter(t);
            }
            showAlert("Succès", "Ajouté avec succès !", Alert.AlertType.INFORMATION);
            reloadCurrent();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private boolean validateReservation() { return !resUserField.getText().isEmpty(); }
    private boolean validateTransport() { return !trTypeField.getText().isEmpty(); }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }

    @FXML private void handleEdit() {
        if (selectedItem == null) {
            System.out.println("❌ Aucun élément sélectionné pour la modification.");
            return;
        }

        try {
            if (showingReservations && selectedItem instanceof Reservation r) {
                // Update the object from the text fields
                r.setUser(resUserField.getText());
                r.setExp(resExpField.getText());
                r.setStatut(resStatutField.getText());
                if (resDateField.getValue() != null) {
                    r.setDate(java.sql.Timestamp.valueOf(resDateField.getValue().atStartOfDay()));
                }
                // Call CRUD
                reservationService.modifier(r);

            } else if (!showingReservations && selectedItem instanceof Transport t) {
                // Update the object from the text fields
                t.setType(trTypeField.getText());
                t.setCapacite(trCapField.getText());
                t.setTarif(Float.parseFloat(trTarifField.getText()));
                t.setDepart(trDepartField.getText());
                t.setArrivee(trArriveeField.getText());
                if (trDateField.getValue() != null) {
                    t.setDateDepart(trDateField.getValue().atStartOfDay());
                }
                // Call CRUD
                transportService.modifier(t);
            }

            formOverlay.setVisible(false); // Close popup
            reloadCurrent(); // Refresh the list
            System.out.println("✅ Modification réussie !");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML private void handleDelete() {
        if (selectedItem == null) {
            System.out.println("❌ Aucun élément sélectionné pour la suppression.");
            return;
        }

        try {
            if (showingReservations && selectedItem instanceof Reservation r) {
                reservationService.supprimer(r.getId());
            } else if (!showingReservations && selectedItem instanceof Transport t) {
                transportService.supprimer(t.getId());
            }

            clearForm();    // Reset the fields
            reloadCurrent(); // Refresh the UI
            System.out.println("🗑️ Suppression réussie !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void clearForm() {
        selectedItem = null;
        if (showingReservations) {
            resUserField.clear();
            resExpField.clear();
            resStatutField.clear();
            resDateField.setValue(null);
        } else {
            trTypeField.clear();
            trCapField.clear();
            trTarifField.clear();
            trDepartField.clear();
            trArriveeField.clear();
            trDateField.setValue(null);
        }
    }


    private void reloadCurrent() {
        if (showingReservations) {
            loadReservations();
        } else {
            loadTransports();
        }
    }

    private void showDetails(Object obj) {
        // 1. Clear interference
        formOverlay.setVisible(false);
        listScroll.setVisible(false);
        listScroll.setManaged(false);

        // 2. Bring Detail Page to the absolute front
        detailScroll.setVisible(true);
        detailScroll.setManaged(true);
        detailScroll.toFront();

        detailGrid.getChildren().clear();

        if (obj instanceof Reservation r) {
            detailGrid.add(createStyledLabel("ID Réservation: " + r.getId()), 0, 0);
            detailGrid.add(createStyledLabel("Passager: " + r.getUser()), 0, 1);
            String dataToEncode = "https://triplove.tn/card?type=" + (obj instanceof Reservation ? "res" : "tr") ;
            qrCodeView.setImage(generateQRCode("RES-" + r.getId() + "-" + r.getUser()));
            qrCodeView.setCursor(Cursor.HAND);
            qrCodeView.setOnMouseClicked(e -> {
                showCuteSuccessPopup(obj);
            });
        } else if (obj instanceof Transport t) {
            detailGrid.add(createStyledLabel("ID Transport: " + t.getId()), 0, 0);
            detailGrid.add(createStyledLabel("Trajet: " + t.getDepart() + " -> " + t.getArrivee()), 0, 1);
            String dataToEncode = "https://triplove.tn/card?type=" + (obj instanceof Reservation ? "res" : "tr") ;
            qrCodeView.setImage(generateQRCode(dataToEncode));
            qrCodeView.setCursor(Cursor.HAND);
            qrCodeView.setOnMouseClicked(e -> {
                showCuteSuccessPopup(t);
            });
        }
    }
    private void showCuteSuccessPopup(Object obj) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);

        WebView webView = new WebView();
        webView.setPrefSize(500, 650);

        String title = (obj instanceof Reservation) ? "Réservation Confirmée !" : "Transport Prêt !";
        String details = (obj instanceof Reservation r) ? r.getUser() : "Bon voyage !";

        // HTML "CUTE" DYNAMIQUE
        String htmlContent = """
        <html>
        <head>
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <style>
                body { font-family: 'Segoe UI', sans-serif; background: linear-gradient(135deg, #f5f7ff 0%, #ffffff 100%); text-align: center; padding: 20px; }
                .card { background: white; border-radius: 40px; padding: 30px; box-shadow: 0 20px 40px rgba(163, 177, 255, 0.2); }
                .logo { width: 60px; filter: drop-shadow(0 4px 8px rgba(0,0,0,0.1)); }
                h1 { color: #A3B1FF; font-size: 24px; margin: 15px 0; }
                .confetti { font-size: 40px; animation: party 1s infinite alternate; }
                @keyframes party { from { transform: scale(1); } to { transform: scale(1.2); } }
                #map { height: 250px; border-radius: 25px; margin: 20px 0; border: 3px solid #EEF2FF; }
                .btn { background: #A3B1FF; color: white; padding: 15px 30px; border-radius: 50px; text-decoration: none; font-weight: bold; border: none; cursor: pointer; }
            </style>
        </head>
        <body>
            <div class="card">
                <div class="confetti">🥳</div>
                <img src="https://cdn-icons-png.flaticon.com/512/201/201623.png" class="logo"> <h1>"" + title + ""</h1>
                <p>Félicitations <b>" + details + "</b> !<br>TripLove vous souhaite une magnifique aventure en Tunisie.</p>
                
                <div id="map"></div>
                
                <button class="btn" onclick="window.close()">Prêt pour le départ !</button>
            </div>

            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <script>
                var map = L.map('map', {zoomControl: false}).setView([70.0, 9.5], 100);
                L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);
                L.marker([36.8065, 10.1815]).addTo(map).bindPopup('Départ: Tunis').openPopup();
                L.marker([33.8869, 9.5375]).addTo(map).bindPopup('Explorez la Tunisie 🇹🇳');
            </script>
        </body>
        </html>
    """;

        webView.getEngine().loadContent(htmlContent);

        // Layout du Popup
        VBox root = new VBox(webView);
        root.setStyle("-fx-background-color: transparent; -fx-padding: 10;");
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        // Fermeture propre
        webView.getEngine().setOnStatusChanged(event -> {
            if ("window.close()".equals(webView.getEngine().getLocation())) stage.close();
        });

        stage.show();
    }

    @FXML

    private void backToList() {
        detailScroll.setVisible(false);
        detailScroll.setManaged(false);

        listScroll.setVisible(true);
        listScroll.setManaged(true);
        listScroll.toFront(); // Ensure the list is back on top

        formOverlay.setVisible(false);
    }
}