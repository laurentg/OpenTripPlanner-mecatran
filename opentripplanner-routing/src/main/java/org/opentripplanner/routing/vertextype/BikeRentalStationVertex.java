package org.opentripplanner.routing.vertextype;

import org.opentripplanner.common.MavenVersion;
import org.opentripplanner.routing.graph.AbstractVertex;
import org.opentripplanner.routing.graph.Graph;

/**
 * A vertex for a bike rental station.
 * 
 * @author laurent
 * 
 */
public class BikeRentalStationVertex extends AbstractVertex {

	private static final long serialVersionUID = MavenVersion.VERSION.getUID();

	private int capacity;

	public BikeRentalStationVertex(Graph g, String label, double x, double y,
			String name, int capacity) {
		super(g, label, x, y, name);
		this.capacity = capacity;
	}

	public int getCapacity() {
		return capacity;
	}

}
