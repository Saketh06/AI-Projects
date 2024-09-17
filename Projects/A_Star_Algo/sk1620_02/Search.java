import java.io.*;
import java.util.*;

public class Search{

    private Node start_node;
    PriorityQueue<Node> openlist;
    HashMap<State, Integer> closed_list;

    Stack<Node> dfs_list;

    Set<State> visited;



    static HashMap<Pair,Integer> blocked_cells=new HashMap<>();
    int generated;
    int expanded;
    public Search(Node start_node) {
        this.start_node = start_node;
        openlist= new PriorityQueue<>();
        closed_list= new HashMap<>();
        dfs_list=new Stack<>();
        visited=new HashSet<>();
        generated=0;
        expanded=0;


    }


    double h0(){
        return 0;
    }

    double h1(Node cur){

        double result=0;
        Pair robot=cur.state.map.get("@").get(0);
        for(Pair p : cur.state.map.get("*")){
            result+= 1;

        }
        return result;
    }
    double h2(Node cur){

        Pair robot=cur.state.map.get("@").get(0);
        double result=0;
        Pair p;

        for(int d=1; d<cur.state.map.get("*").size();d++){
            robot=cur.state.map.get("*").get(d-1);
            p=cur.state.map.get("*").get(d);


            result += 1+Math.abs(robot.first-p.first)+Math.abs(robot.second-p.second);
           for(Pair blck: blocked_cells.keySet()){
                if(((blck.first<=robot.first && blck.first>=p.first) ||(blck.first>=robot.first && blck.first<=p.first) )&&(blck.second==robot.second && blck.second==p.second)){
                    result+=4;
                }
                if(((blck.second<=robot.second && blck.second>=p.second) ||(blck.second>=robot.second && blck.second<=p.second) )&&(blck.first==robot.first && blck.first==p.first)){
                    result+=4;
                }
            }






        }
        return result;
    }



    public List<Node> exp_function(Node exp_node, String hopt){
        double hval=0;
        if(hopt.toLowerCase().equals("h0")){
            hval=h0();
        }
        if(hopt.toLowerCase().equals("h1")){
            hval=h1(exp_node);
        }
        if(hopt.toLowerCase().equals("h2")){
            hval=h2(exp_node);
        }
        List<Node> exp=new ArrayList<>();


        //north
        Node n_node = new Node(exp_node);
        n_node.state.north_function(blocked_cells);

        if(!(closed_list.containsKey(n_node.state))){
            closed_list.put(n_node.state,null);
            generated++;
            n_node.action='N';
            n_node.g+=1;
            n_node.g+=hval;
            n_node.parent=exp_node;
            exp.add(n_node);
            openlist.add(n_node);


        }
        //

        //west
        Node w_node = new Node(exp_node);
        w_node.state.west_function(blocked_cells);
        w_node.action='W';
        w_node.g+=1;
        w_node.g+=hval;
        w_node.parent=exp_node;

        if(!(closed_list.containsKey(w_node.state))){
            closed_list.put(w_node.state,null);
            generated++;
            exp.add(w_node);
            openlist.add(w_node);


        }
        //

        //south
        Node s_node = new Node(exp_node);
        s_node.state.south_function(blocked_cells);
        s_node.action='S';
        s_node.g+=1;
        s_node.g+=hval;
        s_node.parent=exp_node;

        if(!(closed_list.containsKey(s_node.state))){
            closed_list.put(s_node.state,null);
            generated++;
            exp.add(s_node);
            openlist.add(s_node);


        }
        //

        //east
        Node e_node = new Node(exp_node);
        e_node.state.east_function(blocked_cells);
        e_node.action='E';
        e_node.g+=1;
        e_node.g+=hval;
        e_node.parent=exp_node;

        if(!(closed_list.containsKey(e_node.state))){
            closed_list.put(e_node.state,null);
            generated++;
            exp.add(e_node);
            openlist.add(e_node);


        }
        //

        //vacuum
        Node v_node = new Node(exp_node);
        v_node.state.v_function();
        v_node.action='V';
        v_node.g+=1;
        v_node.g+=hval;
        v_node.parent=exp_node;
        generated++;
        if(!(closed_list.containsKey(v_node.state))){
            closed_list.put(v_node.state,null);
            generated++;
            exp.add(v_node);
            openlist.add(v_node);


        }
        //
        return exp;
    }

    public boolean goal_check( State test_state){
        if(!test_state.map.keySet().contains("*")){
            return true;
        }
        if(test_state.map.get("*").isEmpty()){
            return true;
        }
        return false;
    }

    public Node astar(Node start_node, String hopt){
        openlist.add(start_node);
        generated++;
        Node cur=new Node();
        while(openlist.isEmpty() == false){
            cur=openlist.remove();

            closed_list.put(cur.state,null);

            if(goal_check(cur.state)){
                return cur;
            }
            else{
                exp_function(cur,hopt);
                expanded++;
            }
        }
        return null;
    }

