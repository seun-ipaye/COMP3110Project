import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class XMLLineMappingWriter {

    public static class LineMapping {
        public int oldLine;
        public int newLine;

        public LineMapping(int oldLine, int newLine) {
            this.oldLine = oldLine;
            this.newLine = newLine;
        }
    }

    public static void writeXML(String outputPath, String testName, String javaFileName, List<LineMapping> mappings) throws IOException {
        FileWriter writer = new FileWriter(outputPath);

        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        writer.write("<ROOT>\n");
        writer.write("  <TEST NAME=\"TEST_" + testName + "\" FILE=\"" + javaFileName + "\">\n");

        for (LineMapping mapping : mappings) {
            writer.write("    <MAP>\n");
            writer.write("      <OLD>" + mapping.oldLine + "</OLD>\n");
            writer.write("      <NEW>" + mapping.newLine + "</NEW>\n");
            writer.write("    </MAP>\n");
        }

        writer.write("  </TEST>\n");
        writer.write("</ROOT>\n");

        writer.close();
    }
}
