import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;

public class Cbow {

    public static void main(String[] args) {
        try {
            InputStream fstream = ClassLoader.getSystemResourceAsStream("textfile.txt");
            DataInputStream in = new DataInputStream(Objects.requireNonNull(fstream));
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "windows-1251"));
            CbowModel cbowModel = new CbowModel(15, 50, 0.4, 7000);
            Iterator<String> iterator = new Iterator<String>() {
                private LinkedList<String> currentLine = null;

                @Override
                public boolean hasNext() {
                    if (currentLine == null || currentLine.isEmpty()) {
                        try {
                            currentLine = tryToReadLine();
                        } catch (IOException e) {
                            return false;
                        }
                    }
                    return currentLine != null && !currentLine.isEmpty();
                }

                private LinkedList<String> tryToReadLine() throws IOException {
                    String newLine = br.readLine();
                    if (newLine == null) {
                        return null;
                    }
                    while ("".equals(newLine) || newLine.matches("[0-9]+")) {
                        newLine = br.readLine();
                        if (newLine == null) {
                            return null;
                        }
                    }
                    return new LinkedList<>(Arrays.asList(newLine.split("[\\p{Punct}\\s]+")));
                }

                @Override
                public String next() {
                    if (currentLine == null || currentLine.isEmpty()) {
                        try {
                            currentLine = tryToReadLine();
                        } catch (IOException ignored) { }
                    }
                    if (currentLine == null || currentLine.isEmpty()) {
                        throw new RuntimeException("Cannot read line");
                    }
                    return currentLine.removeFirst().toLowerCase();
                }
            };
            cbowModel.train(iterator);

            in.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
