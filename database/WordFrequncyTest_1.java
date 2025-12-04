import java.nio.file.*;

public class WordFrequncyTest {
    public static void main(String[] args) throws Exception {
        String text = Files.readString(Paths.get("input.txt"));
        String[] tokens = text.split("\\s+"); // split by whitespace

        long start = System.currentTimeMillis();
        String result1 = LinkedListCount.mostFrequentWord(tokens);
        long end = System.currentTimeMillis();
        System.out.println("[LinkedList] " + result1);
        System.out.println("Time: " + (end - start) + " ms");

       
    }
}
