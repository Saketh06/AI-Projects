#!/usr/bin/env python

import random
import sys
__author__ = 'Bence Cserna'
#ported to python3 by Devin Thomas 2/2023

def generate_random_graph(vertex_count, probability):
    graph = {}
    edge_count = 0

    for source_vertex in range(1, vertex_count + 1):
        graph[source_vertex] = []
        for target_vertex in range(source_vertex + 1, vertex_count + 1):
            if random.random() < probability:
                graph[source_vertex] += [target_vertex]
                edge_count += 1

    return graph, edge_count


def print_graph(graph, edge_count):
    print("p edge %s %s" % (len(graph), edge_count))
    for source_vertex, edge_list in graph.items():
        for target_vertex in edge_list:
            print("e %d %d" % (source_vertex, target_vertex))

def print_usage():
    print("Usage: [<vertex count (positive int)>] [<edge probability (float)>]")

def main():
    if len(sys.argv) != 3:
        print_usage()
        return

    try:
        vertex_count, probability = int(sys.argv[1]), float(sys.argv[2])
    except ValueError:
        print("Invalid input!")
        print_usage()
        return

    graph, edge_count = generate_random_graph(vertex_count, probability)
    print_graph(graph, edge_count)

    pass


if __name__ == "__main__":
    main()