import java.io.*;
import java.util.*;

public class FileMapper {

    private int m, n, firstLine = 0, lastLine = 0;
    private boolean isMatching = false;
    private String firstStr = "", curNewStr = "";
    private String[][] oldFileLines, newFileLines;

    Map<Integer, List<Integer>> mappings = new HashMap<>();

    
    //TODO: Don't delete this method
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

    
    /*public static File[] readFiles2() {
        // Use proper file paths with escaped backslashes or forward slashes
        String oldFilePath = "C:\\testFiles\\oldFile.txt".trim().replace("\"", "");
        String newFilePath = "C:\\testFiles\\oldFile.txt".trim().replace("\"", "");
        
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
        } else {
            return null; // Or handle the error appropriately
        }
    }*/

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

        // Initialize arrays to store file contents - need List of String arrays
        java.util.List<String[]> oldFileLinesList = new java.util.ArrayList<>();
        java.util.List<String[]> newFileLinesList = new java.util.ArrayList<>();

        // Read both files
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

        // Convert to arrays - corrected syntax
        String[][] oldFileLines = oldFileLinesList.toArray(new String[0][]);
        String[][] newFileLines = newFileLinesList.toArray(new String[0][]);

        // Return as 3D array - corrected syntax
        return new String[][][]{oldFileLines, newFileLines};      
    }


    public static void printLineArrays(String[][][] lineArrays) {
        
        System.out.println("\nprintLineArrays method called");

        System.out.println("=" .repeat(50));

        System.out.println("\nDisplaying line arrays of old file:\n" + Arrays.deepToString(lineArrays[0]));

        System.out.println("\nDisplaying line arrays of new file:\n" + Arrays.deepToString(lineArrays[1]));
    }

    
    public boolean firstStrFound(String firstStr, String[][] newFileLines) {
        System.out.println("\nfirstStrFound method called");

        System.out.println("=" .repeat(50));

        System.out.println("\nFirst string is: " + firstStr);

        for (m = 0; m < newFileLines.length; m++) {
            for (n = 0; n < newFileLines[m].length; n++) {
                curNewStr = newFileLines[m][n];
                System.out.println("\nCurrent string in new file is: " + curNewStr);
                if (curNewStr.equals(firstStr)) {
                    System.out.println("\nMatching first string = " + curNewStr);
                    firstLine = m+1;
                    return true;
                }
            }
        }

        return false;
    }


    public boolean otherWordsMatch(String[] oldFileLine, String[][] newFileLines) {

        System.out.println("\notherWordsMatch method called");

        System.out.println("=" .repeat(50));

        String curOldStr = "", curNewStr = "";

        // Checking other words in the current old file line
        for (int j = 1; j < oldFileLine.length; j++) {
            n++;
            curOldStr = oldFileLine[j];
            
            System.out.println("\nCurrent string in old file = " + curOldStr);

            // Bounds checking           
            if (m >= newFileLines.length) {
                return false; // m is already out of bounds
            }

            if (n >= newFileLines[m].length) {
                if ((m + 1) >= newFileLines.length) {
                    return false; // No next line available
                }
                m++;
                n = 0;
                // Optional: Check if new line has any words
                if (n >= newFileLines[m].length) {
                    return false; // New line is empty
                }
            }

            curNewStr = newFileLines[m][n];

            System.out.println("\nComparing " + curOldStr + " with " + curNewStr);

            if(!curOldStr.equals(curNewStr)) {
                return false;
            }
        }

        lastLine = m+1;
        return true;          
    }


    public void map(String[][][] lineArrays) {
        System.out.println("\nmap method called");

        System.out.println("=" .repeat(50));

        int num;
        String firstStr = "";
        boolean isMatching = false;

        String[][] oldFileLines = lineArrays[0];
        String[][] newFileLines = lineArrays[1];

        // Step 1: Iterating over each old file line
        for (int i = 0; i < oldFileLines.length; i++) { 
            
            m = 0; n = 0;

            System.out.println("\nCurrent old line = " + Arrays.toString(oldFileLines[i]));

            // Step 2: Getting the first string in the current old file line
            firstStr = oldFileLines[i][0];

            System.out.println("\nCurrent first string: " + firstStr);

            //TODO: Change later to check negatives
            if (firstStrFound(firstStr, newFileLines) && otherWordsMatch(oldFileLines[i], newFileLines)) {
                isMatching = true;
                System.out.println("\nExact match found");
            } else {
                System.out.println("\nExact match not found");
                firstLine = 0;
                lastLine = 0;
                continue; // continue to next old line
            }

            num = i + 1;

            System.out.println("\nNew last line for old line " + num + " is " + lastLine);

            mappings.put(num, Arrays.asList(firstLine, lastLine));

            System.out.println("\n" + num + " maps to " + mappings.get(num)); // 1 maps to [m1, m2]
                          
        }

        System.out.println("\nAll mappings:");

        for (int k = 0; k < mappings.size(); k++) {
            num = k + 1;
            System.out.println(num + " maps to " + mappings.get(num));
        }
    
    }
    

    public static void main(String[] args) {
        
        String[][][] lineArrays;
        FileMapper mapper = new FileMapper();
        Scanner scanner = new Scanner(System.in);
        
        File[] files = readFiles(scanner);

        //File[] files = readFiles2();

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