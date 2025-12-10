public class MathUtils1 {

    public int square(int x) {
        return x * x;
    }

    public int cube(int x) {
        return x * x * x;
    }

    public int factorial(int x) {
        if (x <= 1) return 1;
        return x * factorial(x - 1);
    }
}