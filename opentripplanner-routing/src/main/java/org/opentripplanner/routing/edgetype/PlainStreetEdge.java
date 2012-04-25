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

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Set;

import org.opentripplanner.common.geometry.PackedCoordinateSequence;
import org.opentripplanner.routing.core.NoThruTrafficState;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.core.StateEditor;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.core.TraverseOptions;
import org.opentripplanner.routing.patch.Alert;
import org.opentripplanner.routing.util.ElevationUtils;
import org.opentripplanner.routing.util.SlopeCosts;
import org.opentripplanner.routing.vertextype.StreetVertex;
import org.opentripplanner.routing.vertextype.TurnVertex;

import com.vividsolutions.jts.geom.LineString;

/**
 * This represents a street segment. This is unusual in an edge-based graph, but happens when we
 * have to split a set of turns to accommodate a transit stop.
 * 
 * @author novalis
 * 
 */
public class PlainStreetEdge extends StreetEdge {

    private static final long serialVersionUID = 1L;

    private PackedCoordinateSequence elevationProfile;

    private double length;

    private LineString geometry;

    private String name;

    private double slopeSpeedEffectiveLength;

    private double bicycleSafetyEffectiveLength;

    private double slopeWorkCost;

    private boolean wheelchairAccessible = true;

    private double maxSlope;

    private StreetTraversalPermission permission;

    private String id;

    private boolean crossable = true;

    private boolean slopeOverride = false;

    public boolean back;
    
    private boolean roundabout = false;

    private Set<Alert> notes;

    private boolean hasBogusName;

    private boolean noThruTraffic;

    /**
     * This street is a staircase
     */
    private boolean stairs;

    private Set<Alert> wheelchairNotes;

    /**
     * No-arg constructor used only for customization -- do not call this unless you know
     * what you are doing
     */
    public PlainStreetEdge() {
        super(null, null);
    }

    // Presently, we have plainstreetedges that connect both IntersectionVertexes and
    // TurnVertexes
    public PlainStreetEdge(StreetVertex v1, StreetVertex v2, LineString geometry, 
            String name, double length,
            StreetTraversalPermission permission, boolean back) {
        super(v1, v2);
        this.geometry = geometry;
        this.length = length;
        slopeSpeedEffectiveLength = length;
        bicycleSafetyEffectiveLength = length;
        slopeWorkCost = length;
        this.name = name;
        this.permission = permission;
        this.back = back;
    }

    @Override
    public boolean canTraverse(TraverseOptions options) {
        if (options.wheelchairAccessible) {
            if (!wheelchairAccessible) {
                return false;
            }
            if (maxSlope > options.maxSlope) {
                return false;
            }
        }
        if (options.getModes().getWalk() && permission.allows(StreetTraversalPermission.PEDESTRIAN)) {
            return true;
        }
        if (options.getModes().getBicycle() && permission.allows(StreetTraversalPermission.BICYCLE)) {
            return true;
        }
        if (options.getModes().getCar() && permission.allows(StreetTraversalPermission.CAR)) {
            return true;
        }
        return false;
    }
    
    private boolean canTraverse(TraverseOptions options, TraverseMode mode) {
        if (options.wheelchairAccessible) {
            if (!wheelchairAccessible) {
                return false;
            }
            if (maxSlope > options.maxSlope) {
                return false;
            }
        }
        if (mode == TraverseMode.WALK && permission.allows(StreetTraversalPermission.PEDESTRIAN)) {
            return true;
        }
        if (mode == TraverseMode.BICYCLE && permission.allows(StreetTraversalPermission.BICYCLE)) {
            return true;
        }
        if (mode == TraverseMode.CAR && permission.allows(StreetTraversalPermission.CAR)) {
            return true;
        }
        return false;
    }

    @Override
    public PackedCoordinateSequence getElevationProfile() {
        return elevationProfile;
    }

