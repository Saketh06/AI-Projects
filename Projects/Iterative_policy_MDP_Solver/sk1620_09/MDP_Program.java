import java.util.ArrayList;
import java.util.Scanner;

public class MDP_Program {
    public static void main(String[] args) {
        String algorithm = args[0];
        float discountFactor = Float.parseFloat(args[1]);
        float terminationCriterion = Float.parseFloat(args[2]);

        MDP mdp = parser();

        if ("vi".equals(algorithm)) {
            ValueIteration vi = new ValueIteration(mdp, discountFactor, terminationCriterion);
            vi.solve();
        } else {
            System.out.println("Unsupported algorithm.");
        }
    }

    private static MDP parser() {
        Scanner scanner = new Scanner(System.in);
        MDP mdp = new MDP();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.startsWith("number of states")) {

            } else if (line.startsWith("start state")) {
                mdp.startState = Integer.parseInt(line.split(": ")[1]);
                break;
            }
        }

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] parts = line.split(" ");
            if (parts.length < 3) continue;

            float reward = Float.parseFloat(parts[0]);
            boolean isTerminal = Integer.parseInt(parts[1]) == 1;
            State state = new State(reward, isTerminal);

            int numActions = Integer.parseInt(parts[2]);
            for (int a = 0; a < numActions; a++) {
                line = scanner.nextLine().trim();
                parts = line.split(" ");
                Action action = new Action();

                int numTransitions = Integer.parseInt(parts[0]);
                for (int t = 1; t <= numTransitions * 2; t += 2) {
                    int nextState = Integer.parseInt(parts[t]);
                    float probability = Float.parseFloat(parts[t + 1]);
                    action.transitions.add(new Transition(nextState, probability));
                }
                state.actions.add(action);
            }
            mdp.states.add(state);
        }

        scanner.close();
        return mdp;
    }
}

class MDP {
    ArrayList<State> states;
    int startState;

    public MDP() {
        this.states = new ArrayList<>();
    }


}

class State {
    float reward;
    boolean isTerminal;
    ArrayList<Action> actions;

    public State(float reward, boolean isTerminal) {
        this.reward = reward;
        this.isTerminal = isTerminal;
        this.actions = new ArrayList<>();
    }


}

class Action {
    ArrayList<Transition> transitions;

    public Action() {
        this.transitions = new ArrayList<>();
    }


}

class Transition {
    int nextState;
    float probability;

    public Transition(int nextState, float probability) {
        this.nextState = nextState;
        this.probability = probability;
    }
}

class ValueIteration {
    MDP mdp;
    float discountFactor;
    float terminationCriterion;
    float[] values;
    int[] policy;
    int backups;

    public ValueIteration(MDP mdp, float discountFactor, float terminationCriterion) {
        this.mdp = mdp;
        this.discountFactor = discountFactor;
        this.terminationCriterion = terminationCriterion;
        this.values = new float[mdp.states.size()];

        this.policy = new int[mdp.states.size()];
        for (int i = 0; i < policy.length; i++) {
            policy[i] = -1;
        }
        this.backups = 0;
        initializeValues();
    }

    private void initializeValues() {

        for (int s = 0; s < mdp.states.size(); s++) {
            State state = mdp.states.get(s);

            if (state.actions.isEmpty()) {
                values[s] = state.reward;
            } else {

                values[s] = 0;
            }
        }
    }

    public void solve() {
        boolean continueIteration;
        do {
            continueIteration = false;
            for (int s = 0; s < mdp.states.size(); s++) {
                State state = mdp.states.get(s);

                if (state.isTerminal || state.actions.isEmpty()) continue;

                float maxValue = Float.NEGATIVE_INFINITY;
                for (int a = 0; a < state.actions.size(); a++) {
                    float value = 0;
                    for (Transition t : state.actions.get(a).transitions) {
                        value += t.probability * (state.reward + discountFactor * values[t.nextState]);
                    }
                    if (value > maxValue) {
                        maxValue = value;
                        policy[s] = a;
                    }
                }
                if (Math.abs(maxValue - values[s]) > terminationCriterion) {
                    continueIteration = true;
                    values[s] = maxValue;
                    backups++;
                }
            }
        } while (continueIteration);

        outputPolicy();
    }

    private void outputPolicy() {

        for (int s = 0; s < mdp.states.size(); s++) {
            if (policy[s] == -1) {
                System.out.println();
            } else {
                System.out.println(policy[s]);
            }
        }
        System.out.println(backups + " backups performed.");
    }
}
