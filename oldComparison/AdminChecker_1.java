public class AdminChecker1 {

    public boolean isAdmin(String role) {
        if (role != null && role.equals("admin")) {
            return true;
        } else {
            return false;
        }
    }
}