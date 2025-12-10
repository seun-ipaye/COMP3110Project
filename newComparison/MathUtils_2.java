public class MathUtils2 {

    public int powerOf2(int a) {
        return a * a;
    }

    public int powerOf3(int b) {
        return b * b * b;
    }

    public int computeFactorial(int n) {
        return (n <= 1) ? 1 : n * computeFactorial(n - 1);
    }
}