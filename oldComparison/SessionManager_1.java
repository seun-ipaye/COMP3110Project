import java.util.HashMap;
import java.util.Map;

public class SessionManager1 {

    private Map<String, String> sessions = new HashMap<>();

    public void startSession(String userId) {
        sessions.put(userId, "ACTIVE");
    }

    public void endSession(String userId) {
        sessions.remove(userId);
    }

    public boolean isActive(String userId) {
        return sessions.containsKey(userId);
    }
}