    public Node ucs(Node start_node){

                openlist.add(start_node);
                generated++;
                Node cur=new Node();
                while(openlist.isEmpty() == false){
                    cur=openlist.remove();

                    closed_list.put(cur.state,null);

                    if(goal_check(cur.state)){
                        return cur;
                    }
                    else{
                        exp_function(cur,"h0");
                        expanded++;
                    }
                }
                return null;
            }

    public StringBuilder buildpath(Node endnode, String outputPath){
        StringBuilder sb = new StringBuilder();
        Node curnode = new Node();
        curnode = endnode;


        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            while (curnode.action != null) {
                sb.append(curnode.action);
                curnode = curnode.parent;
            }
            sb.reverse();
            StringBuilder formatted = new StringBuilder();

            for (int i = 0; i < sb.length(); i++) {
                formatted.append(sb.charAt(i)).append("\n");
            }
            formatted.append(generated).append(" nodes generated").append("\n");
            formatted.append(expanded).append(" nodes expanded");

            // Writing to CSV file
            writer.write((endnode.state.width*endnode.state.height-blocked_cells.size())*
                    Math.pow(2,endnode.state.width*endnode.state.height-blocked_cells.size())+"\n");
             writer.write("\nGenerated,Expanded\n");
            writer.write(generated + "," + expanded + "\n");
            writer.flush();

            return formatted;

        } catch (IOException e) {
            System.out.println("Error writing to CSV file: " + e.getMessage());
            return null;
        }

    }





    public static void main(String[] args){
        State istate= new State();
        Node start_node=new Node();

        String line;


        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));
        try{
            start_node.state.width= Integer.parseInt(reader.readLine());
            start_node.state.height=  Integer.parseInt(reader.readLine());
        }
        catch (IOException e){
            System.out.println("illegal args");
        }

        try{
            int row=-1;
            while ( (line = reader.readLine()) != null ) {
                row++;
                for(int i=0; i<line.length(); i++){
                    if(line.charAt(i)=='@'){
                        //System.out.println(row);
                        addToMap(start_node.state.map,"@", new Pair(row,i));
                    }
                    if(line.charAt(i)=='*'){
                        addToMap(start_node.state.map,"*", new Pair(row,i));
                    }
                    if(line.charAt(i)=='#'){
                        blocked_cells.put(new Pair(row, i),0);
                    }
                }
            }
        }
        catch (IOException e){
            System.out.println("world generation error");
        }
        Search ucs= new Search(start_node);
        Node goal=null;





        if(args[0].toLowerCase().equals("uniform-cost")){
            goal=ucs.ucs(start_node);
        }
        if(args[0].toLowerCase().equals("depth-first")){
            goal=ucs.dfs(start_node);
        }
        if(args[0].toLowerCase().equals("a-star")){
            goal=ucs.astar(start_node,args[1]);
        }
        if(args[0].toLowerCase().equals("depth-first-id")){
            goal=ucs.iddfs(start_node);
        }



        String s=ucs.buildpath(goal,"/home/csu/sk1620/CS730AI/ASSN2/data.csv").toString();

        System.out.println(s);











    }

    public Node iddfs(Node start_node){


        Node cur = new Node();
        int d = 0;
        while (true) {
            d++;
            dfs_list.clear();
            visited.clear();
            generated=0;
            expanded=0;

            dfs_list.add(start_node);
            while (dfs_list.size() !=0) {
                cur = dfs_list.pop();

                expanded++;

                if (goal_check(cur.state)) {
                    return cur;
                }

                visited.add(cur.state);
                exp_function_dfs(cur,d);
            }




            }

        }




    public Node dfs(Node start_node){
        dfs_list.add(start_node);


        Node cur=new Node();

        while(dfs_list.isEmpty()==false){
            cur=dfs_list.pop();
            expanded++;
            if(goal_check(cur.state)){
                return cur;
            }

            visited.add(cur.state);
            exp_function_dfs(cur, Double.POSITIVE_INFINITY);

        }
        return null;
    }

    public void exp_function_dfs(Node exp_node,double c){



        //north
        Node n_node = new Node(exp_node);
        n_node.state.north_function(blocked_cells);
        n_node.action='N';
        n_node.g++;
        n_node.parent=exp_node;

        if(!(visited.contains(n_node.state))){

            if(n_node.g<=c){

            dfs_list.add(n_node);
            generated++;
        }

        }


        //south
        Node s_node = new Node(exp_node);
        s_node.state.south_function(blocked_cells);
        s_node.action='S';
        s_node.g++;
        s_node.parent=exp_node;

        if(!(visited.contains(s_node.state))){
            if(s_node.g<=c){

            dfs_list.add(s_node);
            generated++;
            }
        }
        //

        //east
        Node e_node = new Node(exp_node);
        e_node.state.east_function(blocked_cells);
        e_node.action='E';
        e_node.g++;
        e_node.parent=exp_node;

        if(!(visited.contains(e_node.state))){

            if(e_node.g<=c){


            dfs_list.add(e_node);
            generated++;
            }
        }

        //west
        Node w_node = new Node(exp_node);
        w_node.state.west_function(blocked_cells);
        w_node.action='W';
        w_node.g++;
        w_node.parent=exp_node;
        if(!(visited.contains(w_node.state))){

            if(w_node.g<=c){
            dfs_list.add(w_node);
            generated++;
            }
        }
        //

        //vacuum
        Node v_node = new Node(exp_node);
        v_node.state.v_function();
        v_node.action='V';
        v_node.g++;
        v_node.parent=exp_node;
        if(!(visited.contains(v_node.state))){

            if(v_node.g<=c){

            dfs_list.add(v_node);
            generated++;
            }
        }


    }



    private static void addToMap(Map<String, List<Pair>> map, String key, Pair pair) {
        map.computeIfAbsent(key, k -> new ArrayList<>()).add(pair);
    }




}
class Pair {
    int first;
    int second;

