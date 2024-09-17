


import java.io.*;
import java.util.*;

public class RRT{
    HashSet<Pair> blocked;
    private Node start_node;
    private Node end_node;
    int width;
    int height;

    List<Node> allnodes;

    Random random = new Random();

    double stepsize;

    public RRT(){
        end_node= new Node();
        blocked= new HashSet<>();
        start_node=new Node();
        width=0;
        height=0;
        stepsize=.05;
        allnodes=new ArrayList<>();

    }


    boolean collisioncheck(double x, double y){
        for(Pair p: blocked){
            //(x >= p.first - 0.002 && x <= p.first + 0.002) && (y >= p.second - 0.002 && y <= p.second + 0.002)
            if((x >= p.first - .15 && x <= p.first + .15) && (y >= p.second - .15 && y <= p.second + .15) ){

                return true;
            }
        }


        return false;
    }

    Boolean checkpath(Node nearest_node, Node sample_node){
        Node next= new Node();

        double curx=nearest_node.p.first;
        double cury=nearest_node.p.second;



        while(!(curx <= sample_node.p.first +0.02 && curx >= sample_node.p.first -0.02) && !(cury <= sample_node.p.second+0.02 && cury>= sample_node.p.second-0.02)){


            double dx = sample_node.p.first - curx;
            double dy = sample_node.p.second - cury;


            double distance = Math.sqrt(dx * dx + dy * dy);


            double unitX = dx / distance;
            double unitY = dy / distance;


            curx += 0.05 * unitX;
            cury += 0.05 * unitY;

            if(collisioncheck(curx,cury)){

                return false;
            }

        }




        return true;
    }
    Node nearest(Node sample_node){
        double minDistance = Double.MAX_VALUE;
        Node nearestNode = null;

        for(Node node : allnodes){

            double distance = Math.sqrt(Math.pow((sample_node.p.first - node.p.first), 2) + Math.pow((sample_node.p.second - node.p.second), 2));




            if(distance < minDistance){
                minDistance = distance;
                nearestNode = node;
            }
        }
//System.out.println(nearestNode.toString()+"\n\n\n new entry\n\n");

        return nearestNode;
    }
    Node sample(double bias) {
        if (random.nextDouble() < bias) {

            return end_node;
        } else {

            double x = random.nextDouble() * width;
            double y = random.nextDouble() * height;
            x=Math.round(x*100);
            x=x/100;
            y=Math.round(y*100);
            y=y/100;
            return new Node(x, y);
        }
    }

    Boolean goalcheck(Node n){
        if((n.p.first<=end_node.p.first+0.1 && n.p.first>= end_node.p.first-0.1) &&
                (n.p.second<=end_node.p.second+0.1 && n.p.second>= end_node.p.second -0.1)){
            return true;
        }
        return false;
    }

    Node runrrt(){
        Node random_node= null;
        Node nearest_node=null;

        while(true){
            random_node=sample(0);
            nearest_node=nearest(random_node);

            if(checkpath(nearest_node,random_node)){

                random_node.parent=nearest_node;
                allnodes.add(random_node);
                if(goalcheck(random_node)){

                    break;
                }
            }



        }
        return random_node;
    }
    public double calculatePathLength(Node goalNode) {
        double totalLength = 0.0;
        Node current = goalNode;

        // Traverse from the goal node back to the start node, summing the distances
        while (current != null && current.parent != null) {
            double dx = current.p.first - current.parent.p.first;
            double dy = current.p.second - current.parent.p.second;
            totalLength += Math.sqrt(dx * dx + dy * dy);
            current = current.parent;
        }

        return totalLength;
    }



    public  StringBuilder buildpath(Node n) {
        StringBuilder output = new StringBuilder();
        Node curnode = n;
        int pathlength = 0;


        while (curnode != null) {
            output.insert(0,curnode + "\n");
            curnode = curnode.parent;
            pathlength++;
        }
        output.append(allnodes.size()+"\n");
        for(Node p: allnodes){
            if(p.parent==null){
                output.append(p.toString()+" "+ p.toString()+"\n");
                continue;
            }
            output.append(p.toedge()+"\n");
        }


        output.insert(0, pathlength  + "\n");

        return output;
    }




    public static void main(String[] args){
        RRT rrt = new RRT();

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
            rrt.start_node = new Node(startX, startY);

            double endX = Double.parseDouble(reader.readLine());
            double endY = Double.parseDouble(reader.readLine());
            double endTheta = Double.parseDouble(reader.readLine());
            rrt.end_node = new Node(endX, endY);

            rrt.allnodes.add(rrt.start_node);
            List<Double> nodeCounts = new ArrayList<>();
            for (int i = 0; i < 10; i++) { // Run RRT 100 times
                rrt.allnodes.clear();
                rrt.allnodes.add(rrt.start_node);
                Node goal = rrt.runrrt();
                if (goal != null) {
                    nodeCounts.add(rrt.calculatePathLength(goal));
                }
            }

            if (!nodeCounts.isEmpty()) {
                double averageNode2Count = nodeCounts.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                System.out.println("Average length over" + nodeCounts.size() + " runs: " + averageNode2Count);
            }

        } catch (IOException e) {
            System.err.println("Error processing input.");
            e.printStackTrace();
        }
    }
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
        if(!( obj instanceof Pair)){
            return false;
        }
        Pair other= (Pair) obj;
        return this.first==other.first && this.second== other.second;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
