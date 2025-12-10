public class DiscountCalculator2 {

    public double calculateDiscount(double amount, double rate) {
        return amount * (1 - rate / 100);
    }

    public boolean eligibleForDiscount(double value) {
        return value >= 101;
    }
}