    public Pair(int first, int second) {
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

class Node implements Comparable<Node>{
    double g;
    State state;
    Node parent;
    Character action;

    public Node(){
        state=new State();
        g=0;
        parent= null;
        action= null;
    }

    public Node(Node copy) {
        this.g = copy.g;
        this.state = new State(copy.state);
        this.parent = copy.parent;
        this.action = copy.action;
    }

    public Node(State s, int gvalue, Node parent, Character action){
        state=s;
        g=gvalue;
        parent=parent;
        action=action;
    }

    @Override
    public int compareTo(Node other) {
        return Double.compare(this.g, other.g);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Node {");
        sb.append("g=").append(g).append(", ");
        sb.append("state=").append(state).append(", ");
        sb.append("parent=").append(parent).append(", ");
        sb.append("action=").append(action);
        sb.append("}");
        return sb.toString();
    }


    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof Node)) {
            return false;
        }
        Node other = (Node) obj;
        return this.state.equals(other.state);
    }



}





class State {
    public Map<String, List<Pair>> map;
    int width;
    int height;

    public State() {
        map = new HashMap<>();
        width = 0;
        height = 0;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("State {");
        sb.append("map=").append(map).append(", ");
        sb.append("width=").append(width).append(", ");
        sb.append("height=").append(height);
        sb.append("}");
        return sb.toString();
    }

    public void north_function(HashMap<Pair, Integer> blocked_cells) {

        if (this.map.get("@").get(0).first - 1 >= 0
                && !(blocked(blocked_cells, new Pair(this.map.get("@").get(0).first - 1, this.map.get("@").get(0).second)))) {
            this.map.get("@").get(0).first--;
            return;
        } else {
            return;
        }


    }

    public State(State copy) {
        this.width = copy.width;
        this.height = copy.height;
        this.map = new HashMap<>();


        for (Map.Entry<String, List<Pair>> entry : copy.map.entrySet()) {
            List<Pair> originalList = entry.getValue();
            List<Pair> copiedList = new ArrayList<>(originalList.size());

            for (Pair pair : originalList) {
                copiedList.add(new Pair(pair.first, pair.second));
            }

            this.map.put(entry.getKey(), copiedList);
        }
    }

    public void south_function(HashMap<Pair, Integer> blocked_cells) {
        if (this.map.get("@").get(0).first + 1 < this.height &&
                !(blocked(blocked_cells, new Pair(this.map.get("@").get(0).first + 1, this.map.get("@").get(0).second)))) {
            this.map.get("@").get(0).first++;
            return;
        } else {
            return;
        }
    }

    public boolean blocked(HashMap<Pair, Integer> blocked_cells, Pair move) {
        if (blocked_cells.isEmpty()) {
            return false;
        }
        for (Pair bl : blocked_cells.keySet()) {
            if (move.equals(bl)) {


                return true;
            }
        }
        return false;
    }

    public void west_function(HashMap<Pair, Integer> blocked_cells) {


        if (this.map.get("@").get(0).second - 1 >= 0 && !(blocked(blocked_cells, new Pair(this.map.get("@").get(0).first, this.map.get("@").get(0).second - 1)))) {
            this.map.get("@").get(0).second--;
            return;
        } else {
            return;
        }
    }

    public void east_function(HashMap<Pair, Integer> blocked_cells) {

        if (this.map.get("@").get(0).second + 1 < this.width && !(blocked(blocked_cells, new Pair(this.map.get("@").get(0).first, this.map.get("@").get(0).second + 1)))) {

            this.map.get("@").get(0).second++;
            return;

        } else {

            return;
        }
    }

    public void v_function() {
        if (this.map.get("*") == null) {
            return;
        }
        if (this.map.get("*").contains(this.map.get("@").get(0))) {
            this.map.get("*").remove(this.map.get("@").get(0));
            return;
        } else {
            return;
        }
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return width == state.width && height == state.height && this.map.equals(state.map);
    }

    @Override
    public int hashCode() {
        int result = 17;

        for (Map.Entry<String, List<Pair>> entry : map.entrySet()) {
            result = 31 * result + entry.getKey().hashCode();
            for (Pair p : entry.getValue()) {
                result = 31 * result + p.first + p.second;
            }
        }

        return result;
    }
}