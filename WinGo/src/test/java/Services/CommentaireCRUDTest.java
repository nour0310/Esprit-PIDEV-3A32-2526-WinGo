package Services;

import Entites.Commentaire;
import Entites.Blog;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommentaireCRUDTest {

    private static CommentaireCRUD commentaireCRUD;
    private static BlogCRUD blogCRUD;
    private static int articleId; // ID de l'article de test, créé une fois

    @BeforeAll
    static void setUp() throws SQLException {
        commentaireCRUD = new CommentaireCRUD();
        blogCRUD = new BlogCRUD();

        // Créer un article de test pour que les commentaires aient une cible
        Blog articleTest = new Blog(
                "Article pour commentaires",
                "Contenu de l'article de test",
                1,
                "test.jpg",
                "Tunis",
                "Test"
        );
        blogCRUD.ajouter(articleTest);

        // Récupérer l'ID de l'article créé
        List<Blog> blogs = blogCRUD.afficher();
        articleId = blogs.stream()
                .filter(b -> "Article pour commentaires".equals(b.getTitre()))
                .findFirst()
                .orElseThrow(() -> new SQLException("Impossible de créer l'article de test"))
                .getId();
    }

    // Pas de @AfterAll ni @AfterEach pour laisser les données

    @Test
    @Order(1)
    void testAjouterCommentaire() throws SQLException {
        Commentaire c = new Commentaire("Super article !", 1, articleId);
        commentaireCRUD.ajouter(c);

        List<Commentaire> commentaires = commentaireCRUD.afficher();
        assertFalse(commentaires.isEmpty());

        boolean trouve = commentaires.stream()
                .anyMatch(com -> "Super article !".equals(com.getContenu()));
        assertTrue(trouve);
    }

    @Test
    @Order(2)
    void testModifierCommentaire() throws SQLException {
        // Créer un commentaire à modifier
        Commentaire c = new Commentaire("Ancien commentaire", 1, articleId);
        commentaireCRUD.ajouter(c);

        List<Commentaire> commentaires = commentaireCRUD.afficher();
        int id = commentaires.stream()
                .filter(com -> "Ancien commentaire".equals(com.getContenu()))
                .findFirst()
                .orElseThrow(() -> new SQLException("Commentaire non trouvé"))
                .getId();

        c.setId(id);
        c.setContenu("Nouveau commentaire");
        commentaireCRUD.modifier(c);

        commentaires = commentaireCRUD.afficher();
        boolean trouve = commentaires.stream()
                .anyMatch(com -> com.getId() == id && "Nouveau commentaire".equals(com.getContenu()));
        assertTrue(trouve, "Le commentaire modifié doit avoir le nouveau contenu");
    }

    @Test
    @Order(3)
    void testSupprimerCommentaire() throws SQLException {
        // Créer un commentaire à supprimer
        Commentaire c = new Commentaire("À supprimer", 1, articleId);
        commentaireCRUD.ajouter(c);

        List<Commentaire> commentaires = commentaireCRUD.afficher();
        int id = commentaires.stream()
                .filter(com -> "À supprimer".equals(com.getContenu()))
                .findFirst()
                .orElseThrow(() -> new SQLException("Commentaire non trouvé"))
                .getId();

        commentaireCRUD.supprimer(id);

        commentaires = commentaireCRUD.afficher();
        boolean existe = commentaires.stream().anyMatch(com -> com.getId() == id);
        assertFalse(existe, "Le commentaire ne doit plus exister après suppression");
    }
}