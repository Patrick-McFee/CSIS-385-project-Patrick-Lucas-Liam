
/**
 * Simple data structure to encapsulate all of the information associated
 * with an edge in the {@link HighwayGraph} structure.
 * 
 * These edges also are used to form linked lists of the edges
 * incident from each {@link HighwayVertex}, which has the reference
 * to the head of the list.
 *
 * @author Jim Teresco
 *
 */

public class HighwayEdge {
        
    // the edge needs to know its own label, its destination vertex,
    // its source vertex (even though it knows its source as which
    // vertex's list contains this edge), an optional array of points
    // that improve the edge's shape, and its length in miles, which
    // is computed on construction
    protected String label;
    protected int source;
    protected int dest;
    protected LatLng[] shapePoints;
    protected double length;
        
    // and HighwayEdge is also a linked list
    protected HighwayEdge next;

    public HighwayEdge(String l, int start, int dst, LatLng startPoint, LatLng points[], LatLng endPoint, HighwayEdge n) {
	label = l;
	source = start;
	dest = dst;
	shapePoints = points;
	next = n;
	length = 0.0;
	LatLng prevPoint = startPoint;
	if (points != null) {
	    for (int pointNum = 0; pointNum < points.length; pointNum++) {
		length += prevPoint.distanceTo(points[pointNum]);
		prevPoint = points[pointNum];
	    }
	}
	length += prevPoint.distanceTo(endPoint);
    }
}

