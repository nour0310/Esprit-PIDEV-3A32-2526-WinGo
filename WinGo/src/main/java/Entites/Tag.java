// Entité Tag.java
public class Tag {
    private int id;
    private String nom;
    // getters/setters
}

// Dans Blog.java, ajouter
private List<Tag> tags = new ArrayList<>();

// Dans BlogController, ajouter un champ pour les tags
@FXML private TextField tagsField;

// Méthode pour ajouter des tags à un article
private void ajouterTags(Blog blog, String tagsString) {
    String[] tagNames = tagsString.split(",");
    for (String tagName : tagNames) {
        tagName = tagName.trim();
        if (!tagName.isEmpty()) {
            Tag tag = tagCRUD.findOrCreateByName(tagName);
            tagCRUD.associerTagArticle(blog.getId(), tag.getId());
        }
    }
}