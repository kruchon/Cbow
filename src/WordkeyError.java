public class WordkeyError {
    private final int wordKey;
    private final double error;

    public WordkeyError(int wordKey, double error) {
        this.wordKey = wordKey;
        this.error = error;
    }

    public int getWordKey() {
        return wordKey;
    }

    public double getError() {
        return error;
    }
}
