
/**
 *
 * This program implements Prim's Algorithm to compute the Minimum Spanning Tree (MST)
 * of a weighted graph. The algorithm starts from vertex 0 and repeatedly
 * adds the smallest edge that connects a vertex in the MST to a vertex not yet visited. In the 
 * context of this project, we will use Prim's Algorithm to find the minimum connecting distance 
 * between cities using highway routes. This implementation uses an adjacency matrix and runs in O(v^2) time.
 *
 * AI Assistance:
 * ChatGPT was used to help:
 * - Explain the logic and structure of Prim's Algorithm. Helped get started.
 * - Assisted in debugging
 * - Provided guidance on implementation
 *
 * (All code was reviewed and understood before uploading changes.)
 *
 * @author Lucas Davey, Patrick Mcfee, Liam Annucci
 * @version Spring 2026
 */
public class Prims {
    public static void primMST(int[][] graph) {
        int V = graph.length;

        boolean[] inMST = new boolean[V];
        int[] minDistance = new int[V];    //min distance of the edge

        //initialize minDistance to a large number becuase we dont know the
        //distance of any edge yet so we treat it bad for now
        for (int i = 0; i < V; i++) {
            minDistance[i] = 1000000000;
        }

        minDistance[0] = 0;
        int totalWeight = 0;
        for (int count = 0; count < V; count++) {
            //find minimum distance vertex not yet in MST
            int min = 1000000000;
            int currentLocation = -1;
            
            for (int nextLocation = 0; nextLocation < V; nextLocation++) {
                if (!inMST[nextLocation] && minDistance[nextLocation] < min) {
                    min = minDistance[nextLocation];
                    currentLocation = nextLocation;
                }
            }

            inMST[currentLocation] = true;
            totalWeight += minDistance[currentLocation];

            //Update adjacent vertices. (Used AI to help clarify this part)
            for (int v = 0; v < V; v++) {
                if (graph[currentLocation][v] != 0 && !inMST[v] && graph[currentLocation][v] < minDistance[v]) {
                    minDistance[v] = graph[currentLocation][v];
                }
            }
        }

        System.out.println("Prim's MST total weight: " + totalWeight);
    }
}

