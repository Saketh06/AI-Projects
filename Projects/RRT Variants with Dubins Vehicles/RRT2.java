
import java.io.*;
import java.util.*;



public class RRT2 {
    HashSet<Pair> blocked;
    private Node2 start_node;
    private Node2 end_node;
    int width;
    int height;

    List<Node2> allnodes;
    Random random = new Random();

    double stepsize = 0.05;
    double rho = 0.20;
    public RRT2() {
        end_node = new Node2();
        blocked = new HashSet<>();
        start_node = new Node2();
        width = 0;
        height = 0;
        allnodes = new ArrayList<>();
    }

    boolean collisionCheck(double x, double y) {
        if (x < 0 || x > width || y < 0 || y > height) {
            return true;
        }
        for (Pair p : blocked) {
          //  System.out.println(p+" "+x+","+y+"   "+Math.sqrt(Math.pow((p.first-x),2)+Math.pow(p.second-y,2)));
            if (Math.sqrt(Math.pow((p.first-x),2)+Math.pow(p.second-y,2))<.15) {
                return true;
            }
        }
        return false;
    }

    boolean checkDubinsPathCollision(Node2 start, Node2 end) {
        DubinsPathCalculator calculator = new DubinsPathCalculator(rho, stepsize);
        double[][] path = calculator.dubinsPath(
                new double[]{start.p.first, start.p.second, start.theta},
                new double[]{end.p.first, end.p.second, end.theta}
        );

        for (double[] point : path) {
            if (collisionCheck(point[0], point[1])) {
                return false;
            }
        }
        return true;
    }

    Node2 nearest(Node2 sample_node) {
        double minDistance = Double.MAX_VALUE;
        Node2 nearestNode2 = null;
        for (Node2 node : allnodes) {
            double distance = Math.sqrt(Math.pow((sample_node.p.first - node.p.first), 2) + Math.pow((sample_node.p.second - node.p.second), 2));
            if (distance < minDistance) {
                minDistance = distance;
                nearestNode2 = node;
            }
        }
        return nearestNode2;
    }

    Node2 sample(double bias) {
        if (random.nextDouble() < bias) {
            return new Node2(end_node.p.first, end_node.p.second, end_node.theta);
        } else {

            double x = random.nextDouble() * width;
            double y = random.nextDouble() * height;

            double theta = random.nextDouble() * 2 * Math.PI;

            while(collisionCheck(x,y)){
                x = random.nextDouble() * width;
                y = random.nextDouble() * height;
            }
            return new Node2(x, y, theta);
        }
    }

    Boolean goalCheck(Node2 n) {
        return Math.abs(n.p.first - end_node.p.first) <= 0.1 &&
                Math.abs(n.p.second - end_node.p.second) <= 0.1;
    }

    Node2 runRRT() {
        Node2 random_node;
        Node2 nearest_node;

        while (true) {
            random_node = sample(0.05);
            nearest_node = nearest(random_node);
            if (checkDubinsPathCollision(nearest_node, random_node)) {
                random_node.parent = nearest_node;
                allnodes.add(random_node);
                if (goalCheck(random_node)) {
                    break;
                }
            }
        }
        return random_node;
    }



    public StringBuilder buildPath(Node2 n) {

        StringBuilder output = new StringBuilder();
        List<String> pathDetails = new ArrayList<>();
        Node2 current = n;


        DubinsPathCalculator calculator = new DubinsPathCalculator(rho, stepsize);

        while (current.parent != null) {

            double[][] path = calculator.dubinsPath(
                    new double[]{current.parent.p.first, current.parent.p.second, current.parent.theta},
                    new double[]{current.p.first, current.p.second, current.theta}
            );


            for (double[] point : path) {
                pathDetails.add(String.format("%.2f %.2f", point[0], point[1]));
            }
            current = current.parent;
        }

        Collections.reverse(pathDetails);
        output.append(pathDetails.size() + "\n");
        for (String detail : pathDetails) {
            output.append(detail + "\n");
        }

        return output;
    }

    public static void main(String[] args) {
        RRT2 rrt = new RRT2();


        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            rrt.width = Integer.parseInt(reader.readLine());
            rrt.height = Integer.parseInt(reader.readLine());

            String line;
            int j = rrt.height;
            while ( j>0 && (line = reader.readLine()) != null) {
                for (int i = 0; i < line.length(); i++) {
                    if (line.charAt(i) == '#') {
                        rrt.blocked.add(new Pair(i+1, j));
                    }
                }
                j--;
            }

            double startX = Double.parseDouble(reader.readLine());
            double startY = Double.parseDouble(reader.readLine());
            double startTheta = Double.parseDouble(reader.readLine());
            rrt.start_node = new Node2(startX, startY, startTheta);

            double endX = Double.parseDouble(reader.readLine());
            double endY = Double.parseDouble(reader.readLine());
            double endTheta = Double.parseDouble(reader.readLine());
            rrt.end_node = new Node2(endX, endY, endTheta);

            rrt.allnodes.add(rrt.start_node);
            List<Double> pathLengths = new ArrayList<>();
            List<Long> runtimes = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                long startRunTime = System.nanoTime();
                Node2 goal = rrt.runRRT();
                long endRunTime = System.nanoTime();
                long runTime = (endRunTime - startRunTime) / 1_000_000;
                runtimes.add(runTime);


            }

            if (!pathLengths.isEmpty()) {
                double averagePathLength = pathLengths.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                System.out.println("Average path length over " + pathLengths.size() + " runs: " + averagePathLength);
            }


        } catch (IOException e) {
            System.err.println("Error processing input.");
            e.printStackTrace();
        }
    }

}


class Node2 {
    Pair p;
    double theta;
    Node2 parent;

    Node2(double x, double y, double theta) {
        this.p = new Pair(x, y);
        this.theta = theta;
        this.parent = null;
    }

    Node2() {
        this.p = new Pair(0,0);
        this.theta = 0.0;
        this.parent = null;
    }

    @Override
    public String toString() {
        return String.format("(%.2f, %.2f)", p.first, p.second);
    }
}

class Pair2 {
    double first;
    double second;

    public Pair2(double first, double second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pair)) {
            return false;
        }
        Pair other = (Pair) obj;
        return this.first == other.first && this.second == other.second;
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}

