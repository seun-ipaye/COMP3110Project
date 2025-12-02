import java.io.*;
import java.util.*;

public class LineMapper {

    private Map<Integer, List<List<Integer>>> mappings = new HashMap<>();
    
    public static File[] readFiles(Scanner scanner) {
        while (true) {
            System.out.print("\nEnter the path of the old file (or 'q' to exit): "); 
            String oldFilePath = scanner.nextLine().trim().replace("\"", "");

            if (oldFilePath.equalsIgnoreCase("q")) {
                return null;
            }

            System.out.print("\nEnter the path of the new file (or 'q' to exit): ");
            String newFilePath = scanner.nextLine().trim().replace("\"", "");
            
            if (newFilePath.equalsIgnoreCase("q")) {
                return null;
            }
            
            String[] filePaths = {oldFilePath, newFilePath};
            File[] files = new File[2];
            boolean validFiles = true;

            for (int i = 0; i < filePaths.length; i++) {
                String filePath = filePaths[i];
                
                if (filePath.isEmpty()) {
                    System.out.println("Error: No path entered for file " + (i + 1) + ".\n");
                    validFiles = false;
                    break;
                }
                
                File file = new File(filePath);
                
                if (!file.exists() || !file.isFile()) {
                    System.out.println("Error: File '" + filePath + "' does not exist.\n");
                    validFiles = false;
                    break;
                }
                
                if (!file.canRead()) {
                    System.out.println("Error: Cannot read file '" + filePath + "'. Check permissions.\n");
                    validFiles = false;
                    break;
                }

                files[i] = file;
            }
            
            if (validFiles) {
                return files;
            }
        }
    }
    
    public static void printFileContents(File[] files) throws IOException {
        System.out.println("\nprintFileContents method called");
        System.out.println("=" .repeat(50));

        if (files == null) {
            System.out.println("No files to display.");
            return;
        }

        System.out.println("\nVerifying contents of entered files");

        for (int i = 0; i < files.length; i++) {
            System.out.println("\nDisplaying contents of file " + (i+1) + ": " + files[i].getName());
            
            try (BufferedReader reader = new BufferedReader(new FileReader(files[i]))) {
                String line;
                int lineNumber = 1;
                while ((line = reader.readLine()) != null) {                
                    System.out.printf("%-3d: %s%n", lineNumber, line);
                    lineNumber++;
                }
            }
            System.out.println("=" .repeat(50));
        }
    }

    public static String[][][] convertTo2dArrays(File[] files) throws IOException {
        List<String[]> oldFileLinesList = new ArrayList<>();
        List<String[]> newFileLinesList = new ArrayList<>();

        try (BufferedReader reader1 = new BufferedReader(new FileReader(files[0]))) {
            String line;
            while ((line = reader1.readLine()) != null) {
                String[] array = line.trim().split("\\s+");
                oldFileLinesList.add(array);
            }
        }

        try (BufferedReader reader2 = new BufferedReader(new FileReader(files[1]))) {
            String line;
            while ((line = reader2.readLine()) != null) {
                String[] array = line.trim().split("\\s+");
                newFileLinesList.add(array);
            }
        }

        String[][] oldFileLines = oldFileLinesList.toArray(new String[0][]);
        String[][] newFileLines = newFileLinesList.toArray(new String[0][]);

        return new String[][][]{oldFileLines, newFileLines};      
    }

    public static void printLineArrays(String[][][] lineArrays) {
        System.out.println("\nprintLineArrays method called");
        System.out.println("=" .repeat(50));

        System.out.println("\nDisplaying line arrays of old file:\n" + Arrays.deepToString(lineArrays[0]));
        System.out.println("\nDisplaying line arrays of new file:\n" + Arrays.deepToString(lineArrays[1]));
    }

    /**
     * Finds ALL possible starting positions where old line could match in new file
     */
    private List<MatchPosition> findPotentialMatches(String[] oldLine, String[][] newFileLines) {
        List<MatchPosition> potentialMatches = new ArrayList<>();
        String firstWord = oldLine[0];
        
        // Find all positions where first word appears in new file
        for (int lineNum = 0; lineNum < newFileLines.length; lineNum++) {
            for (int wordNum = 0; wordNum < newFileLines[lineNum].length; wordNum++) {
                if (newFileLines[lineNum][wordNum].equals(firstWord)) {
                    potentialMatches.add(new MatchPosition(lineNum, wordNum));
                }
            }
        }
        
        return potentialMatches;
    }
    
