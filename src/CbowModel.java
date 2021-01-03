import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.stream.IntStream;

public class CbowModel {
    private final Vocabulary vocabulary = new Vocabulary();
    private final int contextSize;
    private final double[][] inputWeights;
    private final double[][] outputWeights;
    private final int contextMidIndex;
    private final double trainingCoef;
    private final int featureSpaceSize;
    private final int vocabularySize;
    private final double[] avgFeatureVector;

    public CbowModel(int contextSize, int featureSpaceSize, double trainingCoef, int vocabularySize) {
        this.contextSize = contextSize;
        this.inputWeights = new double[vocabularySize][featureSpaceSize];
        this.outputWeights = new double[featureSpaceSize][vocabularySize];
        fillWeightsByRandomValues();
        contextMidIndex = (contextSize - 1) / 2;
        this.trainingCoef = trainingCoef;
        this.featureSpaceSize = featureSpaceSize;
        this.vocabularySize = vocabularySize;
        this.avgFeatureVector = new double[featureSpaceSize];
    }

    private void fillWeightsByRandomValues() {
        Random r = new Random();
        for (double[] inputWeightsRow : inputWeights) {
            for (int i = 0; i < inputWeightsRow.length; i++) {
                inputWeightsRow[i] = r.nextDouble();
            }
        }

        for (double[] outputWeightsRow : outputWeights) {
            for (int i = 0; i < outputWeightsRow.length; i++) {
                outputWeightsRow[i] = r.nextDouble();
            }
        }
    }

    public void train(Iterator<String> simpleTextIterator) {
        LinkedList<String> context = new LinkedList<>();
        initContext(context, simpleTextIterator);
        while (simpleTextIterator.hasNext()) {
            String word = simpleTextIterator.next();
            updateContext(context, word);
            updateFeatures(context);
        }
        calculateAvgFeatureVector();
    }

    private void calculateAvgFeatureVector() {
        for (int i = 0; i < featureSpaceSize; i++) {
            for (int j = 0; j < vocabularySize; j++) {
                avgFeatureVector[i] += inputWeights[j][i];
            }
            avgFeatureVector[i] /= vocabularySize;
        }
    }

    public double cosineSimilarity(String fWord, String sWord) {
        double[] fFeatures = normalisedFeatures(fWord);
        double[] sFeatures = normalisedFeatures(sWord);
        double numerator = 0;
        for (int i = 0; i < fFeatures.length; i++) {
            numerator += fFeatures[i] * sFeatures[i];
        }
        double fDenomerator = 0;
        for (double fFeature : fFeatures) {
            fDenomerator += fFeature * fFeature;
        }
        double sDenomerator = 0;
        for (double sFeature : sFeatures) {
            sDenomerator += sFeature * sFeature;
        }
        return numerator / Math.sqrt(fDenomerator * sDenomerator);
    }

    public double euclideanDistance(String fWord, String sWord) {
        double[] fFeatures = features(fWord);
        double[] sFeatures = features(sWord);
        double sum = 0;
        for (int i = 0; i < fFeatures.length; i++) {
            double sub = sFeatures[i] - fFeatures[i];
            sum += Math.pow(sub, 2);
        }
        return Math.sqrt(sum);
    }

    public double[] features(String word) {
        return inputWeights[vocabulary.getKey(word)];
    }

    public double[] normalisedFeatures(String word) {
        double[] features = features(word);
        for (int i = 0; i < featureSpaceSize; i++) {
            features[i] = avgFeatureVector[i] - features[i];
        }
        return features;
    }

    private void updateFeatures(LinkedList<String> context) {
        int targetWordKey = vocabulary.add(context.get(contextMidIndex));
        double[] hiddenLayer = calculateHiddenLayer(context);
        double[] output = calculateOutput(hiddenLayer);
        double[] prediction = softMax(output);
        correctWeights(prediction, targetWordKey, hiddenLayer);
    }

