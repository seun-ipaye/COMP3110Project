public class NotificationService1 {

    public void sendEmail(String user, String msg) {
        System.out.println("EMAIL to " + user + ": " + msg);
    }

    public void sendSMS(String user, String msg) {
        System.out.println("SMS to " + user + ": " + msg);
    }

    public void sendPush(String user, String msg) {
        System.out.println("PUSH to " + user + ": " + msg);
    }

    public void broadcast(String message) {
        System.out.println("Broadcasting: " + message);
    }
}