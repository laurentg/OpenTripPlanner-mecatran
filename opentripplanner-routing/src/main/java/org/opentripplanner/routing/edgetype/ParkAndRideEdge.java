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

import org.opentripplanner.routing.core.EdgeNarrative;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.graph.AbstractEdge;
import org.opentripplanner.routing.vertextype.ParkAndRideVertex;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Parking a car at a park-and-ride station.
 * 
 * @author laurent
 * 
 */
public class ParkAndRideEdge extends AbstractEdge {

	private static final long serialVersionUID = 1L;

	public ParkAndRideEdge(ParkAndRideVertex parkAndRide) {
		super(parkAndRide, parkAndRide);
	}
	
	@Override
	public State traverse(State s0) {
		TraverseOptions options = s0.getOptions();
		if (options.isArriveBy()) {
			/*
			 * To get back a car, we need to walk and have car mode enabled.
			 */
			if (s0.getNonTransitMode(options) != TraverseMode.WALK)
				return null;
			if (!options.getModes().getCar())
				return null;
			if (!s0.isCarParked())
				throw new IllegalStateException("Your car has been stolen?");
			EdgeNarrative en = new FixedModeEdge(this,
					s0.getNonTransitMode(options));

			StateEditor s1e = s0.edit(this, en);
			s1e.incrementWeight(options.parkAndRideCost);
			s1e.incrementTimeInSeconds(options.parkAndRideTime);
			s1e.setCarParked(false);
			State s1 = s1e.makeState();
			return s1;
		} else {
			/*
			 * To park a car, we need to be in one, and be able to walk afterwards.
			 */
			if (s0.getNonTransitMode(options) != TraverseMode.CAR)
				return null;
			if (!options.getModes().getWalk())
				return null;
			if (s0.isCarParked())
				throw new IllegalStateException("Do you happen to have TWO cars?");
			EdgeNarrative en = new FixedModeEdge(this,
					s0.getNonTransitMode(options));

			StateEditor s1e = s0.edit(this, en);
			s1e.incrementWeight(options.parkAndRideCost);
			s1e.incrementTimeInSeconds(options.parkAndRideTime);
			s1e.setCarParked(true);
			State s1 = s1e.makeState();
			return s1;
		}
	}

	@Override
	public double getDistance() {
		return 0;
	}

	@Override
	public Geometry getGeometry() {
		return null;
	}

	@Override
	public TraverseMode getMode() {
		return TraverseMode.WALK;
	}

	@Override
	public String getName() {
		return getToVertex().getName();
	}

	@Override
	public boolean hasBogusName() {
		return false;
	}


	public boolean equals(Object o) {
		if (o instanceof ParkAndRideEdge) {
			ParkAndRideEdge other = (ParkAndRideEdge) o;
			return other.getFromVertex().equals(fromv)
					&& other.getToVertex().equals(tov);
		}
		return false;
	}

	public String toString() {
		return "ParkAndRideEdge(" + fromv + " -> " + tov + ")";
	}
}
