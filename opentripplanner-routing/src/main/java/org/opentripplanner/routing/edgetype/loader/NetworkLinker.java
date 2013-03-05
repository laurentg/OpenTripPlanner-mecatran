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

package org.opentripplanner.routing.edgetype.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.opentripplanner.common.IterableLibrary;
import org.opentripplanner.gbannotation.BikeRentalStationUnlinked;
import org.opentripplanner.gbannotation.StopUnlinked;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseMode;
import org.opentripplanner.routing.edgetype.StreetEdge;
import org.opentripplanner.routing.edgetype.factory.FindMaxWalkDistances;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.vertextype.BikeRentalStationVertex;
import org.opentripplanner.routing.vertextype.TransitStop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkLinker {

    private static Logger _log = LoggerFactory.getLogger(NetworkLinker.class);

    private Graph graph;

    private NetworkLinkerLibrary networkLinkerLibrary;

    public NetworkLinker(Graph graph, HashMap<Class<?>,Object> extra) {
        this.graph = graph;
        this.networkLinkerLibrary = new NetworkLinkerLibrary(graph, extra);
        networkLinkerLibrary.options = new RoutingRequest(TraverseMode.BICYCLE);
    }

    public NetworkLinker(Graph graph) {
        // we should be using Collections.emptyMap(), but it breaks Java's broken-ass type checker
        this(graph, new HashMap<Class<?>, Object>());
    }

    /**
     * Link the transit network to the street network. Connect each transit vertex to the nearest
     * Street edge with a StreetTransitLink.
     * 
     * @param index
     */
    public void createLinkage() {

        _log.debug("creating linkages...");
        // iterate over a copy of vertex list because it will be modified
        ArrayList<Vertex> vertices = new ArrayList<Vertex>();
        vertices.addAll(graph.getVertices());

        for (TransitStop ts : IterableLibrary.filter(vertices, TransitStop.class)) {
            // only connect transit stops that (a) are entrances, or (b) have no associated
            // entrances
            if (ts.isEntrance() || !ts.hasEntrances()) {
                boolean wheelchairAccessible = ts.hasWheelchairEntrance();
                if (!networkLinkerLibrary.connectVertexToStreets(ts, wheelchairAccessible).getResult()) {
                    _log.warn(graph.addBuilderAnnotation(new StopUnlinked(ts)));
                    networkLinkerLibrary.connectVertexToStreets(ts, wheelchairAccessible);
                }
            }
        }
        //remove replaced edges
        for (HashSet<StreetEdge> toRemove : networkLinkerLibrary.replacements.keySet()) {
            for (StreetEdge edge : toRemove) {
                edge.getFromVertex().removeOutgoing(edge);
                edge.getToVertex().removeIncoming(edge);
            }
        }
        
        // Do we really need this? Commenting out does seem to cause some slowdown. (AMB)
        networkLinkerLibrary.markLocalStops();
        FindMaxWalkDistances.find(graph);
        
        _log.debug("Linking bike rental stations...");
        for (BikeRentalStationVertex brsv : IterableLibrary.filter(vertices,
                BikeRentalStationVertex.class)) {
            if (!networkLinkerLibrary.connectVertexToStreets(brsv).getResult()) {
                _log.warn(graph.addBuilderAnnotation(new BikeRentalStationUnlinked(brsv)));
            }
        }
    }
}
