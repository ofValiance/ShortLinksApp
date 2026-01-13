import java.util.Scanner;
import java.util.UUID;

public class ShortLinksApp {

    private static ShortLinkManager manager = new ShortLinkManager();
    private static UUID curUserId;
    private static Scanner scanner = new Scanner(System.in);
    private static boolean running = true;

    static void main() {
        System.out.println("\n=== СЕРВИС СОКРАЩЕНИЯ ССЫЛОК ===");

        startCleanupTask();
        initUser();

        while (running) {
            printMenu();
            processCommand();
        }
    }

    public static void initUser() {
        System.out.println("\n=== ВХОД ===");
        System.out.println("1. Создать нового пользователя");
        System.out.println("2. Ввести существующий ID пользователя");
        System.out.println("3. Выход");
        System.out.print("\nВыберите вариант: ");

        curUserId = null;
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                curUserId = manager.createUser();
                break;
            case "2":
                System.out.print("\nВведите ваш ID: ");
                try {
                    String inputId = scanner.nextLine().trim();
                    UUID userId = UUID.fromString(inputId);
                    if (manager.getUser(userId) == null) {
                        System.out.println("\nПользователь не найден");
                        initUser();
                    } else {
                        curUserId = userId;
                        System.out.println("\nВы вошли под ID " + userId);
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("\nНеверный формат ID");
                    initUser();
                }
                break;
            case "3":
                running = false;
                break;
            default:
                System.out.println("\nНеверная команда");
                initUser();
        }
    }

    private static void printMenu() {
        System.out.println("\n=== МЕНЮ ===");
        System.out.println("Текущий пользователь: " + curUserId);
        System.out.println("1. Создать короткую ссылку");
        System.out.println("2. Перейти по короткой ссылке");
        System.out.println("3. Показать мои ссылки");
        System.out.println("4. Удалить ссылку");
        System.out.println("5. Сменить пользователя");
        System.out.println("6. Выход");
        System.out.print("\nВыберите действие: ");
    }

    private static void processCommand() {
        String command = scanner.nextLine().trim();

        switch (command) {
            case "1":
                createShortLink();
                break;
            case "2":
                redirectToLink();
                break;
            case "3":
                listLinks();
                break;
            case "4":
                deleteLink();
                break;
            case "5":
                initUser();
                break;
            case "6":
                running = false;
                break;
            default:
                System.out.println("\nНеверная команда");
        }
    }

    private static void createShortLink() {
        System.out.print("\nВведите URL для сокращения (удостоверьтесь в правильности): ");
        String url = scanner.nextLine().trim();

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        System.out.print("\nВведите лимит переходов: ");
        try {
            int maxClicks = Integer.parseInt(scanner.nextLine().trim());
            if (maxClicks <= 0) {
                System.out.println("\nЛимит должен быть положительным числом");
                return;
            }
            manager.createShortLink(url, curUserId, maxClicks);
        } catch (NumberFormatException e) {
            System.out.println("\nНеверный формат числа");
        }
    }

    private static void redirectToLink() {
        System.out.print("\nВведите короткий код (без " + ShortLinkManager.BASE_URL + "): ");
        String shortCode = scanner.nextLine().trim();

        String result = manager.redirect(shortCode);
        System.out.println(result);
    }

    private static void listLinks() {
        manager.listUserLinks(curUserId);
    }

    private static void deleteLink() {
        System.out.print("\nВведите короткий код для удаления: ");
        String shortCode = scanner.nextLine().trim();

        boolean success = manager.deleteLink(curUserId, shortCode);
        if (!success) {
            System.out.println("\nСсылка не найдена или у вас нет прав на ее удаление");
        } else {
            System.out.println("\nСсылка удалена");
        }
    }

    private static void startCleanupTask() {
        Thread cleanupThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(30000);
                    manager.cleanupExpiredLinks();
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }
}

