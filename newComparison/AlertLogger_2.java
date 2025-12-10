public class AlertLogger_2 {

    public void sendAlert(String alertMessage, boolean criticalFlag) {
        String prefix = criticalFlag ? "[!!]" : "[~]";
        System.out.println(prefix + " " + alertMessage.toUpperCase());
    }
}