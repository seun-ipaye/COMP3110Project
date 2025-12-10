import java.util.ArrayList;
import java.util.List;

public class UserManager1 {
    private List<String> users;

    public UserManager1() {
        users = new ArrayList<>();
    }

    public void addUser(String username) {
        if (username != null && !username.trim().isEmpty()) {
            users.add(username.trim());
        }
    }

    public boolean removeUser(String username) {
        return users.remove(username);
    }

    public boolean userExists(String username) {
        return users.contains(username);
    }

    public List<String> getAllUsers() {
        return new ArrayList<>(users);
    }

    public void printUsers() {
        for (String user : users) {
            System.out.println(user);
        }
    }
}
