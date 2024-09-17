#!/usr/bin/env python3
from __future__ import print_function
import sys
from subprocess import Popen, PIPE
import time
import threading
from builtins import str, int, ValueError, len, classmethod, vars, set, bytes, type, any
from enum import Enum

__author__ = 'Bence Cserna'
TIME_LIMIT = 60


def error(*objs):
    print("ERROR: ", *objs, file=sys.stderr)


def filter_input(lines):
    return [line for line in lines if not line.startswith('c') and len(line) > 0]


def read_problem(input):
    lines = filter_input(input)

    header = lines.pop(0)
    #header_items = header.strip().split(" ")
    header_items = header.strip().split()

    if header_items[0] != "p" or header_items[1] != "cnf":
        error("Invalid header: " + header)
        sys.exit(0)

    try:
        variable_count, clause_count = int(header_items[2]), int(header_items[3])
    except ValueError:
        error("Invalid header: " + header)
        sys.exit(0)

    clauses = []

    raw_clauses = " ".join(lines).strip().split(" 0")

    for raw_clause in raw_clauses:
        literals = [literal.strip() for literal in raw_clause.strip().split(" ") if len(literal.strip()) > 0]

        clause = []

        for literal in literals:
            try:
                clause.append(int(literal.strip()))
            except ValueError:
                error("Invalid literal [{}] in clause [{}].".format(literal, raw_clause))
                sys.exit(0)

        if len(clauses) >= clause_count:
            break
        if len(clause) > 0:
            clauses.append(clause)

    return clauses


class Result(Enum):
    solution_found = 1
    no_solution = 0
    solution_not_found = -1

    @classmethod
    def from_int(cls, val):
        for result in Result:
            if result.value == val:
                return result


def read_assignments(input):
    lines = filter_input(input)

    header = lines.pop(0)
    header_items = header.strip().split(" ")

    if header_items[0] != "s" or header_items[1] != "cnf" or len(header_items) != 5:
        error("Invalid header: " + header)
        sys.exit(0)

    try:
        result = Result.from_int(int(header_items[2]))
        number_of_variables = int(header_items[3])
        number_of_clauses = int(header_items[4])

    except ValueError:
        error("Invalid header: " + header)
        sys.exit(0)

    assignments = set()

    for line in lines:
        elements = line.strip().split(" ")

        if any(x in line for x in [".", "nodes", "tries", "total", "flips"]):
            print(line)
            continue

        if elements[0] != "v":
            error("Invalid assignment: " + line)
            sys.exit(0)

        try:
            assignment = int(elements[1])
        except ValueError:
            error("Invalid assignment value: " + line)
            sys.exit(0)

        assignments.add(assignment)

    return assignments, result


def validate_assignment(clauses, assignments):
    if len({abs(a) for a in assignments}) != len(assignments):
        print("Conflicting assignments, invalid solution.")
        return False

    for clause in clauses:
        satisfied = False

        for literal in clause:
            if literal in assignments:
                satisfied = True
                break

        if not satisfied:
            print("Invalid variable assignment! The following clause is not satisfied: " + str(clause))
            return False

    print("Valid assignment!")
    return True


def print_usage():
    print("Usage: [<executable>]")


class Validator:
    def __init__(self, executable, grad_flag):
        self.grad_flag = grad_flag
        self.executable = executable
        self.process = None

    def run(self, timeout):
        def validate(executable, grad_flag):
            raw_input = sys.stdin.readlines()
            raw_input_byte = [str.encode(line) for line in raw_input]

            clauses = read_problem(raw_input)

            if grad_flag:
                cmd = [" ".join(["./" + executable, "-w"])]
            else:
                cmd = ["./" + executable]

            self.process = Popen(cmd, stdin=PIPE, stdout=PIPE, shell=True)

            # print("Executing solver...")
            self.process.stdin.writelines(raw_input_byte)

            start = time.time()
            self.process.stdin.close()

            retcode = self.process.wait()
            end = time.time()
            print("Execution time: %s seconds" % (end - start))

            if retcode != 0:
                error("Process terminated.")
                return

            # print("Retrieving assignments...")

            raw_coloring_byte = self.process.stdout.readlines()

            # print("Parsing assignments...")

            raw_coloring = [bytes.decode(line) for line in raw_coloring_byte]

            assignments, result = read_assignments(raw_coloring)

            if result == Result.no_solution:
                print("Not satisfiable!")
                return

            if result == Result.solution_not_found:
                print("No solution found! (timeout)")
                return

            if result == Result.solution_found:
                print("Satisfiable!")

            # print("Validating assignments...")

            validate_assignment(clauses, assignments)

        thread = threading.Thread(target=validate,
                                  kwargs={"executable": self.executable, "grad_flag": self.grad_flag})
        thread.start()
        thread.join(timeout)
        if thread.is_alive():
            self.process.terminate()
            error("Time limit reached: %s seconds" % timeout)
            thread.join()


def main():
    if len(sys.argv) < 2:
        print_usage()
        return

    try:
        if len(sys.argv) == 2:
            executable, grad_flag = sys.argv[1], False
        else:
            executable, grad_flag = sys.argv[1], True
    except ValueError as e:
        error(["Invalid input: "] + sys.argv)
        print_usage()
        return

    # validate(executable, algorithm, number_of_colors)
    validator = Validator(executable, grad_flag)

    validator.run(TIME_LIMIT)


if __name__ == "__main__":
    main()