public class AlertLogger1 {

    public void logAlert(String message, boolean isCritical) {
        if (isCritical) {
            System.out.println("CRITICAL ALERT: " + message.toUpperCase());
        } else {
            System.out.println("Alert: " + message);
        }
    }
}