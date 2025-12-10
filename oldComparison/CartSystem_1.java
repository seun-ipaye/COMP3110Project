import java.util.ArrayList;
import java.util.List;

public class CartSystem1 {

    private List<String> items = new ArrayList<>();

    public void addItem(String item) {
        items.add(item);
        System.out.println(item + " added to cart.");
    }

    public void removeItem(String item) {
        items.remove(item);
        System.out.println(item + " removed from cart.");
    }

    public void clearCart() {
        items.clear();
        System.out.println("Cart cleared.");
    }

    public List<String> getItems() {
        return items;
    }
}
