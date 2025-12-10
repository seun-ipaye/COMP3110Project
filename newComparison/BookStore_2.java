public class BookStore2 {
    private String name;
    private String city;

    public BookStore2(String name, String city) {
        this.name = name;
        this.city = city;
    }

    public void displayInfo() {
        System.out.println("Bookstore Name: " + name);
        System.out.println("City: " + city);
    }

    public void open() {
        System.out.println("The bookstore is now open.");
    }
}
