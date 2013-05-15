package org.opentripplanner.routing.bike_rental;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;

public class BikeRentalStation implements Serializable {
    private static final long serialVersionUID = 8311460609708089384L;

    @XmlAttribute
    public String id;
    @XmlAttribute
    public String name;
    @XmlAttribute
    public double x, y; //longitude, latitude
    @XmlAttribute
    public int bikesAvailable;
    @XmlAttribute
    public int spacesAvailable;

    /**
     * Whether this station is static (usually coming from OSM data) or a real-time source. If no real-time data, users should take
     * bikesAvailable/spacesAvailable with a pinch of salt, as they are always the total capacity divided by two. Only the total is meaningful.
     */
    @XmlAttribute
    public boolean realTimeData = true;

    public boolean equals(Object o) {
        if (!(o instanceof BikeRentalStation)) {
            return false;
        }
        BikeRentalStation other = (BikeRentalStation) o;
        return other.id.equals(id);
    }
    
    public int hashCode() {
        return id.hashCode() + 1;
    }
}
