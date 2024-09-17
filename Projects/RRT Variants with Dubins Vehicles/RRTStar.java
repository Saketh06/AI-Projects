import java.io.*;
import java.util.*;


public class RRTStar {
    HashSet<Pair> blocked;
    private Node start_node;
    private Node end_node;
    int width;
    int height;

    List<Node> allnodes;
    Random random = new Random();

    double stepsize = 0.05;
    double rho = 0.20;
    double searchRadius = 1.0;

    public RRTStar() {
        end_node = new Node();
        blocked = new HashSet<>();
        start_node = new Node();
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
            if (Math.sqrt(Math.pow((p.first-x),2)+Math.pow(p.second-y,2))<1.2) {
                return true;
            }
        }
        return false;
    }

    boolean checkDubinsPathCollision(Node start, Node end) {
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

    List<Node> near(Node sample_node) {
        List<Node> neighbors = new ArrayList<>();
        for (Node node : allnodes) {
            double distance = Math.sqrt(Math.pow((sample_node.p.first - node.p.first), 2) + Math.pow((sample_node.p.second - node.p.second), 2));
            if (distance < searchRadius) {
                neighbors.add(node);
            }
        }
        return neighbors;
    }

    double cost(Node from, Node to) {
        return Math.sqrt(Math.pow((to.p.first - from.p.first), 2) + Math.pow((to.p.second - from.p.second), 2));
    }

    Node runRRT() {
        boolean goalFound = false;
        Node finalGoalNode = null;
        allnodes.add(start_node);
        int additionalSamples = 0;

        long startTime = System.nanoTime();

        while (true) {
            Node random_node = sample(0.081);
            Node nearest_node = nearest(random_node);

            if (checkDubinsPathCollision(nearest_node, random_node)) {
                List<Node> neighbors = near(random_node);
                Node min_node = nearest_node;
                double min_cost = cost(start_node, nearest_node) + cost(nearest_node, random_node);

                for (Node neighbor : neighbors) {
                    double c = cost(start_node, neighbor) + cost(neighbor, random_node);
                    if (c < min_cost && checkDubinsPathCollision(neighbor, random_node)) {
                        min_node = neighbor;
                        min_cost = c;
                    }
                }

                random_node.parent = min_node;
                allnodes.add(random_node);

                for (Node neighbor : neighbors) {
                    double potential_cost = min_cost + cost(random_node, neighbor);
                    if (potential_cost < cost(start_node, neighbor) && checkDubinsPathCollision(random_node, neighbor)) {
                        neighbor.parent = random_node;
                    }
                }

                if (!goalFound && goalCheck(random_node)) {
                    goalFound = true;
                    finalGoalNode = random_node;
                }

                if (goalFound) {
                    additionalSamples++;
                    if (additionalSamples > 0) {
                        break;
                    }
                }
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000;


        return finalGoalNode;
    }


    Node nearest(Node sample_node) {
        double minDistance = Double.MAX_VALUE;
        Node nearestNode = null;
        for (Node node : allnodes) {
            double distance = Math.sqrt(Math.pow((sample_node.p.first - node.p.first), 2) + Math.pow((sample_node.p.second - node.p.second), 2));
            if (distance < minDistance) {
                minDistance = distance;
                nearestNode = node;
            }
        }
        return nearestNode;
    }

    Node sample(double bias) {
        if (random.nextDouble() < bias) {
            return new Node(end_node.p.first, end_node.p.second, end_node.theta);
        } else {
            double x = random.nextDouble() * width;
            double y = random.nextDouble() * height;
            double theta = random.nextDouble() * 2 * Math.PI;
            return new Node(x, y, theta);
        }
    }

    Boolean goalCheck(Node n) {
        return Math.abs(n.p.first - end_node.p.first) <= 0.1 &&
                Math.abs(n.p.second - end_node.p.second) <= 0.1;
    }


    public StringBuilder buildPath(Node n) {


        StringBuilder output = new StringBuilder();
        List<String> pathDetails = new ArrayList<>();
        Node current = n;


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
        RRTStar rrt = new RRTStar();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            rrt.width = Integer.parseInt(reader.readLine());
            rrt.height = Integer.parseInt(reader.readLine());

            String line;
            int j = rrt.height;
            while (j > 0 && (line = reader.readLine()) != null) {
                for (int i = 0; i < line.length(); i++) {
                    if (line.charAt(i) == '#') {
                        rrt.blocked.add(new Pair(i + 1, j));
                    }
                }
                j--;
            }
            double startX = Double.parseDouble(reader.readLine());
            double startY = Double.parseDouble(reader.readLine());
            double startTheta = Double.parseDouble(reader.readLine());
            rrt.start_node = new Node(startX, startY);

            double endX = Double.parseDouble(reader.readLine());
            double endY = Double.parseDouble(reader.readLine());
            double endTheta = Double.parseDouble(reader.readLine());
            rrt.end_node = new Node(endX, endY);

            rrt.allnodes.add(rrt.start_node);

            Node goal= rrt.runRRT();


            String s=rrt.buildPath(goal).toString();
            System.out.println(s);


        } catch (IOException e) {
            System.err.println("Error processing input.");
            e.printStackTrace();
        }





        class Pair {
            double first;
            double second;

            public Pair(double first, double second) {
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
    }
}

