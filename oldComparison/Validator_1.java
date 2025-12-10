public class Validator1 {

    public boolean isValidEmail(String email) {
        return email.contains("@");
    }

    public boolean isStrongPassword(String password) {
        return password.length() > 8;
    }
}