import java.util.HashMap;
import java.util.Map;

public class SessionManager2 {

    private Map<String, String> activeSessions = new HashMap<>();

    public void login(String uid) {
        activeSessions.put(uid, "ACTIVE");
    }

    public void logout(String uid) {
        activeSessions.remove(uid);
    }

    public boolean checkStatus(String uid) {
        return activeSessions.containsKey(uid);
    }
}