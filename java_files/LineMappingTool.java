import java.io.*;
import java.nio.file.*;
import java.util.*;

public class LineMappingTool {

    public static MatchedResult compareFiles(File oldFile, File newFile, File outputFile) throws IOException {
        return mapFiles(oldFile.toPath(), newFile.toPath(), outputFile);
    }

    public static MatchedResult mapFiles(Path oldFile, Path newFile, File outputFile) throws IOException {
        List<String> oldLines = Files.readAllLines(oldFile);
        List<String> newLines = Files.readAllLines(newFile);

        double threshold = 0.5;
        Set<Integer> matchedNewLines = new HashSet<>();
        Set<Integer> matchedOldLines = new HashSet<>();
        List<MatchedLine> matchedLines = new ArrayList<>();

        try (PrintWriter writer = new PrintWriter(outputFile)) {
            writer.println("== Matched Lines (Side-by-Side) ==");

            for (int i = 0; i < oldLines.size(); i++) {
                String oldLine = oldLines.get(i);
                double bestSim = 0.0;
                int bestJ = -1;

                for (int j = 0; j < newLines.size(); j++) {
                    if (matchedNewLines.contains(j)) continue;
                    String newLine = newLines.get(j);
                    double sim = similarity(oldLine, newLine);

                    if (sim > bestSim) {
                        bestSim = sim;
                        bestJ = j;
                    }
                }

                if (bestSim >= threshold && bestJ != -1) {
                    boolean exactMatch = oldLine.equals(newLines.get(bestJ));
                    boolean renamed = !exactMatch && similarity(normalizeLine(oldLine), normalizeLine(newLines.get(bestJ))) > 0.9;

                    int oldNum = i + 1;
                    int newNum = bestJ + 1;

                    if (renamed) {
                        writer.printf("[RENAMED] Old Line %d ↔ New Line %d%n", oldNum, newNum);
                    } else {
                        writer.printf("Old Line %d ↔ New Line %d%n", oldNum, newNum);
                    }

                    writer.printf("Old: %s%n", oldLine);
                    writer.printf("New: %s%n", newLines.get(bestJ));
                    writer.println("-----------------------------------------------------");

                    matchedOldLines.add(i);
                    matchedNewLines.add(bestJ);
                    matchedLines.add(new MatchedLine(oldNum, newNum));
                }
            }

            // Deleted lines
            writer.println("\n== Deleted Lines (Old but not in New) ==");
            for (int i = 0; i < oldLines.size(); i++) {
                if (!matchedOldLines.contains(i)) {
                    writer.printf("Old Line %d: %s%n", i + 1, oldLines.get(i));
                }
            }

            // Inserted lines
            writer.println("\n== Inserted Lines (New but not in Old) ==");
            for (int j = 0; j < newLines.size(); j++) {
                if (!matchedNewLines.contains(j)) {
                    writer.printf("New Line %d: %s%n", j + 1, newLines.get(j));
                }
            }

            writer.println("\n== Summary ==");
            writer.printf("Matched Lines: %d%n", matchedLines.size());
            writer.printf("Deleted Lines: %d%n", oldLines.size() - matchedOldLines.size());
            writer.printf("Inserted Lines: %d%n", newLines.size() - matchedNewLines.size());
        }

        System.out.println("Written to: " + outputFile.getName());
        return new MatchedResult(matchedLines);
    }

    // === Similarity using LCS of normalized lines ===
    public static double similarity(String a, String b) {
        if (a.equals(b)) return 1.0;

        String normA = normalizeLine(a);
        String normB = normalizeLine(b);

        int lcs = longestCommonSubsequence(normA, normB);
        int maxLen = Math.max(normA.length(), normB.length());
        if (maxLen == 0) return 1.0;

        return (double) lcs / maxLen;
    }

    public static int longestCommonSubsequence(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1))
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                else
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
            }
        }
        return dp[a.length()][b.length()];
    }

    // === Normalize line for renaming-aware matching ===
    private static String normalizeLine(String line) {
        line = line.replaceAll("//.*", "");                      // remove inline comments
        line = line.replaceAll("/\\*.*?\\*/", "");               // remove block comments
        line = line.replaceAll("\\b[_a-zA-Z][_a-zA-Z0-9]*\\b", "_var"); // replace identifiers
        line = line.replaceAll("\\b\\d+\\b", "_num");            // replace numbers
        line = line.trim().replaceAll("\\s+", " ");              // collapse whitespace
        return line;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java LineMappingTool <old_file> <new_file>");
            return;
        }

        Path oldFile = Paths.get(args[0]);
        Path newFile = Paths.get(args[1]);

        String outName =
                oldFile.getFileName().toString().replace(".java", "") + "_" +
                newFile.getFileName().toString().replace(".java", "") + ".out";

        mapFiles(oldFile, newFile, new File(outName));
    }
}
