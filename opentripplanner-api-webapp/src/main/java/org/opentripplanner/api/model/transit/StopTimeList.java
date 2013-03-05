package org.opentripplanner.api.model.transit;

import java.util.HashSet;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.onebusaway.gtfs.model.Route;
import org.opentripplanner.routing.transit_index.adapters.RouteAdapter;

@XmlRootElement(name = "StopTimeList")
public class StopTimeList {

    @JsonSerialize(include = Inclusion.NON_NULL)
    @XmlElementWrapper
    @XmlElement(name = "route")
    @XmlJavaTypeAdapter(RouteAdapter.class)
    public HashSet<Route> routes;

    @XmlElements(value = @XmlElement(name = "stopTime"))
    public List<StopTime> stopTimes;

}
