import java.util.*;

public class LineMapper {

    private int m, n, firstLine = 0, lastLine = 0;
    private boolean isMatching = false;
    private String firstStr = "", curNewStr = "";

    Map<Integer, List<Integer>> mappings = new HashMap<>();

    public boolean firstStrFound(String firstStr, String[][] newFileLines) {
        System.out.println("\nfirstStrFound method called");

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


    public void map(String[][] oldFileLines, String[][] newFileLines) {
        int num;
        String firstStr = "";
        boolean isMatching = false;

        //Map<Integer, List<Integer>> mappings = new HashMap<>();       

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

            System.out.println(num + " maps to " + mappings.get(num)); // 1 maps to [m1, m2]
                          
        }

        System.out.println("\nAll mappings:");

        for (int k = 0; k < mappings.size(); k++) {
            num = k + 1;
            System.out.println(num + " maps to " + mappings.get(num));
        }
    
    }
    

    public static void main(String[] args) {
        LineMapper mapper = new LineMapper();

        String[][] oldFileLines = {
            {"John", "is", "really", "good"}
        };

        String[][] newFileLines = {
            {"John"},
            {"is", "really"},
            {"good"}
        };

        mapper.map(oldFileLines, newFileLines);
    }
}