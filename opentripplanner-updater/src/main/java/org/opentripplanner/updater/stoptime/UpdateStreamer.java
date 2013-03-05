package org.opentripplanner.updater.stoptime;

import java.util.List;

import org.opentripplanner.routing.trippattern.Update;

public interface UpdateStreamer {

    /**
     * Wait for one message to arrive, and decode it into an UpdateList. Blocking call. 
     * @return an UpdateList potentially containing updates for several different trips,
     *         or null if an exception occurred while processing the message
     */
    public List<Update> getUpdates();
    
}
