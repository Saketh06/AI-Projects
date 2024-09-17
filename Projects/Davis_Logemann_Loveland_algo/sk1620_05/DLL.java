package sk1620_05;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class DLL {

    SortedMap<Integer, Integer> assignments = new TreeMap<>();
    ArrayList<Clauses> function = new ArrayList<>();
    ArrayList<Clauses> funcopy = new ArrayList<>();
    ArrayList<Integer> unitclauses = new ArrayList<>();
    ArrayList<Integer> purevar = new ArrayList<>();
    int numvar = 0;
    int numclause = 0;
    int nodes=0;

    boolean DLL(ArrayList<Clauses> function) {
        if (function.isEmpty()) {
            return false;
        }
        for (Clauses c : function) {
            if (c.isEmpty()) {
                return false;
            }
        }

        setUnitclauses();
        simplifyunit(unitclauses);
        if (function.isEmpty()) {
            return true;
        }
        for (Clauses c : function) {
            if (c.isEmpty()) {
                return false;
            }
        }
        setPurevar();

        simplifypure(purevar);
        nodes++;


        if (function.isEmpty()) {
            return true;
        }
        for (Clauses c : function) {
            if (c.isEmpty()) {
                return false;
            }
        }

        ArrayList<Integer> subv = new ArrayList<>();
     for (Map.Entry<Integer, Integer> entry : assignments.entrySet()) {
            if (entry.getValue() == 0) {
                subv.add(entry.getKey());
            }
        }


        for (int v : subv) {

            if (DLL(setvar(v, true))) {

                return true;

            } else {
              // System.out.println("after true:"+function+"\n\n");
                function = funcopy;
                DLL(setvar(v, false));
            }

        }
        return false;


    }

    ArrayList<Clauses> setvar(int v, boolean t) {
        funcopy = new ArrayList<>(function);
        if (!t) {
            v = -v;
        }
        return simplify2(v, funcopy);
    }

    void setUnitclauses() {
        for (int j = 0; j < function.size(); j++) {
            Clauses c = function.get(j);
            if (c.isUnit()) {
                assignments.put(Math.abs(c.variables.get(0)), maketrue(c.variables.get(0)));
                unitclauses.add(c.variables.get(0));
            }
        }
    }

    void setPurevar(){
        int vartracker;
        boolean t=true;
        boolean j=false;
        for(int var: assignments.keySet()){
            t=true;
            j=false;

            for(Clauses c: function){

                if(c.variables.contains(-1*var)){

                    t=false;


                }
                if(c.variables.contains(var)){

                    j=true;
                }
            }
            if(t&&j) {

                assignments.put(var, maketrue(var));
                purevar.add(var);
            }

        }
    }



    int maketrue(int c) {
        if(c>0){
            return c;
        }
        else{

            return c;
        }
    }

    ArrayList<Clauses> simplify2(int v, ArrayList<Clauses> func) {
        int var = v;
        for (int j = 0; j < func.size(); j++) {
            Clauses c = func.get(j);
            if (c.isUnit() && c.variables.get(0) == var) {
                func.remove(c);
                j--;
            } else {
                for (int i = 0; i < c.variables.size(); i++) {
                    if (c.variables.get(i) == -var) {
                        c.variables.remove(i);
                        i--;
                        continue;
                    }
                    if (c.variables.get(i) == var) {
                        func.remove(c);
                        break;
                    }
                }
            }
        }
        return func;
    }

    void simplifyunit(ArrayList<Integer> w) {
        int var;
        for (int k = 0; k < w.size(); k++) {
            var = w.get(k);
            for (int j = 0; j < function.size(); j++) {
                Clauses c = function.get(j);

                if (c.isUnit() && c.variables.get(0) == var) {
                    function.remove(c);
                    j--;
                } else {
                    for (int i = 0; i < c.variables.size(); i++) {
                        if (c.variables.get(i) == -var) {
                            c.variables.remove(i);
                            i--;
                            continue;
                        }
                        if (c.variables.get(i) == var) {
                            function.remove(c);
                            break;
                        }
                    }
                }
            }
        }
        unitclauses.clear();
    }
    void simplifypure(ArrayList<Integer> w) {
        int var;
        for (int k = 0; k < w.size(); k++) {
            var = w.get(k);
            for (int j = 0; j < function.size(); j++) {

                Clauses c = function.get(j);

                if (c.isUnit() && c.variables.get(0) == var) {
                    function.remove(c);
                    j--;
                } else {
                    for (int i = 0; i < c.variables.size(); i++) {
                        if (c.variables.get(i) == -var) {
                            c.variables.remove(i);
                            i--;
                            continue;
                        }
                        if (c.variables.get(i) == var) {
                            function.remove(c);
                            j--;
                            break;
                        }
                    }
                }
            }
        }
        purevar.clear();
    }

    public static void main(String[] args) {
        DLL dll = new DLL();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("p cnf")) {
                    String[] parts = line.split("\\s+");
                    dll.numvar = Integer.parseInt(parts[2]);
                    dll.numclause = Integer.parseInt(parts[3]);
                } else if (!line.startsWith("c")) {

                    line=line.trim();
                    String[] clauseParts = line.split("\\s+");
                    for (String str : clauseParts) {

                    }

                    ArrayList<Integer> variables = new ArrayList<>();

                    for (int i = 0; i < clauseParts.length - 1; i++) {
                        int var = Integer.parseInt(clauseParts[i]);

                        dll.assignments.put(Math.abs(var), 0);
                        variables.add(var);
                    }

                    dll.function.add(new Clauses(variables));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

       boolean satisfiable = dll.DLL(dll.function);

        if (satisfiable) {

            System.out.println("s cnf 1 " + dll.numvar + " " + dll.numclause);

            for (Map.Entry<Integer, Integer> entry : dll.assignments.entrySet()) {
                int var = entry.getKey();
                int val = entry.getValue();

                System.out.println("v " + val );
            }
        } else {

            System.out.println("s cnf 0 " + dll.numvar + " " + dll.numclause);
        }

    }
}

class Clauses {
    ArrayList<Integer> variables = new ArrayList<>();

    public Clauses(ArrayList<Integer> v) {
        for (int j : v) {
            variables.add(j);
        }
    }

    public boolean isEmpty() {
        return variables.isEmpty();
    }

    public boolean isUnit() {
        return variables.size() == 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Clauses)) {
            return false;
        }
        Clauses other = (Clauses) obj;
        return this.variables.equals(other.variables);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int j : variables) {
            sb.append("," + j + " ");
        }
        return "(" + sb.toString() + ")";
    }
}