package Services;

import Entites.Blog;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BlogCRUDTest {

    private static BlogCRUD blogCRUD;

    @BeforeAll
    static void setUp() {
        blogCRUD = new BlogCRUD();
    }

    @Test
    @Order(1)
    void testAjouterBlog() throws SQLException {
        Blog blog = new Blog("Titre Test", "Contenu de test", 1, "test.jpg", "Tunis", "Culture");
        blogCRUD.ajouter(blog);

        List<Blog> blogs = blogCRUD.afficher();
        assertFalse(blogs.isEmpty());

        boolean trouve = blogs.stream().anyMatch(b -> "Titre Test".equals(b.getTitre()));
        assertTrue(trouve);
    }

    @Test
    @Order(2)
    void testModifierBlog() throws SQLException {
        // Créer un article à modifier
        Blog blog = new Blog("Ancien Titre", "Ancien contenu", 1, "image.jpg", "Tunis", "Culture");
        blogCRUD.ajouter(blog);

        // Récupérer l'ID de l'article créé
        List<Blog> blogs = blogCRUD.afficher();
        int id = blogs.stream()
                .filter(b -> "Ancien Titre".equals(b.getTitre()))
                .findFirst()
                .orElseThrow(() -> new SQLException("Article non trouvé"))
                .getId();

        // Modifier l'article
        Blog blogModif = new Blog();
        blogModif.setId(id);
        blogModif.setTitre("Nouveau Titre");
        blogModif.setContenu("Nouveau contenu");
        blogModif.setAuteur(1);
        blogModif.setImage("nouvelle.jpg");
        blogModif.setRegion("Sfax");
        blogModif.setCategorie("Plage");

        blogCRUD.modifier(blogModif);

        // Vérifier la modification
        blogs = blogCRUD.afficher();
        boolean trouve = blogs.stream()
                .anyMatch(b -> b.getId() == id && "Nouveau Titre".equals(b.getTitre()));
        assertTrue(trouve, "L'article modifié doit avoir le nouveau titre");
    }
}