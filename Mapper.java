import java.io.*;
import java.util.*;
import java.nio.file.*;

public class Mapper {
    
    private static final double CONTENT_WEIGHT = 0.6;
    private static final double CONTEXT_WEIGHT = 0.4;
    private static final double SIMILARITY_THRESHOLD = 0.5;
    private static final int CONTEXT_WINDOW = 4;

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== FILE COMPARISON TOOL ===");
        System.out.println("Enter file paths for sequential comparison");
        System.out.println("First file compared with itself and next file");
        System.out.println("Each subsequent file compared with next file");
        System.out.println("Enter 'q' when done");
        System.out.println("==========================================\n");
        
        List<List<String>> allFileLines = new ArrayList<>();
        List<String> fileNames = new ArrayList<>();
        
        // Collect all files until user enters 'q'
        int fileCount = 1;
        while (true) {
            System.out.print("Enter path for file " + fileCount + " (or 'q' to finish): ");
            String filePath = scanner.nextLine().trim();
            
            if (filePath.equalsIgnoreCase("q")) {
                if (allFileLines.size() < 2) {
                    System.out.println("Need at least 2 files for comparison. Exiting.");
                    scanner.close();
                    return;
                }
                break;
            }
            
            filePath = cleanFilePath(filePath);
            
            // Verify file exists
            if (!Files.exists(Paths.get(filePath))) {
                System.err.println("Error: File does not exist: " + filePath);
                continue;
            }
            
            // Read the file
            List<String> fileLines = Files.readAllLines(Paths.get(filePath));
            String fileName = new File(filePath).getName();
            
            allFileLines.add(fileLines);
            fileNames.add(fileName);
            
            System.out.println("✓ Loaded: " + fileName + " (" + fileLines.size() + " lines)");
            fileCount++;
        }
        
        System.out.println("\n=== PROCESSING " + allFileLines.size() + " FILES ===");
        System.out.println("Performing sequential comparisons...\n");
        
        // Perform sequential comparisons:
        // 1. File 1 with File 1 (self-comparison)
        // 2. File 1 with File 2
        // 3. File 2 with File 3
        // etc.
        
        List<String> allXMLResults = new ArrayList<>();
        
        for (int i = 0; i < allFileLines.size(); i++) {
            // Compare with itself (only for first file)
            if (i == 0) {
                System.out.println("=== COMPARISON 1: " + fileNames.get(i) + " with itself ===");
                List<String> fileLines = allFileLines.get(i);
                String fileName = getBaseFileName(fileNames.get(i));
                
                Map<Integer, Integer> selfMappings = new HashMap<>();
                for (int j = 1; j <= fileLines.size(); j++) {
                    selfMappings.put(j, j);
                }
                
                String xmlOutput = generateXML(fileName, fileLines, fileLines, selfMappings, i+1, i+1);
                allXMLResults.add(xmlOutput);
                
                System.out.println("Generated self-mapping for " + fileNames.get(i));
                System.out.println("=".repeat(60) + "\n");
            }
            
            // Compare with next file (if there is one)
            if (i + 1 < allFileLines.size()) {
                System.out.println("=== COMPARISON " + (i+1) + " → " + (i+2) + ": " + 
                                 fileNames.get(i) + " → " + fileNames.get(i+1) + " ===");
                
                List<String> oldLines = allFileLines.get(i);
                List<String> newLines = allFileLines.get(i+1);
                String fileName = getBaseFileName(fileNames.get(i));
                
                System.out.println("Old file: " + oldLines.size() + " lines");
                System.out.println("New file: " + newLines.size() + " lines");
                
                Map<Integer, Integer> mappings = mapLines(oldLines, newLines);
                String xmlOutput = generateXML(fileName, oldLines, newLines, mappings, i+1, i+2);
                allXMLResults.add(xmlOutput);
                
                System.out.println("Found " + mappings.size() + " mappings");
                System.out.println("=".repeat(60) + "\n");
            }
        }
        
        // Display all results
        System.out.println("\n=== ALL COMPARISON RESULTS ===\n");
        for (int i = 0; i < allXMLResults.size(); i++) {
            System.out.println("RESULT " + (i+1) + ":");
            System.out.println(allXMLResults.get(i));
            System.out.println("\n" + "=".repeat(80) + "\n");
        }
        
