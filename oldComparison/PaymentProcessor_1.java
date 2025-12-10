public class PaymentProcessor1 {

    private double balance;

    public PaymentProcessor1(double initialBalance) {
        this.balance = initialBalance;
    }

    public boolean charge(double amount) {
        if (amount <= balance) {
            balance -= amount;
            log("Charged: " + amount);
            return true;
        }
        return false;
    }

    public void refund(double amount) {
        balance += amount;
        log("Refunded: " + amount);
    }

    private void log(String message) {
        System.out.println("[LOG] " + message);
    }

    public double getBalance() {
        return balance;
    }
}
