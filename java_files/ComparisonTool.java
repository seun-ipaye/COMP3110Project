import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class ComparisonTool {
    
    // Class to store manual mappings from XML
    static class ManualMapping {
        int oldLine;
        int newLine;
        
        ManualMapping(int oldLine, int newLine) {
            this.oldLine = oldLine;
            this.newLine = newLine;
        }
        
        @Override
        public String toString() {
            return oldLine + " → " + (newLine == -1 ? "DELETED" : newLine);
        }
    }
    
    // Class to store tool mappings from .out file
    static class ToolMapping {
        int oldLine;
        int newLine;
        boolean isRenamed;
        boolean isDeleted;
        
        ToolMapping(int oldLine, int newLine, boolean isRenamed, boolean isDeleted) {
            this.oldLine = oldLine;
            this.newLine = newLine;
            this.isRenamed = isRenamed;
            this.isDeleted = isDeleted;
        }
        
        @Override
        public String toString() {
            if (isDeleted) return "Line " + oldLine + " DELETED";
            return "Line " + oldLine + " → " + newLine + (isRenamed ? " (RENAMED)" : "");
        }
    }
    
    public static void main(String[] args) throws IOException {
        Path outputsDir = Paths.get("outputs");
        Path testsDir = Paths.get("tests");
        
        // Check if folders exist
        if (!Files.exists(outputsDir)) {
            System.out.println("Error: 'outputs' directory does not exist!");
            System.out.println("Please create the 'outputs' folder with your manual XML files.");
            return;
        }
        
        if (!Files.exists(testsDir)) {
            System.out.println("Error: 'tests' directory does not exist!");
            System.out.println("Please create the 'tests' folder with generated .out files.");
            return;
        }
        
        // Get all XML files from outputs directory
        List<Path> xmlFiles = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(outputsDir, "*.xml")) {
            for (Path entry : stream) {
                xmlFiles.add(entry);
            }
        }
        
        if (xmlFiles.isEmpty()) {
            System.out.println("No XML files found in " + outputsDir);
            return;
        }
        
        System.out.println("=== XML vs .OUT File Comparison ===\n");
        
        int totalFiles = 0;
        int totalMappings = 0;
        int correctMappings = 0;
        
        for (Path xmlFile : xmlFiles) {
            String baseName = xmlFile.getFileName().toString().replace(".xml", "");
            Path outFile = testsDir.resolve(baseName + ".out");
            
            if (!Files.exists(outFile)) {
                System.out.println("WARNING: No matching .out file found for " + baseName + ".xml");
                continue;
            }
            
            System.out.println("File: " + baseName);
            System.out.println("=".repeat(40));
            
            // Parse manual mappings from XML
            List<ManualMapping> manualMappings = parseManualXML(xmlFile);
            
            // Parse tool mappings from .out file
            Map<Integer, ToolMapping> toolMappings = parseToolOUT(outFile);
            
            // Compare mappings
            int fileMappings = 0;
            int fileCorrect = 0;
            
            for (ManualMapping manual : manualMappings) {
                fileMappings++;
                totalMappings++;
                
                boolean isCorrect = false;
                String status = "";
                
                if (manual.newLine == -1) {
                    // Manual says line was deleted
                    ToolMapping toolMap = toolMappings.get(manual.oldLine);
                    if (toolMap != null && toolMap.isDeleted) {
                        isCorrect = true;
                        status = "✓ CORRECT (Line deleted)";
                    } else if (toolMap != null) {
                        status = "✗ INCORRECT (Tool maps line " + manual.oldLine + " to line " + toolMap.newLine + ")";
                    } else {
                        status = "✗ INCORRECT (Line " + manual.oldLine + " not found in tool output)";
                    }
                } else {
                    // Manual says line was mapped to newLine
                    ToolMapping toolMap = toolMappings.get(manual.oldLine);
                    if (toolMap != null && !toolMap.isDeleted && toolMap.newLine == manual.newLine) {
                        isCorrect = true;
                        status = "✓ CORRECT (Matched to line " + manual.newLine + ")";
                    } else if (toolMap != null && toolMap.isDeleted) {
                        status = "✗ INCORRECT (Tool says line " + manual.oldLine + " was deleted, but manual maps to " + manual.newLine + ")";
                    } else if (toolMap != null) {
                        status = "✗ INCORRECT (Tool maps line " + manual.oldLine + " to line " + toolMap.newLine + ", not " + manual.newLine + ")";
                    } else {
                        status = "✗ INCORRECT (Line " + manual.oldLine + " not found in tool output)";
                    }
                }
                
                if (isCorrect) {
                    fileCorrect++;
                    correctMappings++;
                }
                
                System.out.printf("Manual: %3d → %3s | %s%n", 
                    manual.oldLine, 
                    manual.newLine == -1 ? "DEL" : manual.newLine,
                    status);
            }
            
            double accuracy = fileMappings > 0 ? (double) fileCorrect / fileMappings * 100 : 0;
            System.out.printf("%nSummary for %s: %d/%d correct (%.1f%%)%n%n", 
                baseName, fileCorrect, fileMappings, accuracy);
            
            totalFiles++;
        }
        
        System.out.println("=".repeat(60));
        System.out.println("FINAL SUMMARY:");
        System.out.println("Files processed: " + totalFiles);
        System.out.println("Total mappings checked: " + totalMappings);
        System.out.println("Correct mappings: " + correctMappings);
        
        if (totalMappings > 0) {
            double overallAccuracy = (double) correctMappings / totalMappings * 100;
            System.out.printf("Overall accuracy: %.1f%%%n", overallAccuracy);
        }
    }
    
    private static List<ManualMapping> parseManualXML(Path xmlFile) throws IOException {
        List<ManualMapping> mappings = new ArrayList<>();
        List<String> lines = Files.readAllLines(xmlFile);
        
        boolean inVersion2 = false;
        Pattern locationPattern = Pattern.compile("<LOCATION ORIG=\"(\\d+)\" NEW=\"(-?\\d+)\" */>");
        
        for (String line : lines) {
            line = line.trim();
            
            if (line.contains("<VERSION NUMBER=\"2\"")) {
                inVersion2 = true;
                continue;
            }
            
            if (inVersion2 && line.contains("</VERSION>")) {
                break;
            }
            
            if (inVersion2) {
                Matcher matcher = locationPattern.matcher(line);
                if (matcher.find()) {
                    int oldLine = Integer.parseInt(matcher.group(1));
                    int newLine = Integer.parseInt(matcher.group(2));
                    mappings.add(new ManualMapping(oldLine, newLine));
                }
            }
        }
        
        if (mappings.size() != 10) {
            System.out.println("WARNING: " + xmlFile.getFileName() + " has " + mappings.size() + " mappings (expected 10)");
        }
        
        return mappings;
    }
    
    private static Map<Integer, ToolMapping> parseToolOUT(Path outFile) throws IOException {
        Map<Integer, ToolMapping> mappings = new HashMap<>();
        List<String> lines = Files.readAllLines(outFile);
        
        Pattern matchPattern = Pattern.compile("(?:\\[RENAMED\\] )?Old Line (\\d+) ↔ New Line (\\d+)");
        Pattern deletedPattern = Pattern.compile("Old Line (\\d+):");
        
        boolean inMatchedSection = false;
        boolean inDeletedSection = false;
        
        for (String line : lines) {
            line = line.trim();
            
            if (line.contains("== Matched Lines (Side-by-Side) ==")) {
                inMatchedSection = true;
                inDeletedSection = false;
                continue;
            }
            
            if (line.contains("== Deleted Lines (Old but not in New) ==")) {
                inMatchedSection = false;
                inDeletedSection = true;
                continue;
            }
            
            if (line.contains("== Inserted Lines") || line.contains("== Summary ==")) {
                inMatchedSection = false;
                inDeletedSection = false;
                continue;
            }
            
            if (inMatchedSection && !line.isEmpty() && !line.contains("---")) {
                Matcher matcher = matchPattern.matcher(line);
                if (matcher.find()) {
                    int oldLine = Integer.parseInt(matcher.group(1));
                    int newLine = Integer.parseInt(matcher.group(2));
                    boolean isRenamed = line.startsWith("[RENAMED]");
                    mappings.put(oldLine, new ToolMapping(oldLine, newLine, isRenamed, false));
                }
            }
            
            if (inDeletedSection && !line.isEmpty()) {
                Matcher matcher = deletedPattern.matcher(line);
                if (matcher.find()) {
                    int oldLine = Integer.parseInt(matcher.group(1));
                    mappings.put(oldLine, new ToolMapping(oldLine, -1, false, true));
                }
            }
        }
        
        return mappings;
    }
}