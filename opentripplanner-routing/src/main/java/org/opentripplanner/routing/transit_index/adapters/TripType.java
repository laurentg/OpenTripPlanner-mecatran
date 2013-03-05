



















/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (props, at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package org.opentripplanner.routing.transit_index.adapters;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.Trip;

@XmlRootElement(name = "trip")
public class TripType {

    public TripType(Trip obj) {
        this.id = obj.getId();
        this.serviceId = obj.getServiceId();
        this.tripShortName = obj.getTripShortName();
        this.tripHeadsign = obj.getTripHeadsign();
        this.routeId = obj.getRoute().getId();
        this.directionId = obj.getDirectionId();
        this.blockId = obj.getBlockId();
        this.shapeId = obj.getShapeId();
        this.wheelchairAccessible = obj.getWheelchairAccessible();
        this.tripBikesAllowed = obj.getTripBikesAllowed();
        this.route = obj.getRoute();
    }

    public TripType(Trip obj, Boolean extended) {
        this.id = obj.getId();
        this.tripShortName = obj.getTripShortName();
        this.tripHeadsign = obj.getTripHeadsign();
        if (extended != null && extended.equals(true)) {
            this.route = obj.getRoute();
            this.serviceId = obj.getServiceId();
            this.routeId = obj.getRoute().getId();
            this.directionId = obj.getDirectionId();
            this.blockId = obj.getBlockId();
            this.shapeId = obj.getShapeId();
            this.wheelchairAccessible = obj.getWheelchairAccessible();
            this.tripBikesAllowed = obj.getTripBikesAllowed();
        }
    }

    public TripType() {
    }

    @XmlJavaTypeAdapter(AgencyAndIdAdapter.class)
    AgencyAndId id;

    @XmlJavaTypeAdapter(AgencyAndIdAdapter.class)
    AgencyAndId serviceId;

    @XmlAttribute
    String tripShortName;

    @XmlAttribute
    String tripHeadsign;

    @XmlJavaTypeAdapter(AgencyAndIdAdapter.class)
    AgencyAndId routeId;

    @XmlAttribute
    String directionId;

    @XmlAttribute
    String blockId;

    @XmlJavaTypeAdapter(AgencyAndIdAdapter.class)
    AgencyAndId shapeId;

    @XmlAttribute
    Integer wheelchairAccessible;

    @XmlAttribute
    Integer tripBikesAllowed;

    Route route;

    public Route getRoute() {
        return route;
    }
}