    @Override
    public boolean setElevationProfile(PackedCoordinateSequence elev, boolean computed) {
        if (elev == null || elev.size() < 2) {
            return false;
        }
        if (slopeOverride && !computed) {
            return false;
        }

        elevationProfile = elev;

        //compute the various costs of the elevation changes
        double lengthMultiplier = ElevationUtils.getLengthMultiplierFromElevation(elev);
        length *= lengthMultiplier;
        bicycleSafetyEffectiveLength *= lengthMultiplier;

        SlopeCosts costs = ElevationUtils.getSlopeCosts(elev, permission.allows(StreetTraversalPermission.CAR));
        slopeSpeedEffectiveLength = costs.slopeSpeedEffectiveLength;
        maxSlope = costs.maxSlope;
        slopeWorkCost = costs.slopeWorkCost;
        bicycleSafetyEffectiveLength += costs.slopeSafetyCost;
        return costs.flattened;
    }

    @Override
    public double getDistance() {
        return length;
    }

    @Override
    public LineString getGeometry() {
        return geometry;
    }

    @Override
    public TraverseMode getMode() {
        return TraverseMode.WALK;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public State traverse(State s0) {
        return doTraverse(s0, s0.getOptions());
    }

    private State doTraverse(State s0, TraverseOptions options) {
    	TraverseMode traverseMode = s0.getNonTransitMode(options);
        if (!canTraverse(options, traverseMode)) {
            if (traverseMode == TraverseMode.BICYCLE) {
            	// try walking bike since you can't ride here
                return doTraverse(s0, options.getWalkingOptions());
            }
            return null;
        }
        double speed = options.getSpeed(s0.getNonTransitMode(options));
        double time = length / speed;
        double weight;
        if (options.wheelchairAccessible) {
            weight = slopeSpeedEffectiveLength / speed;
        } else if (s0.getNonTransitMode(options).equals(TraverseMode.BICYCLE)) {
            time = slopeSpeedEffectiveLength / speed;
            switch (options.optimizeFor) {
            case SAFE:
            	weight = bicycleSafetyEffectiveLength / speed;
            	break;
            case GREENWAYS:            	
                weight = bicycleSafetyEffectiveLength / speed;
                if (bicycleSafetyEffectiveLength / length <= TurnVertex.GREENWAY_SAFETY_FACTOR) {
                	//greenways are treated as even safer than they really are
                	weight *= 0.66;
                }
                break;
            case FLAT:
                /* see notes in StreetVertex on speed overhead */
                weight = length / speed + slopeWorkCost;
                break;
            case QUICK:
                weight = slopeSpeedEffectiveLength / speed;
                break;
            case TRIANGLE:
                double quick = slopeSpeedEffectiveLength;
                double safety = bicycleSafetyEffectiveLength;
                double slope = slopeWorkCost;
                weight = quick * options.getTriangleTimeFactor() + slope * options.getTriangleSlopeFactor() + safety * options.getTriangleSafetyFactor();
                weight /= speed;
                break;
            default:
                weight = length / speed;
            }
        } else {
            weight = time;
        }
        if (isStairs()) {
            weight *= options.stairsReluctance;
        } else {
            weight *= options.walkReluctance;
        }
        FixedModeEdge en = new FixedModeEdge(this, s0.getNonTransitMode(options));
        if (wheelchairNotes != null && options.wheelchairAccessible) {
            en.addNotes(wheelchairNotes);
        }
        StateEditor s1 = s0.edit(this, en);

        switch (s0.getNoThruTrafficState()) {
        case INIT:
            if (isNoThruTraffic()) {
                s1.setNoThruTrafficState(NoThruTrafficState.IN_INITIAL_ISLAND);
            } else {
                s1.setNoThruTrafficState(NoThruTrafficState.BETWEEN_ISLANDS);
            }
            break;
        case IN_INITIAL_ISLAND:
            if (!isNoThruTraffic()) {
                s1.setNoThruTrafficState(NoThruTrafficState.BETWEEN_ISLANDS);
            }
            break;
        case BETWEEN_ISLANDS:
            if (isNoThruTraffic()) {
                s1.setNoThruTrafficState(NoThruTrafficState.IN_FINAL_ISLAND);
            }
            break;
        case IN_FINAL_ISLAND:
            if (!isNoThruTraffic()) {
                // we have now passed entirely through a no thru traffic region,
                // which is
                // forbidden
                return null;
            }
            break;
        }

        if (traverseMode != TraverseMode.CAR)
            s1.incrementWalkDistance(length);
        s1.incrementTimeInSeconds((int) Math.ceil(time));
        s1.incrementWeight(weight);
        if (s1.weHaveWalkedTooFar(options))
            return null;
        
        return s1.makeState();
    }

    @Override
    public double weightLowerBound(TraverseOptions options) {
        return timeLowerBound(options) * options.walkReluctance;
    }
    
    @Override
    public double timeLowerBound(TraverseOptions options) {
        return this.length / options.getSpeedHigherBound();
    }
        
    public void setSlopeSpeedEffectiveLength(double slopeSpeedEffectiveLength) {
        this.slopeSpeedEffectiveLength = slopeSpeedEffectiveLength;
    }

    public double getSlopeSpeedEffectiveLength() {
        return slopeSpeedEffectiveLength;
    }

    public void setSlopeWorkCost(double slopeWorkCost) {
        this.slopeWorkCost = slopeWorkCost;
    }

    public double getWorkCost() {
        return slopeWorkCost;
    }

    public void setBicycleSafetyEffectiveLength(double bicycleSafetyEffectiveLength) {
        this.bicycleSafetyEffectiveLength = bicycleSafetyEffectiveLength;
    }

    public double getBicycleSafetyEffectiveLength() {
        return bicycleSafetyEffectiveLength;
    }

    public double getLength() {
        return length;
    }

    public StreetTraversalPermission getPermission() {
        return permission;
    }

    public void setPermission(StreetTraversalPermission permission) {
        this.permission = permission;
    }

    public void setWheelchairAccessible(boolean wheelchairAccessible) {
        this.wheelchairAccessible = wheelchairAccessible;
    }

    public boolean isWheelchairAccessible() {
        return wheelchairAccessible;
    }

    public String getId() {
        return id;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        id = null; 
        out.defaultWriteObject();
    }

    public boolean isCrossable() {
        return crossable;
    }

    public boolean getSlopeOverride() {
        return slopeOverride ;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public PackedCoordinateSequence getElevationProfile(double start, double end) {
        return ElevationUtils.getPartialElevationProfile(elevationProfile, start, end);
    }

    public void setSlopeOverride(boolean slopeOverride) {
        this.slopeOverride = slopeOverride;
    }

    public void setRoundabout(boolean roundabout) {
        this.roundabout = roundabout;
    }

    public boolean isRoundabout() {
        return roundabout;
    }

    public Set<Alert> getNotes() {
    	return notes;
    }
    
    public void setNote(Set<Alert> notes) {
    	this.notes = notes;
    }
    
    @Override
    public String toString() {
        return "PlainStreetEdge(" + fromv + " -> " + tov + ")";
    }

    public boolean hasBogusName() {
        return hasBogusName;
    }
    
    public void setBogusName(boolean hasBogusName) {
        this.hasBogusName = hasBogusName;
    }

    public void setNoThruTraffic(boolean noThruTraffic) {
        this.noThruTraffic = noThruTraffic;
    }

    public boolean isNoThruTraffic() {
        return noThruTraffic;
    }

    public boolean isStairs() {
        return stairs;
    }

    public void setStairs(boolean stairs) {
        this.stairs = stairs;
    }

    public void setName(String name) {
       this.name = name;
    }

    public void setWheelchairNote(Set<Alert> wheelchairNotes) {
        this.wheelchairNotes = wheelchairNotes;
    }

    public Set<Alert> getWheelchairNotes() {
        return wheelchairNotes;
    }
}