    private double[] calculateOutput(double[] hiddenLayer) {
        double[] output = new double[vocabularySize];
        for (int hiddenLayerIndex = 0; hiddenLayerIndex < hiddenLayer.length; hiddenLayerIndex++) {
            for (int outputIndex = 0; outputIndex < output.length; outputIndex++) {
                output[outputIndex] += hiddenLayer[hiddenLayerIndex] * outputWeights[hiddenLayerIndex][outputIndex];
            }
        }
        return output;
    }

    private double[] calculateHiddenLayer(LinkedList<String> context) {
        double[] hiddenLayer = new double[featureSpaceSize];
        int contextWordIndex = 0;
        for (String contextWord : context) {
            if (contextWordIndex == contextMidIndex) {
                continue;
            }
            int contextWordKey = vocabulary.add(contextWord);
            for (int hiddenLayerIndex = 0; hiddenLayerIndex < featureSpaceSize; hiddenLayerIndex++) {
                hiddenLayer[hiddenLayerIndex] += inputWeights[contextWordKey][hiddenLayerIndex];
            }
            contextWordIndex++;
        }
        return hiddenLayer;
    }

    private void correctWeights(double[] prediction, int targetWordKey, double[] hiddenLayer) {
        double[] outputErrors = calculateOutputErrors(prediction, targetWordKey);
        //System.out.println(Arrays.stream(outputErrors).sum());
        correctOutputWeights(outputErrors, hiddenLayer);
        correctInputWeights(outputErrors);
    }

    private void correctInputWeights(double[] outputErrors) {
        double[] inputErrors = new double[featureSpaceSize];
        for (int hiddenLayerIndex = 0; hiddenLayerIndex < featureSpaceSize; hiddenLayerIndex++) {
            for (int outputIndex = 0; outputIndex < vocabularySize; outputIndex++) {
                inputErrors[hiddenLayerIndex] += outputErrors[outputIndex] * outputWeights[hiddenLayerIndex][outputIndex];
            }
        }
        for (int hiddenLayerIndex = 0; hiddenLayerIndex < featureSpaceSize; hiddenLayerIndex++) {
            for (int inputIndex = 0; inputIndex < vocabularySize; inputIndex++) {
                inputWeights[inputIndex][hiddenLayerIndex] -= trainingCoef * inputErrors[hiddenLayerIndex];
            }
        }
    }

    private void correctOutputWeights(double[] outputErrors, double[] hiddenLayer) {
        for (int hiddenLayerIndex = 0; hiddenLayerIndex < featureSpaceSize; hiddenLayerIndex++) {
            for (int outputIndex = 0; outputIndex < vocabularySize; outputIndex++) {
                outputWeights[hiddenLayerIndex][outputIndex] -= trainingCoef * hiddenLayer[hiddenLayerIndex] * outputErrors[outputIndex];
            }
        }
    }

    private double[] calculateOutputErrors(double[] prediction, int targetWordKey) {
        return IntStream.range(0, vocabularySize)
                .mapToDouble(wordKey -> {
                            int expected = wordKey == targetWordKey ? 1 : 0;
                            double error = prediction[wordKey] - expected;
                            return Math.abs(error) > 0.001 ? error : 0;
                        }
                ).toArray();
    }

    private double[] softMax(double[] input) {
        double sum = Arrays.stream(input).map(Math::exp).sum();
        return Arrays.stream(input).map(Math::exp).map((number) -> number / sum).toArray();
    }

    private void initContext(LinkedList<String> context, Iterator<String> simpleTextIterator) {
        for (int i = 0; i < contextSize; i++) {
            if (!simpleTextIterator.hasNext()) {
                throw new RuntimeException("Data set is too small");
            }
            String word = simpleTextIterator.next();
            vocabulary.add(word);
            context.addLast(word);
        }
    }

    private void updateContext(LinkedList<String> context, String word) {
        vocabulary.add(word);
        context.addLast(word);
        context.removeFirst();
    }
}
