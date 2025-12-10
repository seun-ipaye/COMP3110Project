public class Inventory_2 {
    private int stock;

    public Inventory_2(int initialStock) {
        this.stock = initialStock;
    }

    public void updateStock(int amount) {
        stock += amount;
    }

    public boolean hasStock() {
        return stock > 0;
    }
}