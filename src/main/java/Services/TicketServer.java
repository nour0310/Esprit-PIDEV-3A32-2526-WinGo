package Services;

import Entites.Event;
import Entites.Participation;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * TicketServer — lightweight embedded HTTP server that serves digital ticket
 * pages when a QR code is scanned.
 *
 * Usage:
 *   TicketServer.start();                            // call once at app startup
 *   String url = TicketServer.registerTicket(p, e); // returns e.g. http://localhost:8765/ticket?id=3
 */
public class TicketServer {

    private static final int PORT = 8765;
    private static HttpServer server;

    // Maps ticketKey → HTML page content
    private static final Map<String, String> ticketPages = new ConcurrentHashMap<>();

    // ── Lifecycle ─────────────────────────────────────────────────────

    /** Starts the server (idempotent — safe to call multiple times). */
    public static synchronized void start() {
        if (server != null) return;
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
            server.createContext("/ticket", TicketServer::handleTicket);
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            System.out.println("TicketServer started on http://localhost:" + PORT);
        } catch (IOException e) {
            System.err.println("TicketServer could not start: " + e.getMessage());
        }
    }

    /** Stops the server gracefully. */
    public static synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    // ── Registration ─────────────────────────────────────────────────

    /**
     * Registers a participation + event pair and returns the URL that the
     * generated QR code should encode.
     *
     * @param p  Participation entity
     * @param e  Event entity (may be null)
     * @return   Full URL string, e.g. "http://localhost:8765/ticket?id=7"
     */
    public static String registerTicket(Participation p, Event e) {
        if (p == null) return "http://localhost:" + PORT + "/ticket?id=0";

        String key = String.valueOf(p.getIdParticipation());
        String html = buildTicketHtml(p, e);
        ticketPages.put(key, html);

        return "http://localhost:" + PORT + "/ticket?id=" + key;
    }

    // ── HTTP handler ──────────────────────────────────────────────────

    private static void handleTicket(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery(); // "id=7"
        String id = "";
        if (query != null && query.startsWith("id=")) {
            id = query.substring(3);
        }

        String html = ticketPages.getOrDefault(id, errorPage(id));
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // ── HTML builders ─────────────────────────────────────────────────

    private static String buildTicketHtml(Participation p, Event e) {
        String eventTitle    = e != null ? htmlSafe(e.getTitle())    : "—";
        String eventLocation = e != null ? htmlSafe(e.getLocation()) : "—";
        String eventDate     = e != null && e.getDateEvent() != null
                ? e.getDateEvent().toString() : "—";
        String eventTime     = e != null && e.getStartTime() != null
                ? e.getStartTime().toString().substring(0, 5) : "—";
        double price         = e != null ? e.getPrice() : 0.0;
        double total         = price * p.getNombrePlaces();

        String fullName = htmlSafe(p.getPrenomParticipant()) + " " + htmlSafe(p.getNomParticipant());
        String ticketId = "WGO-" + p.getIdParticipation()
                + (e != null ? "-" + e.getIdEvent() : "");

        return "<!DOCTYPE html><html lang='fr'><head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1'>"
                + "<title>Ticket — " + eventTitle + "</title>"
                + "<style>"
                + "body{margin:0;padding:0;background:#f0f2f5;font-family:Arial,sans-serif}"
                + ".card{max-width:520px;margin:40px auto;background:#fff;border-radius:16px;"
                + "box-shadow:0 8px 32px rgba(0,0,0,0.12);overflow:hidden}"
                + ".header{background:linear-gradient(135deg,#1a3c5e,#2d6a9f);padding:30px;"
                + "text-align:center;color:#fff}"
                + ".header h1{margin:0 0 4px;font-size:28px;letter-spacing:2px}"
                + ".header p{margin:0;opacity:.75;font-size:13px;letter-spacing:1px}"
                + ".body{padding:30px}"
                + ".row{display:flex;justify-content:space-between;padding:10px 0;"
                + "border-bottom:1px solid #eee;font-size:14px}"
                + ".row .label{color:#888;font-weight:600}"
                + ".row .value{color:#1a1a1a;font-weight:700}"
                + ".total{text-align:right;padding:20px 0 0;font-size:22px;"
                + "font-weight:900;color:#1a3c5e}"
                + ".badge{display:inline-block;background:#e8f5e9;color:#2e7d32;"
                + "border-radius:30px;padding:4px 14px;font-size:12px;font-weight:700}"
                + ".footer{background:#1a3c5e;padding:16px;text-align:center;"
                + "color:rgba(255,255,255,.6);font-size:12px;letter-spacing:1px}"
                + "</style></head><body>"
                + "<div class='card'>"
                + "<div class='header'>"
                + "<h1>WinGO Events</h1>"
                + "<p>DIGITAL TICKET</p>"
                + "<p style='margin-top:8px;font-size:18px;font-weight:700;opacity:.95'>"
                + ticketId + "</p>"
                + "</div>"
                + "<div class='body'>"
                + row("Event", eventTitle)
                + row("Date", eventDate)
                + row("Time", eventTime)
                + row("Location", eventLocation)
                + row("Participant", fullName)
                + row("Seats", String.valueOf(p.getNombrePlaces()))
                + row("Price / seat", String.format("%.2f DT", price))
                + row("Status", "<span class='badge'>" + htmlSafe(p.getStatut()) + "</span>")
                + "<div class='total'>Total: " + String.format("%.2f DT", total) + "</div>"
                + "</div>"
                + "<div class='footer'>Present this ticket at the event entrance &mdash; WinGO</div>"
                + "</div></body></html>";
    }

    private static String row(String label, String value) {
        return "<div class='row'><span class='label'>" + htmlSafe(label)
                + "</span><span class='value'>" + value + "</span></div>";
    }

    private static String errorPage(String id) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'>"
                + "<title>Ticket Not Found</title></head>"
                + "<body style='font-family:Arial;text-align:center;padding:60px'>"
                + "<h2 style='color:#c0392b'>Ticket Not Found</h2>"
                + "<p>No ticket found for ID: " + htmlSafe(id) + "</p>"
                + "</body></html>";
    }

    private static String htmlSafe(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}