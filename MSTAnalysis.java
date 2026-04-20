import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

/**
 * This program analyzes Minimum Spanning Tree algorithms (Prim's vs. Kruskal's) 
 * to compare their efficiency using METAL project regional highway data networks.
 * @author Patrick McFee
 * @version Spring 2026
 */
public class MSTAnalysis {

    /**
     * This class is used to track edge comparisons and structural operations (like 
     * Priority Queue insertions or Union-Find updates) while the MST algorithm runs.
     */
    static class Metrics {
        long comparisons = 0;
        long operations = 0;
    }

    /**
     * A simple Union-Find (Disjoint Set) structure required for Kruskal's Algorithm
     */
    static class UnionFind {
        int[] parent;
        int[] rank;

        public UnionFind(int size) {
            parent = new int[size];
            rank = new int[size];
            for (int i = 0; i < size; i++) {
                parent[i] = i;
            }
        }

        public int find(int i) {
            if (parent[i] == i) {
                return i;
            }
            return parent[i] = find(parent[i]); // Path compression
        }

        public void union(int i, int j) {
            int rootI = find(i);
            int rootJ = find(j);

            if (rootI != rootJ) {
                if (rank[rootI] < rank[rootJ]) {
                    parent[rootI] = rootJ;
                } else if (rank[rootI] > rank[rootJ]) {
                    parent[rootJ] = rootI;
                } else {
                    parent[rootJ] = rootI;
                    rank[rootI]++;
                }
            }
        }
    }

    /**
     * Main method used to take input and test the MST algorithms
     * @param args[0] .tmg map data file to load
     * @param args[1] number of trials to run
     * @param args[2] algorithm to be used (prim or kruskal)
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("Usage: java MSTAnalysis <tmg_file> <trials> <algorithm>");
            System.out.println("Algorithms: prim, kruskal");
            System.exit(1);
        }

        String fileName = args[0];
        int trials = Integer.parseInt(args[1]);
        String algorithm = args[2].toLowerCase();

        // Parse the HighwayGraph once to avoid timing the file I/O
        Scanner s = new Scanner(new File(fileName));
        HighwayGraph graph = new HighwayGraph(s);
        s.close();

        long totalComparisons = 0;
        long totalOperations = 0;
        long totalTimeNs = 0;

        for (int t = 0; t < trials; t++) {
            Metrics metrics = new Metrics();

            long startTime = System.nanoTime();
            runMST(graph, algorithm, metrics);
            long endTime = System.nanoTime();

            totalTimeNs += (endTime - startTime);
            totalComparisons += metrics.comparisons;
            totalOperations += metrics.operations;
        }

        double avgTimeSeconds = (totalTimeNs / 1_000_000_000.0) / trials;
        long avgComparisons = totalComparisons / trials;
        long avgOperations = totalOperations / trials;
        
        // Extract just the filename for cleaner output
        String shortName = new File(fileName).getName();

        // Output format: Vertices Edges Algorithm FileName AvgTime(s) AvgComp AvgOps
        System.out.printf("%d %d %s %s %.6f %d %d%n", 
            graph.vertices.length, graph.numEdges, algorithm, shortName, 
            avgTimeSeconds, avgComparisons, avgOperations);
    }

    /**
     * Decides which MST algorithm to use based on user input
     * @param graph HighwayGraph to find the MST for
     * @param algorithm MST algorithm to be used
     * @param metrics variable pair used to track comparisons and operations
     */
    private static void runMST(HighwayGraph graph, String algorithm, Metrics metrics) {
        switch (algorithm) {
            case "prim":
                primMST(graph, metrics);
                break;
            case "kruskal":
                kruskalMST(graph, metrics);
                break;
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
        }
    }

    /**
     * Finds the Minimum Spanning Tree using Prim's Algorithm
     * @param graph The input highway graph
     * @param metrics tracks edge weight comparisons and PQ operations
     */
    private static void primMST(HighwayGraph graph, Metrics metrics) {
        int numVertices = graph.vertices.length;
        boolean[] inMST = new boolean[numVertices];
        
        // Priority Queue ordered by edge length
        PriorityQueue<HighwayEdge> pq = new PriorityQueue<>(new Comparator<HighwayEdge>() {
            @Override
            public int compare(HighwayEdge e1, HighwayEdge e2) {
                metrics.comparisons++;
                return Double.compare(e1.length, e2.length);
            }
        });

        // Start from vertex 0
        inMST[0] = true;
        HighwayEdge startEdge = graph.vertices[0].head;
        while (startEdge != null) {
            pq.add(startEdge);
            metrics.operations++;
            startEdge = startEdge.next;
        }

        int edgesInMST = 0;

        while (!pq.isEmpty() && edgesInMST < numVertices - 1) {
            HighwayEdge minEdge = pq.poll();
            metrics.operations++;

            int nextVertex = minEdge.dest;

            // If the destination is not yet in the MST, add it
            if (!inMST[nextVertex]) {
                inMST[nextVertex] = true;
                edgesInMST++;

                // Add all incident edges from the newly added vertex to the PQ
                HighwayEdge adjacent = graph.vertices[nextVertex].head;
                while (adjacent != null) {
                    if (!inMST[adjacent.dest]) {
                        pq.add(adjacent);
                        metrics.operations++;
                    }
                    adjacent = adjacent.next;
                }
            }
        }
    }

    /**
     * Finds the Minimum Spanning Tree using Kruskal's Algorithm
     * @param graph The input highway graph
     * @param metrics tracks edge weight comparisons and Union-Find operations
     */
    private static void kruskalMST(HighwayGraph graph, Metrics metrics) {
        int numVertices = graph.vertices.length;
        List<HighwayEdge> allEdges = new ArrayList<>();

        // Harvest all unique edges from the adjacency lists
        for (HighwayVertex v : graph.vertices) {
            HighwayEdge e = v.head;
            while (e != null) {
                // Since it's an undirected graph represented with directed edges,
                // we only keep one copy of each edge (where source < dest)
                if (e.source < e.dest) {
                    allEdges.add(e);
                }
                e = e.next;
            }
        }

        // Sort all edges by length
        allEdges.sort(new Comparator<HighwayEdge>() {
            @Override
            public int compare(HighwayEdge e1, HighwayEdge e2) {
                metrics.comparisons++;
                return Double.compare(e1.length, e2.length);
            }
        });

        UnionFind uf = new UnionFind(numVertices);
        int edgesInMST = 0;

        for (HighwayEdge edge : allEdges) {
            metrics.operations++; // Counting edges evaluated
            
            int rootSource = uf.find(edge.source);
            int rootDest = uf.find(edge.dest);

            // If including this edge doesn't cause a cycle
            if (rootSource != rootDest) {
                uf.union(edge.source, edge.dest);
                metrics.operations++; // Counting union operations
                edgesInMST++;

                // Optimization: stop early if we've added V-1 edges
                if (edgesInMST == numVertices - 1) {
                    break;
                }
            }
        }
    }
}