import java.util.*;

public class Prover {

    static int totalResolutions = 0;

    static class Clause {
        List<Literal> literals;


        public Clause(List<Literal> literals) {
            this.literals = new ArrayList<>(literals);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("{ ");
            for (int i = 0; i < literals.size(); i++) {
                builder.append(literals.get(i));
                if (i < literals.size() - 1) {
                    builder.append(" | ");
                }
            }
            builder.append(" }");
            return builder.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Clause)) return false;
            Clause other = (Clause) obj;
            Set<Literal> thisLiterals = new HashSet<>(literals);
            Set<Literal> otherLiterals = new HashSet<>(other.literals);
            return thisLiterals.equals(otherLiterals);
        }

        @Override
        public int hashCode() {
            return Objects.hash(new HashSet<>(literals));
        }

    }

    static class Literal {
        private String name;
        private List<Term> terms;
        private boolean negated;

        public Literal(String name, List<Term> terms, boolean negated) {
            this.name = name;
            this.terms = new ArrayList<>(terms);
            this.negated = negated;
        }

        Literal negate() {
            return new Literal(this.name, this.terms, !this.negated);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (negated) {
                builder.append("-");
            }
            builder.append(name).append("(");
            for (int i = 0; i < terms.size(); i++) {
                builder.append(terms.get(i));
                if (i < terms.size() - 1) {
                    builder.append(", ");
                }
            }
            builder.append(")");
            return builder.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Literal)) {
                return false;
            }
            Literal other = (Literal) obj;
            return name.equals(other.name) && negated == other.negated && terms.equals(other.terms);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + name.hashCode();
            result = prime * result + (negated ? 1231 : 1237);
            result = prime * result + terms.hashCode();
            return result;
        }
    }

    static abstract class Term {
        public abstract boolean containsVariable(Variable variable);
    }

    static class Variable extends Term {
        private String name;

        public Variable(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean containsVariable(Variable variable) {
            return this.equals(variable);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Variable)) return false;
            Variable other = (Variable) obj;
            return Objects.equals(name, other.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }

    static class Function extends Term {
        private String name;
        private List<Term> arguments;

        public Function(String name, List<Term> arguments) {
            this.name = name;
            this.arguments = new ArrayList<>(arguments);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(name).append("(");
            for (int i = 0; i < arguments.size(); i++) {
                builder.append(arguments.get(i));
                if (i < arguments.size() - 1) {
                    builder.append(", ");
                }
            }
            builder.append(")");
            return builder.toString();
        }

        @Override
        public boolean containsVariable(Variable variable) {
            for (Term term : arguments) {
                if (term.containsVariable(variable)) {
                    return true;
                }
            }
            return false;
        }
    }

    static class Substitution {
        Map<Variable, Term> mapping = new HashMap<>();

        void add(Variable var, Term term) {
            mapping.put(var, term);
        }
        Term apply(Term term) {
            if (term instanceof Variable && mapping.containsKey(term)) {
                return mapping.get(term);
            } else if (term instanceof Function) {
                Function func = (Function) term;
                List<Term> newArgs = new ArrayList<>();
                for (Term arg : func.arguments) {
                    newArgs.add(apply(arg));
                }
                return new Function(func.name, newArgs);
            }
            return term;
        }
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("{");
            boolean first = true;
            for (Map.Entry<Variable, Term> entry : mapping.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    builder.append(", ");
                }
                builder.append(entry.getKey()).append(" -> ").append(entry.getValue());
            }
            builder.append("}");
            return builder.toString();
        }
    }
    static Substitution unify(Term term1, Term term2) {
        Substitution substitution = new Substitution();
        if (term1.equals(term2)) {
            return substitution;
        } else if (term1 instanceof Variable) {
            substitution.add((Variable) term1, term2);
            return substitution;
        } else if (term2 instanceof Variable) {
            substitution.add((Variable) term2, term1);
            return substitution;
        } else if (term1 instanceof Function && term2 instanceof Function) {
            Function f1 = (Function) term1;
            Function f2 = (Function) term2;
            if (!f1.name.equals(f2.name) || f1.arguments.size() != f2.arguments.size()) {
                return null;
            }
            for (int i = 0; i < f1.arguments.size(); i++) {
                Substitution argSubst = unify(f1.arguments.get(i), f2.arguments.get(i));
                if (argSubst == null) {
                    return null;
                }
                for (int j = i + 1; j < f1.arguments.size(); j++) {
                    f1.arguments.set(j, applySubstitution(f1.arguments.get(j), argSubst));
                    f2.arguments.set(j, applySubstitution(f2.arguments.get(j), argSubst));
                }
                substitution.mapping.putAll(argSubst.mapping);
            }
            return substitution;
        }
        return null;
    }
    static Literal applySubstitutionToLiteral(Literal literal, Substitution substitution) {
        List<Term> substitutedTerms = new ArrayList<>();
        for (Term term : literal.terms) {
            substitutedTerms.add(applySubstitution(term, substitution));
        }
        return new Literal(literal.name, substitutedTerms, literal.negated);
    }
    static Term applySubstitution(Term term, Substitution substitution) {
        if (term instanceof Variable && substitution.mapping.containsKey(term)) {
            return substitution.mapping.get(term);
        } else if (term instanceof Function) {
            Function func = (Function) term;
            List<Term> newArgs = new ArrayList<>();
            for (Term arg : func.arguments) {
                newArgs.add(applySubstitution(arg, substitution));
            }
            return new Function(func.name, newArgs);
        }
        return term;
    }
    static Optional<Clause> resolve(Clause clause1, Clause clause2) {
        totalResolutions++;
        for (Literal lit1 : clause1.literals) {
            for (Literal lit2 : clause2.literals) {
                if (lit1.name.equals(lit2.name) && lit1.negated != lit2.negated) {

                    Substitution substitution = unifyTerms(lit1.terms, lit2.terms);
                    if (substitution != null) {

                        List<Literal> newLiterals = new ArrayList<>();
                        for (Literal l : clause1.literals) {
                            if (!l.equals(lit1)) {
                                newLiterals.add(applySubstitutionToLiteral(l, substitution));
                            }
                        }
                        for (Literal l : clause2.literals) {
                            if (!l.equals(lit2)) {
                                newLiterals.add(applySubstitutionToLiteral(l, substitution));
                            }
                        }
                        return Optional.of(new Clause(newLiterals));
                    }
                }
            }
        }
        return Optional.empty();
    }
    static Substitution unifyTerms(List<Term> terms1, List<Term> terms2) {
        if (terms1.size() != terms2.size()) return null;
        Substitution substitution = new Substitution();
        for (int i = 0; i < terms1.size(); i++) {
            Substitution sub = unify(terms1.get(i), terms2.get(i));
            if (sub == null) return null;

            for (int j = i + 1; j < terms1.size(); j++) {
                terms1.set(j, applySubstitution(terms1.get(j), sub));
                terms2.set(j, applySubstitution(terms2.get(j), sub));
            }
            substitution.mapping.putAll(sub.mapping);
        }
        return substitution;
    }
    static List<Clause> resolveClauses(List<Clause> clauses) {
        boolean addedNew = true;
        boolean proofFound = false;
        List<Clause> newClauses = new ArrayList<>(clauses);
        Set<Clause> uniqueNewClauses = new HashSet<>();

        while (addedNew && !proofFound) {

            addedNew = false;
            uniqueNewClauses.clear();
            List<Clause> currentRound = new ArrayList<>(newClauses);


            for (int i = 0; i < currentRound.size(); i++) {

                for (int j = i + 1; j < currentRound.size(); j++) {

                    Optional<Clause> resolved = resolve(currentRound.get(i), currentRound.get(j));
                    if (resolved.isPresent()) {
                        Clause newClause = resolved.get();
                        if (uniqueNewClauses.add(newClause) && !newClauses.contains(newClause)) {
                            newClauses.add(newClause);
                            addedNew = true;




                            if (newClause.literals.isEmpty()) {
                                System.out.println("True");
                                proofFound = true;
                                break;
                            }
                        }
                    }
                }
                if (proofFound) break;
            }

            if (!addedNew) {
                System.out.println("No Proof Exists");
                break;
            }
        }

        return newClauses;
    }


    static boolean isNewClauseUnique(Clause newClause, List<Clause> existingClauses) {
        return existingClauses.stream().noneMatch(existingClause -> existingClause.equals(newClause));
    }


    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        List<Clause> kb = new ArrayList<>();


        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();

            if (line.equals("--- negated query ---")) {

                continue;
            }

            if (!line.isEmpty()) {
                Clause clause = parseClause(line);
                if (clause != null) {
                    kb.add(clause);
                }
            }
        }

        List<Clause> resolvedClauses = resolveClauses(kb);

        System.out.println("resolutions:");
        resolvedClauses.forEach(System.out::println);

        boolean proofFound = resolvedClauses.stream().anyMatch(c -> c.literals.isEmpty());
        if (proofFound) {
            System.out.println("Empty clause found");
        } else {
            System.out.println("No Proof Exists");
        }

        System.out.println(totalResolutions+ " resolutions performed: ");
    }





    private static Clause parseClause(String line) {
        String[] literalStrings = line.split("\\|");
        List<Literal> literals = new ArrayList<>();

        for (String literalStr : literalStrings) {
            literalStr = literalStr.trim();
            boolean negated = literalStr.startsWith("-");
            if (negated) {
                literalStr = literalStr.substring(1);
            }

            int openParenIndex = literalStr.indexOf('(');
            int closeParenIndex = literalStr.indexOf(')');



            String name = (openParenIndex != -1) ? literalStr.substring(0, openParenIndex) : literalStr;
            List<Term> terms = new ArrayList<>();

            if (openParenIndex != -1 && closeParenIndex != -1) {
                String[] termStrings = literalStr.substring(openParenIndex + 1, closeParenIndex).split(",");

                for (String termStr : termStrings) {
                    termStr = termStr.trim();
                    if (termStr.contains("(")) {
                        int funcOpenParen = termStr.indexOf('(');
                        int funcCloseParen = termStr.lastIndexOf(')');
                        String functionName = termStr.substring(0, funcOpenParen);
                        String[] functionArgsStrings = termStr.substring(funcOpenParen + 1, funcCloseParen).split(",");
                        List<Term> functionArgs = new ArrayList<>();
                        for (String arg : functionArgsStrings) {
                            arg = arg.trim();
                            functionArgs.add(new Variable(arg));
                        }
                        terms.add(new Function(functionName, functionArgs));
                    } else if (!termStr.isEmpty()) {

                        terms.add(new Variable(termStr));
                    }
                }
            }

            literals.add(new Literal(name, terms, negated));
        }

        return new Clause(literals);
    }

}