import java.util.*;

public class LinkedListCount {
    static class WordFreq {
        String word;
        int count;
        WordFreq(String word, int count) {
            this.word = word;
            this.count = count;
        }
    }

    public static String mostFrequentWord(String[] tokens) {
        LinkedList<WordFreq> wordFreqList = new LinkedList<>();

        for (String token : tokens) {
            boolean found = false;

            for (WordFreq wf : wordFreqList) {
                if (wf.word.equals(token)) {
                    wf.count++;
                    found = true;
                    break;
                }
            }

            if (!found) {
                wordFreqList.add(new WordFreq(token, 1));
            }
        }

        WordFreq mostFreq = wordFreqList.getFirst();
        for (WordFreq wf : wordFreqList) {
            if (wf.count > mostFreq.count) {
                mostFreq = wf;
            }
        }

        return "Most frequent word is \"" + mostFreq.word + "\" with " + mostFreq.count + " occurrences.";
    }
}

