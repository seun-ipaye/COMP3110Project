public class LoginController1 {

    private String username;
    private String password;

    public boolean login(String user, String pass) {
        if (user.equals(username) && pass.equals(password)) {
            return true;
        }
        return false;
    }
}