public class DiscountCalculator1 {

    public double applyDiscount(double price, double percent) {
        return price - (price * percent / 100);
    }

    public boolean isEligible(double total) {
        return total > 100;
    }
}
