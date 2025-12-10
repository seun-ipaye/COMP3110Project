public class Inventory_1 {
    private int stock;

    public Inventory_1(int initialStock) {
        this.stock = initialStock;
    }

    public void addStock(int amount) {
        stock += amount;
    }

    public void removeStock(int amount) {
        if (amount <= stock) {
            stock -= amount;
        }
    }
}