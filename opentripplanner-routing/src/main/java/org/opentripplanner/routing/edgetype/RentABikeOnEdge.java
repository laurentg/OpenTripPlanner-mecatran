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
import org.opentripplanner.routing.graph.Vertex;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Renting a bike edge.
 * 
 * Cost is the time to pickup a bike plus "inconvenience of renting".
 * 
 * @author laurent
 * 
 */
public class RentABikeOnEdge extends AbstractEdge {

	private static final long serialVersionUID = 1L;

	public RentABikeOnEdge(Vertex from, Vertex to) {
		super(from, to);
	}

	@Override
	public State traverse(State s0) {
		TraverseOptions options = s0.getOptions();
		if (options.isArriveBy()) {
			/*
			 * To dropoff a bike, we need to have rented one.
			 */
			if (!s0.isBikeRenting())
				return null;
			EdgeNarrative en = new FixedModeEdge(this,
					s0.getNonTransitMode(options));

			StateEditor s1 = s0.edit(this, en);
			s1.incrementWeight(options.bikeRentalDropoffCost);
			s1.incrementTimeInSeconds(options.bikeRentalDropoffTime);
			s1.setBikeRenting(false);
			return s1.makeState();
		} else {
			/*
			 * If we already have a bike (rented or own) we won't go any faster
			 * by having a second one.
			 */
			if (!s0.getNonTransitMode(options).equals(TraverseMode.WALK))
				return null;
			/*
			 * To rent a bike, we need to have BICYCLE in allowed modes.
			 */
			if (!options.getModes().contains(TraverseMode.BICYCLE))
				return null;
			EdgeNarrative en = new FixedModeEdge(this,
					s0.getNonTransitMode(options));

			StateEditor s1 = s0.edit(this, en);
			s1.incrementWeight(options.bikeRentalPickupCost);
			s1.incrementTimeInSeconds(options.bikeRentalPickupTime);
			s1.setBikeRenting(true);
			return s1.makeState();
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
		if (o instanceof RentABikeOnEdge) {
			RentABikeOnEdge other = (RentABikeOnEdge) o;
			return other.getFromVertex().equals(fromv)
					&& other.getToVertex().equals(tov);
		}
		return false;
	}

	public String toString() {
		return "RentABikeOnEdge(" + fromv + " -> " + tov + ")";
	}
}
