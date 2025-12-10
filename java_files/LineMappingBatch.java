import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LineMappingBatch {

    public static void main(String[] args) throws IOException {
        // Define folders - all relative to current directory
        File oldDir = new File("../oldComparison");
        File newDir = new File("../newComparison");
        File outputDir = new File("outputs");
        File testDir = new File("tests");

        // Check if required directories exist
        if (!oldDir.exists()) {
            System.out.println("Error: 'oldComparison' directory does not exist!");
            return;
        }
        
        if (!newDir.exists()) {
            System.out.println("Error: 'newComparison' directory does not exist!");
            return;
        }
        
        if (!outputDir.exists()) {
            System.out.println("Error: 'outputs' directory does not exist!");
            System.out.println("Please create the 'outputs' folder for XML files.");
            return;
        }
        
        if (!testDir.exists()) {
            System.out.println("Error: 'tests' directory does not exist!");
            System.out.println("Please create the 'tests' folder for .out files.");
            return;
        }

        // Get all files ending in 1.java from old/
        File[] oldFiles = oldDir.listFiles((dir, name) -> name.endsWith("_1.java"));

        if (oldFiles == null || oldFiles.length == 0) {
            System.out.println("No files found in oldComparison folder.");
            return;
        }

        int count = 0;

        // Loop through each old file
        for (File oldFile : oldFiles) {
            String baseName = oldFile.getName().replace("_1.java", "");
            File newFile = new File(newDir, baseName + "_2.java");

            if (newFile.exists()) {
                // .out files go to tests folder
                File outFile = new File(testDir, baseName + ".out");
                System.out.println("Comparing: " + oldFile.getName() + " ↔ " + newFile.getName());

                // Capture result from compareFiles
                MatchedResult result = LineMappingTool.compareFiles(oldFile, newFile, outFile);
                count++;

                // Optional: Generate full XML mapping
                String javaFileName = baseName + ".java";
                String xmlOutputPath = new File(outputDir, baseName + "_full.xml").getPath();

                // Convert matched lines into XML-friendly structure
                List<XMLLineMappingWriter.LineMapping> xmlMappings = new ArrayList<>();
                for (MatchedLine m : result.matchedLines) {
                    xmlMappings.add(new XMLLineMappingWriter.LineMapping(m.oldLineNumber, m.newLineNumber));
                }

                // Write XML file with full mappings (optional)
                XMLLineMappingWriter.writeXML(xmlOutputPath, baseName, javaFileName, xmlMappings);

            } else {
                System.out.println("Skipping: No match for " + oldFile.getName() + " in newComparison folder.");
            }
        }

        System.out.println("\nBatch Completed: " + count + " file pair(s) mapped.");
        System.out.println(".out files saved to: " + testDir.getPath());
    }
}