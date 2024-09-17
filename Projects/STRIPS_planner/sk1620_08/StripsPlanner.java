package sk1620_08;

import java.util.*;

public class StripsPlanner {
    private int nodesGenerated = 0;
    private int nodesExpanded = 0;

    private PriorityQueue<Node> openList;
    private Set<String> closedList;
    private List<Action> actions;
    private List<String> constants;

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Expected arguments: <weight> <heuristic>");
            return;
        }
        double weight = Double.parseDouble(args[0]);
        String heuristic = args[1];

        Scanner scanner = new Scanner(System.in);


        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.startsWith("predicates:")) {
                break;
            }
        }


        String constantsLine = "";
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.startsWith("constants:")) {
                constantsLine = line.replace("constants: ", "");
                break;
            }
        }

        StripsPlanner planner = new StripsPlanner();
        planner.parseConstants(constantsLine);


        int actionsCount = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.matches("\\d+ actions")) {
                try {
                    actionsCount = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                    break;
                } catch (NumberFormatException e) {
                    System.err.println("Invalid format for actions count.");
                    return;
                }
            }
        }

        planner.parseActions(scanner, actionsCount);


        String line;
        do {
            line = scanner.nextLine().trim();
        } while (!line.startsWith("initial:"));


        Set<Predicate> initialState = planner.parseState(line.replace("initial: ", ""));

        do {
            line = scanner.nextLine().trim();
            if (!line.isEmpty() && !line.startsWith("#") && line.startsWith("goal:")) {
                break;
            }
        } while (scanner.hasNextLine());


        Set<Predicate> goalState = planner.parseState(line.replace("goal: ", ""));

        Optional<List<Action>> plan = planner.plan(initialState, goalState, weight, heuristic);

        plan.ifPresentOrElse(
                p -> {
                    for (int i = 0; i < p.size(); i++) {
                        System.out.println(i + " " + p.get(i).name);
                    }
                },
                () -> System.out.println("No plan found.")
        );
    }

    public StripsPlanner() {
        openList = new PriorityQueue<>();
        closedList = new HashSet<>();
        actions = new ArrayList<>();
        constants = new ArrayList<>();
    }

    private void parseActions(Scanner scanner, int actionsCount) {
        System.out.println("Starting to parse actions...");
        for (int i = 0; i < actionsCount; i++) {

            String actionName = scanner.nextLine().trim();
            while (actionName.isEmpty() || actionName.startsWith("#")) {
                actionName = scanner.nextLine().trim();
            }
            System.out.println("Parsing action: " + actionName);


            String preLine = readNextValidLine(scanner);
            List<Predicate> preconditions = parsePredicates(preLine.replace("pre: ", "").trim());

            String prenegLine = readNextValidLine(scanner);


            String delLine = readNextValidLine(scanner);
            List<Predicate> delEffects = parsePredicates(delLine.replace("del: ", "").trim());

            String addLine = readNextValidLine(scanner);
            List<Predicate> addEffects = parsePredicates(addLine.replace("add: ", "").trim());

            System.out.println("Preconditions: " + preconditions);
            System.out.println("Delete Effects: " + delEffects);
            System.out.println("Add Effects: " + addEffects);

            actions.add(new Action(actionName, preconditions, addEffects, delEffects));
        }
        System.out.println("Finished parsing actions.");
    }

    private String readNextValidLine(Scanner scanner) {
        String line = "";
        while (scanner.hasNextLine()) {
            line = scanner.nextLine().trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
                break;
            }
        }
        return line;
    }
    private Set<Predicate> parseState(String stateLine) {
        Set<Predicate> state = new HashSet<>();
        System.out.println("Parsing state line: " + stateLine);
        for (String part : stateLine.split("\\s+")) {
            if (part.isEmpty() || !part.contains("(") || !part.contains(")")) {
                System.out.println("Skipping part: " + part);
                continue;
            }

            String name = part.substring(0, part.indexOf('('));
            String paramsStr = part.substring(part.indexOf('(') + 1, part.indexOf(')'));
            List<String> parameters = paramsStr.isEmpty() ? Collections.emptyList() : Arrays.asList(paramsStr.split(","));
            state.add(new Predicate(name, parameters));
            System.out.println("Parsed predicate: " + name + " " + parameters);
        }
        return state;
    }

    private List<Predicate> parsePredicates(String line) {
        List<Predicate> predicates = new ArrayList<>();
        if (line.isEmpty()) return predicates;

        for (String part : line.split(" ")) {
            if (!part.contains("(")) {
                predicates.add(new Predicate(part, Collections.emptyList()));
                continue;
            }
            String[] splitPart = part.split("\\(");
            String name = splitPart[0];
            if (splitPart.length < 2) {
                predicates.add(new Predicate(name, Collections.emptyList()));
                continue;
            }
            String params = splitPart[1].replace(")", "");
            List<String> elements = Arrays.asList(params.split(","));
            predicates.add(new Predicate(name, elements));
        }
        return predicates;
    }

    private void parseConstants(String constantsLine) {
        constants = Arrays.asList(constantsLine.split("\\s+"));
        System.out.println("Constants: " + constants);
    }




    public Optional<List<Action>> plan(Set<Predicate> initialState, Set<Predicate> goalState, double weight, String heuristic) {

        nodesGenerated = 0;
        nodesExpanded = 0;

        Node startNode = new Node(initialState, null, null, 0, estimateHeuristic(initialState, goalState));
        openList.add(startNode);
        nodesGenerated++;

        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();
            nodesExpanded++;

            if (goalState.equals(currentNode.statePredicates)) {

                List<Action> plan = constructPlan(currentNode);
                System.out.println(nodesGenerated + " nodes generated");
                System.out.println(nodesExpanded + " nodes expanded");
                return Optional.of(plan);
            }

            closedList.add(currentNode.stateID());
System.out.println(constants);
            for (Action action : actions) {
                for (String constant : constants) {
                    if (action.isApplicable(currentNode.statePredicates)) {
                        Set<Predicate> newState = action.apply(currentNode.statePredicates);
                        if (!closedList.contains(newState.toString())) {
                            Node newNode = new Node(newState, currentNode, action, currentNode.g + 1, estimateHeuristic(newState, goalState));
                            openList.add(newNode);
                            nodesGenerated++;
                        }
                    }
                }
            }
        }


        System.out.println("No plan found.");
        return Optional.empty();
    }

    private double estimateHeuristic(Set<Predicate> state, Set<Predicate> goalState) {

        return 0;
    }

    private List<Action> constructPlan(Node goalNode) {
        LinkedList<Action> plan = new LinkedList<>();
        Node current = goalNode;
        while (current != null && current.action != null) {
            plan.addFirst(current.action);
            current = current.parent;
        }
        return plan;
    }

    static class Predicate {
        String name;
        List<String> parameters;

        public Predicate(String name, List<String> parameters) {
            this.name = name;
            this.parameters = parameters;
        }

        @Override
        public String toString() {
            return name + "(" + String.join(", ", parameters) + ")";
        }
    }

    static class Action {
        String name;
        List<Predicate> preconditions;
        List<Predicate> addEffects;
        List<Predicate> deleteEffects;

        @Override
        public String toString() {
            StringBuilder preconditionsStr = new StringBuilder();
            for (Predicate p : preconditions) {
                if (preconditionsStr.length() > 0) preconditionsStr.append(", ");
                preconditionsStr.append(p.toString());
            }

            StringBuilder addEffectsStr = new StringBuilder();
            for (Predicate p : addEffects) {
                if (addEffectsStr.length() > 0) addEffectsStr.append(", ");
                addEffectsStr.append(p.toString());
            }

            StringBuilder deleteEffectsStr = new StringBuilder();
            for (Predicate p : deleteEffects) {
                if (deleteEffectsStr.length() > 0) deleteEffectsStr.append(", ");
                deleteEffectsStr.append(p.toString());
            }

            return "Action[name=" + name + ", preconditions=[" + preconditionsStr +
                    "], addEffects=[" + addEffectsStr + "], deleteEffects=[" + deleteEffectsStr + "]]";
        }

        public Action(String name, List<Predicate> preconditions, List<Predicate> addEffects, List<Predicate> deleteEffects) {
            this.name = name;
            this.preconditions = preconditions;
            this.addEffects = addEffects;
            this.deleteEffects = deleteEffects;
        }

        public boolean isApplicable(Set<Predicate> currentState) {
            //tbd
         return false;
        }

        public Set<Predicate> apply(Set<Predicate> currentState) {
            Set<Predicate> newState = new HashSet<>(currentState);

            newState.removeAll(deleteEffects);

            newState.addAll(addEffects);
            return newState;
        }


    }

    static class Node implements Comparable<Node> {
        Set<Predicate> statePredicates;
        Node parent;
        Action action;
        double g;
        double f;

        public Node(Set<Predicate> statePredicates, Node parent, Action action, double g, double h) {
            this.statePredicates = statePredicates;
            this.parent = parent;
            this.action = action;
            this.g = g;
            this.f = g + h;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.f, other.f);
        }

        public String stateID() {
            List<String> parts = new ArrayList<>();
            for (Predicate p : statePredicates) {
                parts.add(p.toString());
            }
            Collections.sort(parts);
            return String.join(",", parts);
        }

        @Override
        public String toString() {
            StringBuilder statePredicatesStr = new StringBuilder();
            for (Predicate p : statePredicates) {
                if (statePredicatesStr.length() > 0) statePredicatesStr.append(", ");
                statePredicatesStr.append(p.toString());
            }

            return "Node[statePredicates=[" + statePredicatesStr + "], action=" +
                    (action != null ? action.name : "None") + ", g=" + g + ", f=" + f + "]";
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Node node = (Node) obj;
            return Objects.equals(statePredicates, node.statePredicates);
        }

        @Override
        public int hashCode() {
            return Objects.hash(statePredicates);
        }
    }
}
