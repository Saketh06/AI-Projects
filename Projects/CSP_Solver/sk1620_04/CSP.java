package sk1620_04;

import java.io.*;
import java.util.*;

public class CSP {
    int colors_num = 0;
    String algo;
    int numVertices;
    int numEdges;

    ArrayList<Node> nodes = new ArrayList<>();
    int branchingNodesExplored = 0;

    public static void main(String[] args) {
        CSP csp = new CSP();



        csp.colors_num = Integer.parseInt(args[1]);
        csp.algo = args[0];

        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("p edge")) {
                    String[] parts = line.split("\\s+");
                    csp.numVertices = Integer.parseInt(parts[2]);
                    csp.numEdges = Integer.parseInt(parts[3]);

                    for (int i = 1; i <= csp.numVertices; i++) {
                        csp.nodes.add(new Node(i, csp.colors_num));
                    }
                } else if (line.startsWith("e")) {
                    String[] parts = line.split("\\s+");


                    int v1 = Integer.parseInt(parts[1]);
                    int v2 = Integer.parseInt(parts[2]);

                    if (v1 == v2) {
                        continue;
                    }

                    csp.nodes.get(v1 - 1).neighbors.add(v2);

                    csp.nodes.get(v2 - 1).neighbors.add(v1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean solutionFound = false;

        if (csp.algo.equals("dfs")) {
            solutionFound = csp.dfsbacktracksearch();
        }

        else if (csp.algo.equals("fc")) {
            solutionFound = csp.fcbacktracksearch();
        }

        else if (csp.algo.equals("mcv")) {
            solutionFound = csp.mcvbacktracksearch();
        }

        if (solutionFound) {
            csp.printSolution();
        } else {
            System.out.println("No solution");
        }

        System.out.println(csp.branchingNodesExplored + " branching nodes explored.");
    }

    public boolean dfsbacktracksearch() {
        if (numVertices == 0) {
            System.out.println("No vertices to color.");
            return false;
        }

        return dfsbacktrack(0);
    }

    public boolean fcbacktracksearch() {
        if (numVertices == 0) {
            System.out.println("No vertices to color.");
            return false;
        }

        return fcbacktrack(0);
    }

    public boolean mcvbacktracksearch() {
        if (numVertices == 0) {
            System.out.println("No vertices to color.");
            return false;
        }

        return mcvbacktrack(0);
    }

    private boolean dfsbacktrack(int vertexIndex) {
        branchingNodesExplored++;
        if (vertexIndex == numVertices) {
            return true;
        }

        Node currentVertex = nodes.get(vertexIndex);

        for (int color = 1; color <= colors_num; color++) {
            if (isSafe(currentVertex, color)) {
                currentVertex.color = color;
                if (dfsbacktrack(vertexIndex + 1)) {
                    return true;
                }

                currentVertex.color = 0;
            }
        }
        return false;
    }

    private boolean fcbacktrack(int vertexIndex) {
        branchingNodesExplored++;
        if (vertexIndex == numVertices) {
            return true;
        }

        Node currentVertex = nodes.get(vertexIndex);

        for (int color = 1; color <= colors_num; color++) {
            if (isSafe(currentVertex, color)) {
                currentVertex.color = color;
                if (forwardCheck(currentVertex, color) && fcbacktrack(vertexIndex + 1)) {
                    return true;
                }

                currentVertex.color = 0;
            }
        }
        return false;
    }

    private boolean mcvbacktrack(int vertexIndex) {
        branchingNodesExplored++;
        if (vertexIndex == numVertices) {
            return true;
        }


        List<Node> minvertlist = minvert();


        Node currentVertex = minvertlist.get(new Random().nextInt(minvertlist.size()));


        for (int color = 1; color <= colors_num; color++) {
            if (isSafe(currentVertex, color)) {
                currentVertex.color = color;
                if (forwardCheck(currentVertex, color) && mcvbacktrack(vertexIndex + 1)) {
                    return true;
                }

                currentVertex.color = 0;
            }
        }
        return false;
    }

    private List<Node> minvert() {
        List<Node> minvertlist = new ArrayList<>();
        int minDomainSize = Integer.MAX_VALUE;
        for (Node vertex : nodes) {
            if (vertex.color == 0 && vertex.neighbors.size() < minDomainSize) {
                minvertlist.clear();
                minvertlist.add(vertex);


                minDomainSize = vertex.neighbors.size();
            } else if (vertex.color == 0 && vertex.neighbors.size() == minDomainSize) {
                minvertlist.add(vertex);
            }
        }

        return minvertlist;
    }

    private boolean isSafe(Node vertex, int color) {
        for (int neighbor : vertex.neighbors) {
            if (nodes.get(findIndex(nodes,neighbor) ).color == color) {
                return false;
            }
        }
        return true;
    }

    private boolean forwardCheck(Node vertex, int color) {

        for (int neighborIndex : vertex.neighbors) {

            Node neighborNode = nodes.get(findIndex(nodes,neighborIndex));
            if (neighborNode.color == 0) {

                neighborNode.domain.remove(Integer.valueOf(vertex.color));

            }
        }
        return true;
    }

    private int findIndex(ArrayList<Node> list, int value) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).vertice==value) {
                return i;
            }
        }
        return -1;
    }

    private void printSolution() {

        System.out.println("s col " + colors_num);

        for (Node node : nodes) {
            System.out.println("l " + node.vertice + " " + node.color);
        }
    }
}
class Node {
    int vertice;
    int color;
    ArrayList<Integer> neighbors = new ArrayList<>();
    ArrayList<Integer> domain = new ArrayList<>();

    Node(int vertice, int colors_num) {
        this.vertice = vertice;
        this.color = 0;
        for (int i = 1; i <= colors_num; i++) {
            domain.add(i);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(!( obj instanceof Node)){
            return false;
        }
        Node other= (Node) obj;

        return this.vertice==other.vertice;
    }

    @Override
    public String toString() {
        return "(" + vertice + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(vertice);
    }
}