import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileReaderUtil1 {

    public void readFile(String path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
        br.close();
    }
}