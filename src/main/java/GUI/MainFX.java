package GUI;

import Controlles.WhatsAppWebhook;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage stage) {
        try {
            System.out.println("🚀 Démarrage WinGo...");

            // ✅ Tuer toute instance précédente avant de démarrer
            WhatsAppWebhook.stopServer();

            // Démarrer le serveur WhatsApp dans un thread séparé
            Thread whatsappThread = new Thread(() -> {
                try {
                    // Petite pause pour laisser le port se libérer
                    Thread.sleep(500);
                    WhatsAppWebhook.startServer();
                } catch (Exception e) {
                    System.err.println("⚠ Serveur WhatsApp non démarré: " + e.getMessage());
                }
            });
            whatsappThread.setDaemon(true); // S'arrête automatiquement avec l'app
            whatsappThread.start();

            // ✅ OUVRIR LE LOGIN EN PREMIER
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Scene scene = new Scene(loader.load(), 480, 580);

            stage.setTitle("WinGo - Connexion");
            stage.setScene(scene);

            stage.setResizable(false);
            stage.show();

            // Arrêter le serveur WhatsApp à la fermeture
            stage.setOnCloseRequest(e -> {
                WhatsAppWebhook.stopServer();
                Platform.exit();
            });

            System.out.println("✅ Application démarrée !");

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}