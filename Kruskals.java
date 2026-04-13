import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * This program implements Kruskal's Algorithm to compute the Minimum Spanning Tree (MST)
 * of a weighted graph. The algorithm sorts all edges by weight and repeatedly adds the
 * smallest edge that does not form a cycle. In the context of this project, we use
 * Kruskal's Algorithm to find the minimum connecting distance between cities using highway routes.
 *
 * AI Assistance:
 * ChatGPT was used to help:
 * - Explain the logic and structure of Kruskal's Algorithm
 * - Help debug
 * - Provide guidance on implementation
 *
 * (All code was reviewed and understood before uploading changes.)
 *
 * @author Lucas Davey, Patrick Mcfee, Liam Annucci
 * @version Spring 2026
 */
public class Kruskals {

    static class Edge implements Comparable<Edge> {
        int source;
        int destination;
        int weight;

        public Edge(int source, int destination, int weight) {
            this.source = source;
            this.destination = destination;
            this.weight = weight;
        }

        public int compareTo(Edge other) {
            return this.weight - other.weight;
        }
    }

    public static int find(int[] parent, int vertex) {
        while (parent[vertex] != vertex) {
            vertex = parent[vertex];
        }
        return vertex;
    }

    public static void kruskalMST(int[][] graph) {
        int V = graph.length;
        ArrayList<Edge> edges = new ArrayList<Edge>();

        for (int i = 0; i < V; i++) {
            for (int j = i + 1; j < V; j++) {
                if (graph[i][j] != 0) {
                    edges.add(new Edge(i, j, graph[i][j]));
                }
            }
        }

        Collections.sort(edges);

        int[] parent = new int[V];
        for (int i = 0; i < V; i++) {
            parent[i] = i;
        }

        int totalWeight = 0;
        int edgesUsed = 0;

        for (Edge e : edges) {
            int root1 = find(parent, e.source);
            int root2 = find(parent, e.destination);

            if (root1 != root2) {
                parent[root1] = root2;
                totalWeight += e.weight;
                edgesUsed++;

                if (edgesUsed == V - 1) {
                    break;
                }
            }
        }

        System.out.println("Kruskal's MST total weight: " + totalWeight);
    }
}