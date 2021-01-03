import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Vocabulary {
    private final AtomicInteger keyCounter = new AtomicInteger(-1);
    private final Map<String, Integer> wordKeys = new TreeMap<>();

    public int add(String word) {
        if (wordKeys.containsKey(word)) {
            return wordKeys.get(word);
        } else {
            int keyCounterValue = keyCounter.incrementAndGet();
            System.out.println(keyCounterValue + " " + word);
            wordKeys.put(word, keyCounterValue);
            return keyCounterValue;
        }
    }

    public int getKey(String word) {
        return wordKeys.get(word);
    }
}
