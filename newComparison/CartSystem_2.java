import java.util.LinkedList;
import java.util.List;

public class CartSystem2 {

    private List<String> cartItems = new LinkedList<>();

    public void insert(String product) {
        cartItems.add(product);
        log(product + " added.");
    }

    public void delete(String product) {
        cartItems.remove(product);
        log(product + " removed.");
    }

    public void resetCart() {
        cartItems.clear();
        log("Cart has been reset.");
    }

    public List<String> viewCart() {
        return cartItems;
    }

    private void log(String message) {
        System.out.println("[CartLog] " + message);
    }
}
