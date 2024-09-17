#!/usr/bin/env python3

import random
import sys
from builtins import range, len, int, float, ValueError, str, sorted, bool

__author__ = 'Bence Cserna'

def add_sign(literal):
    if bool(random.getrandbits(1)):
        return literal
    return -literal


def generate_random_cnf(variable_count, clause_count):
    clauses = []

    for x in range(0, clause_count):
        abs_clause = sorted(random.sample(range(1, variable_count + 1), 3))
        clause = [add_sign(literal) for literal in abs_clause]
        clauses.append(clause)

    return clauses


def print_cnf(clauses, variable_count, clause_count):
    print("p cnf %s %s" % (variable_count, clause_count))
    for clause in clauses:
        print(" ".join(str(literal) for literal in clause) + " 0")


def print_usage():
    print("Usage: [<number of variables>] [<number of clauses>]")


def main():
    if len(sys.argv) != 3:
        print_usage()
        return

    try:
        variable_count, clause_count = int(sys.argv[1]), int(sys.argv[2])
    except ValueError:
        print("Invalid input!")
        print_usage()
        return

    if variable_count < 3:
        print("The variable count should be at least 3.")
        print_usage()
        return

    clauses = generate_random_cnf(variable_count, clause_count)
    print_cnf(clauses, variable_count, clause_count)

    pass


if __name__ == "__main__":
    main()