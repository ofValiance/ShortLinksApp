import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ShortLinkManager {

    public static final String BASE_URL = "clck.ru/";
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SHORT_CODE_LENGTH = 6;

    private Map<String, ShortLink> linksByCode = new HashMap<>();
    private Map<UUID, User> usersById = new HashMap<>();
    private Random random = new Random();

    public UUID createUser() {
        User newUser = new User();
        usersById.put(newUser.getId(), newUser);
        System.out.println("\nСоздан новый пользователь с ID: " + newUser.getId() + ". Запомните его!");
        return newUser.getId();
    }

    public User getUser(UUID userId) {
        return usersById.get(userId);
    }

    private String generateShortCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    public String createShortLink(String originalUrl, UUID userId, int maxClicks) {
        User user = usersById.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("\nПользователь не найден");
        }

        ShortLink newShortLink = new ShortLink(originalUrl, userId, maxClicks);

        String shortCode;
        do {
            shortCode = generateShortCode();
        } while (linksByCode.containsKey(shortCode));

        newShortLink.setShortCode(shortCode);
        linksByCode.put(shortCode, newShortLink);
        user.addShortLink(newShortLink);

        System.out.println("\n=== СОЗДАНА НОВАЯ ССЫЛКА ===");
        System.out.println("Оригинальная ссылка: " + originalUrl);
        System.out.println("Короткая ссылка: " + BASE_URL + shortCode);
        System.out.println("Лимит переходов: " + maxClicks);
        System.out.println("Действительна до: " +
                newShortLink.getExpiresAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")));

        return BASE_URL + shortCode;
    }

    public boolean deleteLink(UUID userId, String shortCode) {
        User user = usersById.get(userId);
        if (user == null) return false;

        ShortLink link = user.findLinkByCode(shortCode);
        if (link == null) return false;

        linksByCode.remove(shortCode);
        user.removeShortLink(shortCode);
        return true;
    }

    public boolean openInBrowser(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String checkLinkState(String shortCode) {
        ShortLink link = linksByCode.get(shortCode);

        if (link == null) {
            return "Ссылка не найдена или была удалена";
        }

        String state = link.getLinkState();

        if (link.hasReachedClicksLimit()) {
            link.setActive(false);
            state = "Лимит переходов исчерпан";
        }

        if (link.isExpired()) {
            link.setActive(false);
            state = "Срок действия ссылки истек";
        }

        link.setLinkState(state);
        return state;
    }

    public String redirect(String shortCode) {
        ShortLink link = linksByCode.get(shortCode);
        String state = checkLinkState(shortCode);

        if (link == null) {
            return "\nСсылка не найдена или была удалена";
        }

        if (!link.isActive()) {
            return "\nНевозможно открыть ссылку: " + state;
        }

        if (!openInBrowser(link.getOriginalUrl())) {
            return "\nОшибка: не удалось открыть браузер";
        };

        link.incrementClicks();

        System.out.println("\nПеренаправление на: " + link.getOriginalUrl());
        System.out.println("Переходов: " + link.getCurrentClicks() + "/" + link.getMaxClicks());

        if (!checkLinkState(shortCode).equals(state)) {
            System.out.println("Состояние ссылки обновлено: " + checkLinkState(shortCode));
        }

        return "\n=== ССЫЛКА ОТКРЫТА ===";
    }

    public void listUserLinks(UUID userId) {
        User user = usersById.get(userId);
        if (user == null) {
            System.out.println("\nПользователь не найден");
            return;
        }

        List<ShortLink> links = user.getShortLinks();
        if (links.isEmpty()) {
            System.out.println("\nНет созданных ссылок");
            return;
        }

        System.out.println("\nCсылки:");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        for (ShortLink link : links) {
            String status = checkLinkState(link.getShortCode());

            System.out.println("\n=== " + link.getId() + " ===");
            System.out.println("Статус: " + status);
            System.out.println("Короткая ссылка: " + BASE_URL + link.getShortCode());
            System.out.println("Оригинал: " + link.getOriginalUrl());
            System.out.println("Переходы: " + link.getCurrentClicks() + "/" + link.getMaxClicks());
            System.out.println("Создана: " + link.getCreatedAt().format(formatter));
            System.out.println("Активна до: " + link.getExpiresAt().format(formatter));
        }
    }

    public void cleanupExpiredLinks() {
        LocalDateTime now = LocalDateTime.now();
        boolean isDeleted = false;

        for (Map.Entry<String, ShortLink> entry : linksByCode.entrySet()) {
            ShortLink link = entry.getValue();

            if (link.isExpired() && now.isAfter(link.getExpiresAt().plusMinutes(30))) {
                User user = usersById.get(link.getUserId());
                UUID userId;
                String shortCode;

                if (user != null) {
                    userId = usersById.get(link.getUserId()).getId();
                    shortCode = link.getShortCode();

                    isDeleted = deleteLink(userId, shortCode);

                    if (isDeleted) {
                        System.out.println("\n=== УВЕДОМЛЕНИЕ ===");
                        System.out.println("Ссылка " + BASE_URL + shortCode + " автоматически удалена (истек срок действия)");
                    }
                }
            }
        }
    }
}
