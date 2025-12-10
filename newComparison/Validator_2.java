public class Validator2 {

    public boolean validateEmail(String input) {
        return input.indexOf('@') != -1;
    }

    public boolean checkPasswordStrength(String pwd) {
        return pwd.length() > 8;
    }
}