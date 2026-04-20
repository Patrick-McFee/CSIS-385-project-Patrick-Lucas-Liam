/**    
 * Small data structure representing a
 * latitude-longitude pair.  It has the added benefit
 * of being able to compute its distance to another
 * LatLng object.
 *
 * @author Jim Teresco
 */
public class LatLng {

    /** latitude */
    protected double lat;
    /** longitude */
    protected double lng;

    /**
     * Construct a new LatLng with the given coordinates.
     *
     * @param lat the latitude
     * @param lng the longitude
     */
    public LatLng(double lat, double lng) {
	this.lat = lat;
	this.lng = lng;
    }

    /**
       Compute the distance in miles from this LatLng to another

       @param other another LatLng
       @return the distance in miles from this LatLng to other
    */
    public double distanceTo(LatLng other) {
	/** radius of the Earth in statute miles */
	final double EARTH_RADIUS = 3963.1;

	// did we get the same point?
	if (equals(other)) return 0.0;

	// coordinates in radians
	double rlat1 = Math.toRadians(lat);
	double rlng1 = Math.toRadians(lng);
	double rlat2 = Math.toRadians(other.lat);
	double rlng2 = Math.toRadians(other.lng);

	return Math.acos(Math.cos(rlat1)*Math.cos(rlng1)*Math.cos(rlat2)*Math.cos(rlng2) +
			 Math.cos(rlat1)*Math.sin(rlng1)*Math.cos(rlat2)*Math.sin(rlng2) +
			 Math.sin(rlat1)*Math.sin(rlat2)) * EARTH_RADIUS;
    }

    /**
       Compare another LatLng with this for equality, subject to the
       specified tolerance.

       @param o the other LatLng
       @return whether the two lat/lng pairs should be considered equal
    */
    public boolean equals(Object o) {
	final double TOLERANCE = 0.00001;
	LatLng other = (LatLng)o;

	return ((Math.abs(other.lat-lat) < TOLERANCE) &&
                (Math.abs(other.lng-lng) < TOLERANCE));
    }

    /**
     * Return the latitude and longitude in a nicely-formatted pair
     *
     * @return the latitude and longitude in a nicely-formatted pair
     */
    public String toString() {
	return "(" + lat + "," + lng + ")";
    }
}
