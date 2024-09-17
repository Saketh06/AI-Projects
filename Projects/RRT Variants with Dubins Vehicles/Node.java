public class Node {
    Pair p;
    Node parent;
    double theta;

    Node(double x, double y) {
        p = new Pair(x, y);
        this.parent = null;
    }

    Node(double x, double y, double theta) {
        this.p = new Pair(x, y);
        this.theta = theta;
        this.parent = null;
    }


    Node() {
        p = new Pair(0, 0);
        parent = null;
    }

    @Override
    public String toString() {
        return p.first + " " + p.second;
    }

    public String toedge() {
        return p.first + " " + p.second + " " + parent.p.first + " " + parent.p.second;
    }
}
