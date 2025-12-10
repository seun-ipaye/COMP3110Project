import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LineMappingBatch {

    public static void main(String[] args) throws IOException {
        // Define folders
        File oldDir = new File("../oldComparison");
        File newDir = new File("../newComparison");
        File outputDir = new File("outputs");

        // Create outputs directory if it doesn't exist
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Get all files ending in 1.java from old/
        File[] oldFiles = oldDir.listFiles((dir, name) -> name.endsWith("_1.java"));

        if (oldFiles == null || oldFiles.length == 0) {
            System.out.println("No files found in old/ folder.");
            return;
        }

        int count = 0;

        // Loop through each old file
        for (File oldFile : oldFiles) {
            String baseName = oldFile.getName().replace("_1.java", "");
            File newFile = new File(newDir, baseName + "_2.java");

            if (newFile.exists()) {
                File outputFile = new File(outputDir, baseName + ".out");
                System.out.println("Comparing: " + oldFile.getName() + " ↔ " + newFile.getName());

                // Modified: capture result from compareFiles
                MatchedResult result = LineMappingTool.compareFiles(oldFile, newFile, outputFile);
                count++;

                // Prepare XML output
                String javaFileName = baseName + ".java";
                String xmlOutputPath = new File(outputDir, baseName + ".xml").getPath();

                // Convert matched lines into XML-friendly structure
                List<XMLLineMappingWriter.LineMapping> xmlMappings = new ArrayList<>();
                for (MatchedLine m : result.matchedLines) {
                    xmlMappings.add(new XMLLineMappingWriter.LineMapping(m.oldLineNumber, m.newLineNumber));
                }

                // Write XML file
                XMLLineMappingWriter.writeXML(xmlOutputPath, baseName, javaFileName, xmlMappings);

            } else {
                System.out.println("Skipping: No match for " + oldFile.getName() + " in new/ folder.");
            }
        }

        System.out.println("\nBatch Completed: " + count + " file pair(s) mapped.");
    }
}
