package org.opentripplanner.routing.vertextype;

import org.opentripplanner.common.MavenVersion;
import org.opentripplanner.routing.graph.Graph;

/**
 * A vertex for a park and ride area.
 * 
 * @author laurent
 * 
 */
public class ParkAndRideVertex extends StreetVertex {

	private static final long serialVersionUID = MavenVersion.VERSION.getUID();

	public ParkAndRideVertex(Graph g, String label, double x, double y,
			String name) {
		super(g, label, x, y, name);
	}

}
