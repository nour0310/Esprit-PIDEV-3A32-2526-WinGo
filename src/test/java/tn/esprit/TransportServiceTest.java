package tn.esprit;

import Entites.Transport;
import Services.TransportCRUD;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TransportServiceTest {

    private static TransportCRUD transportCRUD;
    private static int transportId;

    @BeforeAll
    static void setUp() {
        transportCRUD = new TransportCRUD();
    }

    // -------------------- AJOUT --------------------
    @Test
    @Order(1)
    void testAjouterTransport() throws SQLException {

        Transport transport = new Transport(
                "Bus",
                "50",
                25.5f,
                "Tunis",
                "Sfax",
                LocalDateTime.now()
        );

        transportCRUD.ajouter(transport);

        List<Transport> transports = transportCRUD.afficher();
        assertFalse(transports.isEmpty());

        transportId = transports.stream()
                .filter(t -> "Bus".equals(t.getType()))
                .findFirst()
                .orElseThrow(() -> new SQLException("Transport non trouvé"))
                .getId();

        assertTrue(transportId > 0);
    }

    // -------------------- MODIFICATION --------------------
    @Test
    @Order(2)
    void testModifierTransport() throws SQLException {

        Transport transportModif = new Transport();
        transportModif.setId(transportId);
        transportModif.setType("Train");
        transportModif.setCapacite("200");
        transportModif.setTarif(40.0f);
        transportModif.setDepart("Sousse");
        transportModif.setArrivee("Gabes");
        transportModif.setDateDepart(LocalDateTime.now());

        transportCRUD.modifier(transportModif);

        List<Transport> transports = transportCRUD.afficher();

        boolean trouve = transports.stream()
                .anyMatch(t ->
                        t.getId() == transportId &&
                                "Train".equals(t.getType())
                );

        assertTrue(trouve);
    }

    // -------------------- SUPPRESSION --------------------
    @Test
    @Order(3)
    void testSupprimerTransport() throws SQLException {

        transportCRUD.supprimer(transportId);

        List<Transport> transports = transportCRUD.afficher();

        boolean existe = transports.stream()
                .anyMatch(t -> t.getId() == transportId);

        assertFalse(existe);
    }
}

