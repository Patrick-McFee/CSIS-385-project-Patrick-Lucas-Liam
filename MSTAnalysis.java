import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 3 || args.length > 4) {
            System.out.println("Usage: java MSTAnalysis <tmg_file> <trials> <algorithm> [output_pth_file]");
            System.out.println("Algorithms: prim, kruskal");
            System.exit(1);
        }

        String fileName = args[0];
        int trials = Integer.parseInt(args[1]);
        String algorithm = args[2].toLowerCase();
        String outputFileName = args.length == 4 ? args[3] : null;

        // Parse the HighwayGraph once to avoid timing the file I/O
        Scanner s = new Scanner(new File(fileName));
        HighwayGraph graph = new HighwayGraph(s);
        s.close();

        long totalComparisons = 0;
        long totalOperations = 0;
        long totalTimeNs = 0;
        
        List<HighwayEdge> mstEdges = null;

        for (int t = 0; t < trials; t++) {
            Metrics metrics = new Metrics();

            long startTime = System.nanoTime();
            mstEdges = runMST(graph, algorithm, metrics);
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
            
        // If an output file name was provided, write the MST to it in .pth format
        if (outputFileName != null && mstEdges != null && !mstEdges.isEmpty()) {
            writeMSTToPTH(graph, mstEdges, outputFileName);
        }
    }

    private static List<HighwayEdge> runMST(HighwayGraph graph, String algorithm, Metrics metrics) {
        switch (algorithm) {
            case "prim":
                return primMST(graph, metrics);
            case "kruskal":
                return kruskalMST(graph, metrics);
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
        }
    }

    private static List<HighwayEdge> primMST(HighwayGraph graph, Metrics metrics) {
        int numVertices = graph.vertices.length;
        boolean[] inMST = new boolean[numVertices];
        List<HighwayEdge> mstEdges = new ArrayList<>();
        
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
                mstEdges.add(minEdge); // Capture the edge for our output file

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
        return mstEdges;
    }

    private static List<HighwayEdge> kruskalMST(HighwayGraph graph, Metrics metrics) {
        int numVertices = graph.vertices.length;
        List<HighwayEdge> allEdges = new ArrayList<>();
        List<HighwayEdge> mstEdges = new ArrayList<>();

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
                mstEdges.add(edge); // Capture the edge for our output file

                // Optimization: stop early if we've added V-1 edges
                if (edgesInMST == numVertices - 1) {
                    break;
                }
            }
        }
        return mstEdges;
    }

    /**
     * Writes the resulting Minimum Spanning Tree out to a standard .pth path file
     * matching the formatting of HDX/METAL visualization tools.
     */
    private static void writeMSTToPTH(HighwayGraph graph, List<HighwayEdge> mstEdges, String outputFileName) {
        try (PrintWriter pw = new PrintWriter(outputFileName)) {
            if (mstEdges.isEmpty()) {
                System.out.println("MST is empty, nothing to write.");
                return;
            }

            // The .pth format requires a START line. We use the source of the first edge.
            HighwayVertex startVertex = graph.vertices[mstEdges.get(0).source];
            pw.println("START " + startVertex.label + " (" + startVertex.point.lat + "," + startVertex.point.lng + ")");
            
            // Output each edge in the format: "EdgeLabel (shape_points...) DestLabel (DestLat,DestLng)"
            for (HighwayEdge e : mstEdges) {
                HighwayVertex destVertex = graph.vertices[e.dest];
                
                // Start with the edge label
                pw.print(e.label + " ");
                
                // Include intermediate shape points if the edge has them
                if (e.shapePoints != null && e.shapePoints.length > 0) {
                    for (LatLng p : e.shapePoints) {
                        pw.print("(" + p.lat + "," + p.lng + ") ");
                    }
                }
                
                // End with the destination label and its coordinates
                pw.println(destVertex.label + " (" + destVertex.point.lat + "," + destVertex.point.lng + ")");
            }
            System.out.println("-> MST successfully written to " + outputFileName);
        } catch (IOException ex) {
            System.err.println("Error writing to output file: " + ex.getMessage());
        }
    }
}