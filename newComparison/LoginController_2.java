public class LoginController2 {

    private String storedUser;
    private String storedPass;

    public boolean authenticate(String u, String p) {
        return u.equals(storedUser) && p.equals(storedPass);
    }
}