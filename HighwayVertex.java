
/**
 * Simple data structure to encapsulate all of the information associated
 * with a vertex in the {@link HighwayGraph} structure.
 *
 * @author Jim Teresco
 *
 */
public class HighwayVertex {

    /** index of this vertex in the {@link HighwayGraph} array of vertices */
    protected int vNum;

    /** (unique) label of this vertex */
    protected String label;

    /** the coordinates of this vertex */
    protected LatLng point;

    /** head of the linked list of incident {@link HighwayEdge} objects */
    protected HighwayEdge head;

    /** visited field available for use by algorithms that need this */
    protected boolean visited;

    /**
     * Construct a new HighwayVertex object
     *
     * @param vNum the index of this vertex in {@link HighwayGraph} array of vertices
     * @param l the vertex label
     * @param lat latitude of this point's location
     * @param lng longitude of this point's location
     */
    public HighwayVertex(int vNum, String l, double lat, double lng) {
	this.vNum = vNum;
	label = l;
	point = new LatLng(lat,lng);
    }

}
