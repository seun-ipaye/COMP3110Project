import java.util.ArrayList;
import java.util.List;

public class NotificationService2 {

    private List<String> logs = new ArrayList<>();

    public void deliverEmail(String username, String content) {
        System.out.println("Email delivered to " + username + ": " + content);
        logs.add("Email -> " + username);
    }

    public void deliverSMS(String username, String content) {
        System.out.println("SMS delivered to " + username + ": " + content);
        logs.add("SMS -> " + username);
    }

    public void sendPushNotification(String username, String content) {
        System.out.println("Push sent to " + username + ": " + content);
        logs.add("Push -> " + username);
    }

    public void announce(String msg) {
        System.out.println("ANNOUNCEMENT: " + msg);
    }

    public List<String> getLogs() {
        return logs;
    }
}