    /**
     * Checks if old line matches starting from a specific position in new file
     */
    private List<Integer> checkMatchFromPosition(String[] oldLine, String[][] newFileLines, 
                                                MatchPosition startPos) {
        List<Integer> matchedLines = new ArrayList<>();
        int currentLine = startPos.lineNum;
        int currentWord = startPos.wordNum;
        
        // Check if all words in old line can be matched
        for (int i = 0; i < oldLine.length; i++) {
            // If we've moved to a new line, add it to matched lines
            if (i == 0 || currentWord == 0) {
                matchedLines.add(currentLine + 1); // +1 for 1-based line numbers
            }
            
            // Check bounds
            if (currentLine >= newFileLines.length || 
                currentWord >= newFileLines[currentLine].length) {
                return null; // No match
            }
            
            // Check if words match
            if (!oldLine[i].equals(newFileLines[currentLine][currentWord])) {
                return null; // No match
            }
            
            // Move to next word/line
            currentWord++;
            if (currentWord >= newFileLines[currentLine].length) {
                currentLine++;
                currentWord = 0;
            }
        }
        
        return matchedLines;
    }
    
    /**
     * Maps old lines to multiple new lines when a single old line splits across multiple new lines
     */
    public void map(String[][][] lineArrays) {
        System.out.println("\nmap method called");
        System.out.println("=" .repeat(50));

        String[][] oldFileLines = lineArrays[0];
        String[][] newFileLines = lineArrays[1];

        // For each old line, find all possible matches
        for (int oldLineNum = 0; oldLineNum < oldFileLines.length; oldLineNum++) {
            String[] oldLine = oldFileLines[oldLineNum];
            System.out.println("\nProcessing old line " + (oldLineNum + 1) + ": " + Arrays.toString(oldLine));
            
            List<MatchPosition> potentialStarts = findPotentialMatches(oldLine, newFileLines);
            List<List<Integer>> allMatchesForThisLine = new ArrayList<>();
            
            // Try each potential starting position
            for (MatchPosition startPos : potentialStarts) {
                List<Integer> matchedNewLines = checkMatchFromPosition(oldLine, newFileLines, startPos);
                if (matchedNewLines != null && !matchedNewLines.isEmpty()) {
                    allMatchesForThisLine.add(matchedNewLines);
                    System.out.println("  Found match starting at line " + (startPos.lineNum + 1) + 
                                     ", word " + (startPos.wordNum + 1));
                }
            }
            
            if (allMatchesForThisLine.isEmpty()) {
                System.out.println("  No matches found for old line " + (oldLineNum + 1));
                mappings.put(oldLineNum + 1, Arrays.asList(Collections.emptyList()));
            } else {
                mappings.put(oldLineNum + 1, allMatchesForThisLine);
                System.out.println("  Total matches found: " + allMatchesForThisLine.size());
            }
        }
        
        // Print all mappings
        System.out.println("\n" + "=" .repeat(50));
        System.out.println("FINAL MAPPINGS:");
        System.out.println("=" .repeat(50));
        
        for (Map.Entry<Integer, List<List<Integer>>> entry : mappings.entrySet()) {
            System.out.print("Old line " + entry.getKey() + " → ");
            List<List<Integer>> matches = entry.getValue();
            
            if (matches.isEmpty() || (matches.size() == 1 && matches.get(0).isEmpty())) {
                System.out.println("deleted");
            } else if (matches.size() == 1) {
                List<Integer> match = matches.get(0);
                if (match.size() == 1) {
                    System.out.println(match.get(0));
                } else {
                    System.out.println(formatWithParentheses(match));
                }
            } else {
                // Multiple possible matches
                System.out.print("[");
                for (int i = 0; i < matches.size(); i++) {
                    List<Integer> match = matches.get(i);
                    if (match.size() == 1) {
                        System.out.print(match.get(0));
                    } else {
                        System.out.print(formatWithParentheses(match));
                    }
                    if (i < matches.size() - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.println("]");
            }
        }
    }
    
    /**
     * Formatting line numbers with parentheses around each string chain
     * Example: [3, 4, 5] becomes "[3, 4, 5]" (single chain)
     * Example: [[3], [4, 5]] becomes "[3], [4, 5]" (multiple chains)
     */
    private String formatWithParentheses(List<Integer> lineNumbers) {
        if (lineNumbers.isEmpty()) return "deleted";
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < lineNumbers.size(); i++) {
            sb.append(lineNumbers.get(i));
            if (i < lineNumbers.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Helper class to store match positions
     */
    private static class MatchPosition {
        int lineNum;
        int wordNum;
        
        MatchPosition(int lineNum, int wordNum) {
            this.lineNum = lineNum;
            this.wordNum = wordNum;
        }
    }
    
    public static void main(String[] args) {
        String[][][] lineArrays;
        LineMapper mapper = new LineMapper();
        Scanner scanner = new Scanner(System.in);
        
        File[] files = readFiles(scanner);

        try {
            printFileContents(files);
            lineArrays = convertTo2dArrays(files);
            printLineArrays(lineArrays);
            mapper.map(lineArrays);
        } catch (IOException e) {
            System.out.println("Error reading files: " + e.getMessage());
        }
    }
}