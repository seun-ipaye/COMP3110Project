public class Inventory2 {
    private int stock;

    public Inventory2(int initialStock) {
        this.stock = initialStock;
    }

    public void updateStock(int amount) {
        stock += amount;
    }

    public boolean hasStock() {
        return stock > 0;
    }
}