import java.util.*;

public class Classifier {
static Integer numattributes;
    static class Instance {
        List<Integer> attributes;
        int label;

        public Instance(List<Integer> attributes, int label) {
            this.attributes = attributes;
            this.label = label;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Label: ").append(label).append(", Attributes: ").append(attributes);
            return sb.toString();
        }
    }

    static class TestInstance {
        List<Integer> attributes;

        public TestInstance(List<Integer> attributes) {
            this.attributes = attributes;
        }

        @Override
        public String toString() {
            return attributes.toString();
        }
    }

    public static void main(String[] args) {
        String algorithm = args[0];
        Scanner scanner = new Scanner(System.in);

        String headerLine = scanner.nextLine().trim();
        String[] headerParts = headerLine.split(" ");
        int numAttributes = Integer.parseInt(headerParts[0]);
        numattributes=numAttributes;
        int numValues = Integer.parseInt(headerParts[2]);
        int numClasses = Integer.parseInt(headerParts[4]);



        List<Instance> trainingData = new ArrayList<>();
        boolean training = true;
        scanner.nextLine();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.equals("-- test --")) {
                training = false;
                break;
            }

            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split(" ");
            List<Integer> attributes = new ArrayList<>();

            for (int i = 1; i < parts.length; i++) {
                attributes.add(Integer.parseInt(parts[i]));
            }
            int label = Integer.parseInt(parts[0]);
            trainingData.add(new Instance(attributes, label));
        }

        if (algorithm.equals("linear")) {
            initializeLinearRegression(trainingData);
        }


        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                System.out.println("got");
                continue;

            }

            String[] parts = line.split(" ");
            List<Integer> attributes = new ArrayList<>();
            for (String part : parts) {
                attributes.add(Integer.parseInt(part));
            }
            TestInstance testInstance = new TestInstance(attributes);

            if (algorithm.equals("knn")) {

                knn(trainingData, testInstance, 500, numClasses);
            } else if (algorithm.equals("linear")) {

                linearRegression(testInstance);
                System.out.flush();
            }

        }

        scanner.close();
    }

    static double[] weights;
    static double learningRate = 0.01;

    static void initializeLinearRegression(List<Instance> trainingData) {
        weights = new double[numattributes];

        for (Instance instance : trainingData) {
            List<Integer> attributes = instance.attributes;
            double predicted = predict(attributes);
            double error = instance.label - predicted;
            for (int i = 0; i < weights.length; i++) {
                weights[i] += learningRate * error * attributes.get(i);
            }
        }
    }

    static void linearRegression(TestInstance testInstance) {
        double prediction = predict(testInstance.attributes);
        int predictedLabel = (int) Math.round(prediction);
        double confidence = 1.0 / (1 + Math.exp(-prediction));

        System.out.println(+ predictedLabel + " " + String.format("%.2f", confidence));
        System.out.flush();

    }

    static double predict(List<Integer> attributes) {


        double sum = 0;
        for (int i = 0; i < attributes.size(); i++) {
            sum += weights[i] * attributes.get(i);
        }
        return sum;
    }

    static void knn(List<Instance> trainingData, TestInstance testInstance, int k, int numClasses) {


            PriorityQueue<DistanceLabelPair> pq = new PriorityQueue<>(Comparator.comparingDouble(DistanceLabelPair::getDistance));

            for (Instance trainingInstance : trainingData) {
                double distance = distance(trainingInstance.attributes, testInstance.attributes);
                pq.add(new DistanceLabelPair(distance, trainingInstance.label));
                if (pq.size() > k) {
                    pq.poll();
                }
            }


            int[] classCounts = new int[numClasses];
            for (DistanceLabelPair pair : pq) {
                classCounts[pair.getLabel()]++;
            }

            int maxCount = -1;
            int predictedLabel = -1;
            for (int i = 0; i < numClasses; i++) {

                if (classCounts[i] >= maxCount) {
                    if (classCounts[i] > maxCount || maxCount == -1) {
                        maxCount = classCounts[i];
                        predictedLabel = i;
                    } else if (pq.peek() != null && pq.peek().getLabel() == i) {
                        maxCount = classCounts[i];
                        predictedLabel = i;
                    }
                }
            }


            double confidence = (double) maxCount / k;


            System.out.println( + predictedLabel + " " + confidence);
        System.out.flush();

    }

    static class DistanceLabelPair {
        private double distance;
        private int label;

        @Override
        public String toString() {
            return "Distance: " + distance + ", Label: " + label;
        }

        public DistanceLabelPair(double distance, int label) {
            this.distance = distance;
            this.label = label;
        }

        public double getDistance() {
            return distance;
        }

        public int getLabel() {
            return label;
        }
    }

    static double distance(List<Integer> attributes1, List<Integer> attributes2) {
        double sum = 0;
        for (int i = 0; i < attributes1.size(); i++) {
            double diff = attributes1.get(i) - attributes2.get(i);
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

}
