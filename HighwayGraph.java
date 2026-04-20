import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * An undirected, adjacency-list based graph data structure developed
 * specifically for METAL highway mapping graphs.
 *
 * Many protected fields are used here and in the supporting classes
 * and all code is currently in the Java default package to simplify 
 * the use of these classes in implementations of various algorithms.
 *
 * @author Jim Teresco
 * @see HighwayVertex
 * @see HighwayEdge
 *
 */

public class HighwayGraph
{

    public static final DecimalFormat df = new DecimalFormat("#.###");

    /** array of vertices in the graph */
    protected HighwayVertex[] vertices;

    /** number of edges */
    protected int numEdges;

    /**
     * Construct a HighwayGraph from a TMG format file that comes from
     * the given Scanner (likely over a File or URLConnection, but
     * does not matter here)
     *
     * @param s Scanner from which the TMG format data is to be read
     */
    public HighwayGraph(Scanner s) {

        // read header line -- for now assume it's OK, but should
        // check
        s.nextLine();

        // read number of vertices and edges
        int numVertices = s.nextInt();
        numEdges = s.nextInt();

        // construct our array of Vertices
        vertices = new HighwayVertex[numVertices];

        // next numVertices lines are HighwayVertex entries
        for (int vNum = 0; vNum < numVertices; vNum++) {
            vertices[vNum] = new HighwayVertex(vNum, s.next(),
					s.nextDouble(), s.nextDouble());
        }

        // next numEdge lines are Edge entries
        for (int eNum = 0; eNum < numEdges; eNum++) {
            int v1 = s.nextInt();
            int v2 = s.nextInt();
            String label = s.next();
            // shape points take us to the end of the line, and this
            // will be just a new line char if there are none for this edge
            String shapePointText = s.nextLine().trim();
            String[] shapePointStrings = shapePointText.split(" ");
            LatLng v1Tov2[] = null;
            LatLng v2Tov1[] = null;
            if (shapePointStrings.length > 1) {
                // build arrays in both orders
                v1Tov2 = new LatLng[shapePointStrings.length/2];
                v2Tov1 = new LatLng[shapePointStrings.length/2];
                for (int pointNum = 0; pointNum < shapePointStrings.length/2; pointNum++) {
                    LatLng point = new LatLng(Double.parseDouble(shapePointStrings[pointNum*2]),
                            Double.parseDouble(shapePointStrings[pointNum*2+1]));
                    v1Tov2[pointNum] = point;
                    v2Tov1[shapePointStrings.length/2 - pointNum - 1] = point;
                }
            }

            // build our HighwayEdge structures and add to each adjacency list
            vertices[v1].head = new HighwayEdge(label, v1, v2, vertices[v1].point, v1Tov2, vertices[v2].point, vertices[v1].head);
            vertices[v2].head = new HighwayEdge(label, v2, v1, vertices[v2].point, v2Tov1, vertices[v1].point, vertices[v2].head);
        }
    }

    /**
     * Construct and return a human-readable text representation of the 
     * contents of the graph.
     *
     * @return a human-readable text representation of the 
     * contents of the graph
     */
    @Override
    public String toString() {

        StringBuilder s = new StringBuilder();
        s.append("|V|=" + vertices.length + ", |E|=" + numEdges + "\n");
        for (HighwayVertex v : vertices) {
            s.append(v.label + " " + v.point + "\n");
            HighwayEdge e = v.head;
            while (e != null) {
                HighwayVertex o = vertices[e.dest];
                s.append("  to " + o.label + " " + o.point + " on " + e.label);
                if (e.shapePoints != null) {
                    s.append(" via");
                    for (int pointNum = 0; pointNum < e.shapePoints.length; pointNum++) {
                        s.append(" " + e.shapePoints[pointNum]);
                    }
                }
                s.append(" length " + df.format(e.length) + "\n");
                e = e.next;
            }
        }

        return s.toString();
    }

    /**
     * Look up a HighwayVertex by name/label
     *
     * @param label the label to match
     *
     * @return the HighwayVertex matching the label, or null if not found
     */
    public HighwayVertex getVertexByName(String label) {

	for (HighwayVertex v: vertices) {
	    if (v.label.equals(label)) return v;
	}
	return null;
    }

    /**
     * Mark all vertices as unvisited.
     */
    public void markAllUnvisited() {

	for (HighwayVertex v: vertices) {
	    v.visited = false;
	}
    }
	
    /**
     * A main method for testing the construction and printing of
     * a graph loaded from a METAL .tmg file.
     *
     * @param args command-line parameters, with the name of the
     * tmg file to load specified in args[0]
     */
    public static void main(String args[]) throws IOException {

        if (args.length != 1) {
            System.err.println("Usage: java HighwayGraph tmgfile");
            System.exit(1);
        }

        // read in the file to construct the graph
        Scanner s = new Scanner(new File(args[0]));
        HighwayGraph g = new HighwayGraph(s);
        s.close();

        // print summary of the graph
        System.out.println(g);

    }
}
