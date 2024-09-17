#!/usr/bin/env python

from __future__ import print_function
import sys
from subprocess import Popen, PIPE
import time
import threading
import subprocess

__author__ = 'Bence Cserna'
TIME_LIMIT = 60


def error(*objs):
    print("ERROR: ", *objs, file=sys.stderr)


def filter_input(lines):
    return [line for line in lines if not line.startswith("c") and len(line) > 0]


def read_graph(input):
    lines = filter_input(input)

    header = lines.pop(0)
    header_items = header.strip().split(" ")

    if header_items[0] != "p" or header_items[1] != "edge":
        error("Invalid header: " + header)
        sys.exit(0)

    try:
        vertex_count, edge_count = int(header_items[2]), int(header_items[3])
    except ValueError:
        error("Invalid header: " + header)
        sys.exit(0)

    graph = {x: [] for x in xrange(1, vertex_count + 1)}

    for line in lines:
        elements = line.strip().split(" ")

        if elements[0] != "e":
            error("Invalid edge: " + line)
            sys.exit(0)

        try:
            v1, v2 = int(elements[1]), int(elements[2])
        except ValueError:
            error("Invalid edge: " + line)
            sys.exit(0)

        # Undirected edges
        graph[v1] += [v2]
        graph[v2] += [v1]

    return graph


def read_coloring(input):
    lines = filter_input(input)

    header = lines.pop(0)
    header_items = header.strip().split(" ")

    if header_items[0].startswith("No"):
        print(header)
        return {}

    if header_items[0] != "s" or header_items[1] != "col":
        error("Invalid header: " + header)
        sys.exit(0)

    try:
        color_count = int(header_items[2])
    except ValueError:
        error("Invalid header: " + header)
        sys.exit(0)

    colors = {}

    for line in lines:
        elements = line.strip().split(" ")

        if elements[0] != "l":
            error("Invalid color: " + line)
            sys.exit(0)

        try:
            vertex, color = int(elements[1]), int(elements[2])
        except ValueError:
            error("Invalid color: " + line)
            sys.exit(0)

        if color > color_count or color <= 0:
            error("Invalid color: " + header)
            sys.exit(0)

        colors[vertex] = color

    return colors


def validate_coloring(graph, colors, number_of_colors):
    for vertex, color in colors.iteritems():
        if color > number_of_colors:
            error("Invalid coloring: Color is out of range: %s (max: %s)" % (color, number_of_colors))
            return False

        for adjecent in graph[vertex]:
            if colors[adjecent] == color:
                error("Invalid coloring: %s and %s have the same %s color" % (vertex, adjecent, color))
                return False

    print("Valid coloring!")
    return True


def print_usage():
    print("Usage: [<executable>] [<algorithm>] [<number of colors>]")


def validate(executable, algorithm, number_of_colors):
    raw_input = sys.stdin.readlines()
    graph = read_graph(raw_input)

    process = Popen([" ".join(["./" + executable, algorithm, str(number_of_colors)])],
                    stdin=PIPE, stdout=PIPE, shell=True)

    print("Executing solver...")
    process.stdin.writelines(raw_input)

    start = time.time()
    process.stdin.close()

    process.wait()
    end = time.time()
    raw_coloring = process.stdout.readlines()

    print("Execution time: %s seconds" % (end - start))
    print("Parsing coloring...")

    print(raw_coloring.pop())
    coloring = read_coloring(raw_coloring)

    print("Validating coloring...")

    validate_coloring(graph, coloring, number_of_colors)


class Validator:
    def __init__(self, executable, algorithm, number_of_colors):
        self.number_of_colors = number_of_colors
        self.algorithm = algorithm
        self.executable = executable
        self.process = None

    def run(self, timeout):
        def validate(executable, algorithm, number_of_colors):
            raw_input = sys.stdin.readlines()
            graph = read_graph(raw_input)

            self.process = Popen([" ".join(["./" + executable, algorithm, str(number_of_colors)])], stdin=PIPE,
                                 stdout=PIPE,
                                 shell=True)

            print("Executing solver...")
            self.process.stdin.writelines(raw_input)

            start = time.time()
            self.process.stdin.close()

            retcode = self.process.wait()
            end = time.time()
            print("Execution time: %s seconds" % (end - start))

            if retcode != 0:
                error("Process terminated.")
                return

            print("Parsing coloring...")

            raw_coloring = self.process.stdout.readlines()
            print(raw_coloring.pop())
            coloring = read_coloring(raw_coloring)

            if not len(coloring):
                return False

            print("Validating coloring...")

            validate_coloring(graph, coloring, number_of_colors)

        thread = threading.Thread(target=validate,
                                  kwargs={"executable": self.executable, "algorithm": self.algorithm,
                                          "number_of_colors": self.number_of_colors})
        thread.start()
        thread.join(timeout)
        if thread.is_alive():
            self.process.terminate()
            error("Time limit reached: %s seconds" % timeout)
            thread.join()


def main():
    if len(sys.argv) < 4:
        print_usage()
        return

    try:
        executable, algorithm, number_of_colors = sys.argv[1], sys.argv[2], int(sys.argv[3])
    except ValueError, e:
        error(str(e))
        error(["Invalid input: "] + sys.argv)
        print_usage()
        return

    # validate(executable, algorithm, number_of_colors)
    validator = Validator(executable, algorithm, number_of_colors)

    validator.run(TIME_LIMIT)


if __name__ == "__main__":
    main()