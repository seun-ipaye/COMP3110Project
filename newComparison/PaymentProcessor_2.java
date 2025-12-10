import java.time.LocalDateTime;

public class PaymentProcessor2 {

    private double currentBalance;

    public PaymentProcessor2(double initialBalance) {
        this.currentBalance = initialBalance;
    }

    public boolean processPayment(double amount) {
        if (amount > currentBalance) {
            audit("Payment failed for: " + amount);
            return false;
        }

        currentBalance -= amount;
        audit("Payment processed: " + amount);
        return true;
    }

    public void issueRefund(double funds) {
        currentBalance += funds;
        audit("Refund issued: " + funds);
    }

    private void audit(String msg) {
        System.out.println(LocalDateTime.now() + " :: " + msg);
    }

    public double viewBalance() {
        return currentBalance;
    }
}
