package floor;

import classes.RunnableFloorSystem;
import global.Globals;
import junit.framework.TestCase;

public class FloorSystemTest extends TestCase {
    public void testFloorSystem() {
        final RunnableFloorSystem runnableFloorSystem =
                new RunnableFloorSystem("/data.txt", 10);
        final FloorSystem floorSystem = runnableFloorSystem.getFloorSystem();
        
        // Verify initial FloorSystem state.
        assertEquals(Globals.MAX_FLOORS, floorSystem.getFloors().size());
        assertEquals(4, floorSystem.getRequests().size());
        
        // Begin sending requests to Scheduler.
        new Thread(runnableFloorSystem).start();
        Globals.sleep(1000);
        
        // Verify all requests are sent to the Scheduler.
        assertEquals(0, floorSystem.getRequests().size());
        
        floorSystem.closeSockets();
    }
}
