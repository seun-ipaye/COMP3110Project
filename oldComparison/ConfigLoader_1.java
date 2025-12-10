import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader1 {

    public Properties loadConfig(String filePath) throws IOException {
        Properties props = new Properties();
        FileInputStream fis = new FileInputStream(filePath);
        props.load(fis);
        fis.close();
        return props;
    }
}