        // Ask about saving
        System.out.print("Save all results to files? (y/n): ");
        String saveChoice = scanner.nextLine().trim().toLowerCase();
        if (saveChoice.equals("y") || saveChoice.equals("yes")) {
            for (int i = 0; i < allXMLResults.size(); i++) {
                String fileName;
                if (i == 0) {
                    fileName = getBaseFileName(fileNames.get(0)) + "_self_mapping.xml";
                } else {
                    fileName = getBaseFileName(fileNames.get(i-1)) + "_to_" + 
                              getBaseFileName(fileNames.get(i)) + "_mapping.xml";
                }
                
                System.out.print("Save result " + (i+1) + " as " + fileName + "? (y/n): ");
                String saveFile = scanner.nextLine().trim().toLowerCase();
                if (saveFile.equals("y") || saveFile.equals("yes")) {
                    try {
                        Files.write(Paths.get(fileName), allXMLResults.get(i).getBytes());
                        System.out.println("✓ Saved to: " + fileName);
                    } catch (IOException e) {
                        System.err.println("Error saving file: " + e.getMessage());
                    }
                }
            }
        }
        
        System.out.println("\nAll comparisons completed!");
        scanner.close();
    }
    
    private static String getBaseFileName(String fileName) {
        if (fileName.contains("_")) {
            return fileName.substring(0, fileName.indexOf("_"));
        }
        if (fileName.contains(".")) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        }
        return fileName;
    }
    
    private static String cleanFilePath(String path) {
        // Remove surrounding quotation marks if present
        if (path.startsWith("\"") && path.endsWith("\"")) {
            return path.substring(1, path.length() - 1);
        }
        if (path.startsWith("'") && path.endsWith("'")) {
            return path.substring(1, path.length() - 1);
        }
        return path;
    }
    
    private static Map<Integer, Integer> mapLines(List<String> oldLines, List<String> newLines) {
        Map<Integer, Integer> mappings = new HashMap<>();
        
        // Preprocess lines
        List<String> normalizedOld = normalizeLines(oldLines);
        List<String> normalizedNew = normalizeLines(newLines);
        
        System.out.print("Calculating similarities... ");
        
        // Keep track of which new lines have already been mapped
        Set<Integer> mappedNewLines = new HashSet<>();
        
        // For ALL old lines, find best match in new file
        for (int oldIdx = 0; oldIdx < normalizedOld.size(); oldIdx++) {
            int oldLineNum = oldIdx + 1;
            
            String oldLine = normalizedOld.get(oldIdx);
            double bestScore = 0.0;
            int bestMatch = -1;
            
            // Check each new line that hasn't been mapped yet
            for (int newIdx = 0; newIdx < normalizedNew.size(); newIdx++) {
                // Skip if this new line is already mapped
                if (mappedNewLines.contains(newIdx)) {
                    continue;
                }
                
                String newLine = normalizedNew.get(newIdx);
                
                double contentSim = calculateContentSimilarity(oldLine, newLine);
                double contextSim = calculateContextSimilarity(normalizedOld, normalizedNew, oldIdx, newIdx);
                
                double combinedScore = (CONTENT_WEIGHT * contentSim) + (CONTEXT_WEIGHT * contextSim);
                
                // Small position bonus
                double positionBonus = 1.0 - (Math.abs(oldIdx - newIdx) / (double) Math.max(oldLines.size(), newLines.size()));
                combinedScore += positionBonus * 0.05;
                
                if (combinedScore > bestScore && combinedScore >= SIMILARITY_THRESHOLD) {
                    bestScore = combinedScore;
                    bestMatch = newIdx;
                }
            }
            
            if (bestMatch != -1) {
                int newLineNum = bestMatch + 1;
                mappings.put(oldLineNum, newLineNum);
                mappedNewLines.add(bestMatch);
            }
        }
        
        System.out.println("Done!");
        System.out.println("✓ Found " + mappings.size() + " mappings out of " + oldLines.size() + " old lines.");
        return mappings;
    }
    
    private static List<String> normalizeLines(List<String> lines) {
        List<String> normalized = new ArrayList<>();
        for (String line : lines) {
            // Special normalization for Java source files
            String norm = line.trim();
            
            // Remove inline comments
            norm = norm.replaceAll("//.*$", "");
            norm = norm.replaceAll("/\\*.*\\*/", "");
            
            // Remove NLS tags like //$NON-NLS-1$
            norm = norm.replaceAll("//\\$NON-NLS-[0-9]+\\$", "");
            
            // Remove comment blocks (partial handling)
            if (norm.startsWith("/*") || norm.startsWith("*")) {
                norm = "";
            }
            
            if (!norm.isEmpty()) {
                // Normalize string literals - replace with placeholder
                norm = norm.replaceAll("\"[^\"]*\"", "\"STRING\"");
                norm = norm.replaceAll("\'[^\']*\'", "\'CHAR\'");
                
                // Normalize whitespace
                norm = norm.toLowerCase().replaceAll("\\s+", " ");
            }
            normalized.add(norm);
        }
        return normalized;
    }
    
    private static double calculateContentSimilarity(String s1, String s2) {
        // Handle empty strings
        if (s1.isEmpty() && s2.isEmpty()) return 1.0;
        if (s1.isEmpty() || s2.isEmpty()) return 0.0;
        
        // Check for common patterns
        if ((s1.contains("if (") && s2.contains("if (")) ||
            (s1.contains("return ") && s2.contains("return ")) ||
            (s1.contains("public ") && s2.contains("public ")) ||
            (s1.contains("private ") && s2.contains("private "))) {
            // Same keyword - give small bonus
            return 0.3 + (1.0 - levenshteinDistance(s1, s2) / (double) Math.max(s1.length(), s2.length())) * 0.7;
        }
        
        // Levenshtein distance similarity (normalized)
        int maxLen = Math.max(s1.length(), s2.length());
        int distance = levenshteinDistance(s1, s2);
        return 1.0 - ((double) distance / maxLen);
    }
    
    private static int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        
        return dp[a.length()][b.length()];
    }
    
    private static double calculateContextSimilarity(List<String> oldLines, List<String> newLines, 
                                                     int oldIdx, int newIdx) {
        // Context = surrounding lines within CONTEXT_WINDOW
        List<String> oldContext = getContext(oldLines, oldIdx, CONTEXT_WINDOW);
        List<String> newContext = getContext(newLines, newIdx, CONTEXT_WINDOW);
        
        // If both contexts are empty, return 1.0 (perfect match for empty context)
        if (oldContext.isEmpty() && newContext.isEmpty()) return 1.0;
        
        // Convert context to a single string for cosine similarity
        String oldContextStr = String.join(" ", oldContext);
        String newContextStr = String.join(" ", newContext);
        
        return cosineSimilarity(oldContextStr, newContextStr);
    }
    
    private static List<String> getContext(List<String> lines, int idx, int window) {
        List<String> context = new ArrayList<>();
        int start = Math.max(0, idx - window);
        int end = Math.min(lines.size() - 1, idx + window);
        
        for (int i = start; i <= end; i++) {
            if (i != idx) {
                String line = lines.get(i);
                if (!line.isEmpty()) {
                    context.add(line);
                }
            }
        }
        return context;
    }
    
    private static double cosineSimilarity(String s1, String s2) {
        // Simplified cosine similarity using word frequencies
        if (s1.isEmpty() && s2.isEmpty()) return 1.0;
        if (s1.isEmpty() || s2.isEmpty()) return 0.0;
        
        Map<String, Integer> freq1 = getTermFrequency(s1);
        Map<String, Integer> freq2 = getTermFrequency(s2);
        
        Set<String> allTerms = new HashSet<>();
        allTerms.addAll(freq1.keySet());
        allTerms.addAll(freq2.keySet());
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (String term : allTerms) {
            int f1 = freq1.getOrDefault(term, 0);
            int f2 = freq2.getOrDefault(term, 0);
            
            dotProduct += f1 * f2;
            norm1 += f1 * f1;
            norm2 += f2 * f2;
        }
        
        if (norm1 == 0 || norm2 == 0) return 0.0;
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    private static Map<String, Integer> getTermFrequency(String text) {
        Map<String, Integer> freq = new HashMap<>();
        String[] words = text.split("\\s+");
        
        for (String word : words) {
            if (word.length() > 0) {
                freq.put(word, freq.getOrDefault(word, 0) + 1);
            }
        }
        return freq;
    }
    
    private static String generateXML(String fileName, List<String> oldLines, List<String> newLines,
                                    Map<Integer, Integer> mappings, int versionFrom, int versionTo) {
        StringBuilder xml = new StringBuilder();
        
        if (versionFrom == versionTo) {
            // Self-comparison
            xml.append("<TEST NAME=\"TEST_SELF\" FILE=\"").append(fileName).append("\">\n\n");
            xml.append(" <VERSION NUMBER=\"1\" CHECKED=\"TRUE\">\n\n");
            
            // Map ALL lines to themselves
            for (int oldLineNum = 1; oldLineNum <= oldLines.size(); oldLineNum++) {
                xml.append(String.format("   <LOCATION ORIG=\"%d\" NEW=\"%d\" />\n", oldLineNum, oldLineNum));
            }
            xml.append("   \n</VERSION>\n");
        } else {
            // Comparison between different files
            xml.append("<TEST NAME=\"TEST_").append(versionFrom).append("_").append(versionTo)
               .append("\" FILE=\"").append(fileName).append("\">\n\n");
            xml.append(" <VERSION NUMBER=\"").append(versionTo).append("\" CHECKED=\"TRUE\">\n");
            
            // Map ALL lines from old file to new file (or -1 if no match)
            for (int oldLineNum = 1; oldLineNum <= oldLines.size(); oldLineNum++) {
                int newLineNum = mappings.getOrDefault(oldLineNum, -1);
                xml.append(String.format("   <LOCATION ORIG=\"%d\" NEW=\"%d\" />\n", oldLineNum, newLineNum));
            }
            xml.append(" </VERSION>\n");
        }
        
        xml.append("</TEST>");
        return xml.toString();
    }
}