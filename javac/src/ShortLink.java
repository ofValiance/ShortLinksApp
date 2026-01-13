import java.time.LocalDateTime;
import java.util.UUID;

public class ShortLink {

    private UUID id;
    private String originalUrl;
    private String shortCode;
    private UUID userId;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private final int linkLifeHours = 24;
    private int maxClicks;
    private int currentClicks;
    private boolean isActive;
    private String linkState;

    public ShortLink(String originalUrl, UUID userId, int maxClicks) {
        this.id = UUID.randomUUID();
        this.originalUrl = originalUrl;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusHours(linkLifeHours);
        this.maxClicks = maxClicks;
        this.currentClicks = 0;
        this.isActive = true;
        this.linkState = "Ссылка активна";
    }

    public UUID getId() {
        return id;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public UUID getUserId() {
        return userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public int getMaxClicks() {
        return maxClicks;
    }

    public void incrementClicks() {
        this.currentClicks++;
    }

    public int getCurrentClicks() {
        return currentClicks;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public String getLinkState() {
        return linkState;
    }

    public void setLinkState(String linkState) {
        this.linkState = linkState;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean hasReachedClicksLimit() {
        return currentClicks >= maxClicks;
    }
}
