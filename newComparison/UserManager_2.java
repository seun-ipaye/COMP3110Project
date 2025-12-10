import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserManager2 {
    private List<String> users;

    public UserManager2() {
        users = new ArrayList<>();
    }

    public void addUser(String username) {
        if (isValid(username)) {
            users.add(username.trim());
        }
    }

    public boolean removeUser(String username) {
        return users.remove(username);
    }

    public boolean userExists(String username) {
        return users.contains(username);
    }

    public List<String> getSortedUsers() {
        List<String> copy = new ArrayList<>(users);
        Collections.sort(copy);
        return copy;
    }

    public void printUsers() {
        for (String user : users) {
            System.out.println("User: " + user);
        }
    }

    private boolean isValid(String username) {
        return username != null && !username.trim().isEmpty();
    }
}
