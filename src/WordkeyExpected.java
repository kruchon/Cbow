public class WordkeyExpected {
    private final int wordKey;
    private final int expected;

    public WordkeyExpected(int wordKey, int expected) {
        this.wordKey = wordKey;
        this.expected = expected;
    }

    public int getWordKey() {
        return wordKey;
    }

    public int getExpected() {
        return expected;
    }
}