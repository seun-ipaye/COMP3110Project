public class BookStore_1 {
    private String name;
    private String location;

    public BookStore_1(String name, String location) {
        this.name = name;
        this.location = location;
    }

    public void displayInfo() {
        System.out.println("Bookstore: " + name);
        System.out.println("Location: " + location);
    }
}
