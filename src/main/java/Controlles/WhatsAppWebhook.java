package Controlles;

import Services.WhatsAppService;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class WhatsAppWebhook {

    private static WhatsAppService whatsAppService = new WhatsAppService();
    private static HttpServer server;
    private static int activePort = -1;

    public static void startServer() throws IOException {
        int port = 8080;

        // ✅ Arrêter proprement l'ancienne instance
        if (server != null) {
            server.stop(0);
            server = null;
            try { Thread.sleep(300); } catch (Exception ignored) {}
            System.out.println("🔄 Ancien serveur arrêté");
        }

        while (port < 9000) {  // plage élargie 8080-8999
            try {
                server = HttpServer.create(new InetSocketAddress(port), 0);
                server.createContext("/webhook/whatsapp", new WhatsAppHandler());
                server.setExecutor(null);
                server.start();
                activePort = port;

                System.out.println("\n╔══════════════════════════════════════════╗");
                System.out.println("║  ✅ Serveur WhatsApp démarré           ║");
                System.out.println("║  Port: " + port + "                               ║");
                System.out.println("║  URL: http://localhost:" + port + "/webhook/whatsapp ║");
                System.out.println("╠══════════════════════════════════════════╣");
                System.out.println("║  Pour Twilio:                           ║");
                System.out.println("║  1. ngrok http " + port + "                    ║");
                System.out.println("║  2. Copier l'URL https://xxxx.ngrok.io  ║");
                System.out.println("║  3. Configurer dans Twilio Console      ║");
                System.out.println("╚══════════════════════════════════════════╝");
                return;

            } catch (Exception e) {
                System.out.println("⚠ Port " + port + " occupé, essai port suivant");
                port++;
            }
        }

        System.err.println("❌ Aucun port libre trouvé");
    }

    public static void stopServer() {
        if (server != null) {
            server.stop(0);
            server = null;
            activePort = -1;
            System.out.println("✅ Serveur WhatsApp arrêté — port libéré");
        }
    }

    static class WhatsAppHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            if ("GET".equals(method)) {
                String response = "✅ Webhook WinGo WhatsApp actif";
                sendResponse(exchange, 200, response);
                return;
            }

            if ("POST".equals(method)) {
                String body = readBody(exchange);
                Map<String, String> params = parseParams(body);

                String from = params.get("From");
                String msgBody = params.get("Body");

                if (from != null && msgBody != null) {
                    String reponse = whatsAppService.recevoirMessage(from, msgBody);
                    String xmlResponse = buildTwiML(reponse);
                    exchange.getResponseHeaders().set("Content-Type", "text/xml; charset=UTF-8");
                    sendResponse(exchange, 200, xmlResponse);
                } else {
                    sendResponse(exchange, 200, buildTwiML("Bienvenue sur WinGo! Tapez MENU"));
                }
                return;
            }

            sendResponse(exchange, 405, "Method Not Allowed");
        }

        private String buildTwiML(String message) {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<Response><Message>" + escapeXml(message) + "</Message></Response>";
        }

        private String escapeXml(String s) {
            return s.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;");
        }

        private String readBody(HttpExchange exchange) throws IOException {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                return sb.toString();
            }
        }

        private void sendResponse(HttpExchange exchange, int code, String body) throws IOException {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(code, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private Map<String, String> parseParams(String query) {
            Map<String, String> params = new HashMap<>();
            if (query == null || query.isEmpty()) return params;
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    try {
                        params.put(URLDecoder.decode(kv[0], "UTF-8"), URLDecoder.decode(kv[1], "UTF-8"));
                    } catch (UnsupportedEncodingException ignored) {}
                }
            }
            return params;
        }
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(WhatsAppWebhook::stopServer));

        try {
            startServer();
            System.out.println("\n📻 En attente de messages... Ctrl+C pour arrêter\n");
            Thread.currentThread().join();
        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
        }
    }
}