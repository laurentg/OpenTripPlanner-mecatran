/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.routing.edgetype;

import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.graph.Edge;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.vertextype.ParkAndRideVertex;

import com.vividsolutions.jts.geom.LineString;

/**
 * Edge to enter / exit a Park and Ride.
 * 
 * Prevent using P+R as footway/car shortcuts: only allow exit if a car has been
 * parked.
 * 
 * @author laurent
 * 
 */
public class ParkAndRideLinkEdge extends Edge {

	private static final long serialVersionUID = 1L;

	private boolean exit;

	public ParkAndRideLinkEdge(ParkAndRideVertex from, Vertex to) {
		super(from, to);
		exit = true;
	}

	public ParkAndRideLinkEdge(Vertex from, ParkAndRideVertex to) {
		super(from, to);
		exit = false;
	}

	@Override
	public State traverse(State s0) {
		StateEditor s1 = s0.edit(this);

		Edge backEdge = s0.getBackEdge();
		boolean back = s0.getOptions().isArriveBy();
		// If we are exiting (or entering-backward), check if we
		// really parked a car: this will prevent using P+R as
		// shortcut.
		if ((back != exit) && !(backEdge instanceof ParkAndRideEdge))
			return null;

		s1.incrementWeight(1);
		return s1.makeState();
	}

	@Override
	public double getDistance() {
		return 0;
	}

	@Override
	public LineString getGeometry() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	public String toString() {
		return "ParkAndRideLinkEdge(" + fromv + " -> " + tov + ")";
	}
}
