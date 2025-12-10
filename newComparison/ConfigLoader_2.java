import java.io.*;
import java.util.Properties;

public class ConfigLoader2 {

    public Properties getConfiguration(String path) throws IOException {
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(path)) {
            p.load(in);
        }
        return p;
    }
}