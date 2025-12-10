public class BookStore1 {
    private String name;
    private String location;

    public BookStore1(String name, String location) {
        this.name = name;
        this.location = location;
    }

    public void displayInfo() {
        System.out.println("Bookstore: " + name);
        System.out.println("Location: " + location);
    }
}
