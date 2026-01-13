import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {

    private UUID id;
    private List<ShortLink> shortLinks;

    public User() {
        this.id = UUID.randomUUID();
        this.shortLinks = new ArrayList<>();
    }

    public List<ShortLink> getShortLinks() {
        return shortLinks;
    }

    public UUID getId() {
        return id;
    }

    public void addShortLink(ShortLink link) {
        this.shortLinks.add(link);
    }

    public void removeShortLink(String shortCode) {
        this.shortLinks.removeIf(link -> link.getShortCode().equals(shortCode));
    }

    public ShortLink findLinkByCode(String shortCode) {
        return shortLinks.stream()
                .filter(link -> link.getShortCode().equals(shortCode))
                .findFirst()
                .orElse(null);
    }

}
