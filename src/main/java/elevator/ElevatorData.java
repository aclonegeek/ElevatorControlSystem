package elevator;

import java.io.Serializable;
import java.time.LocalTime;

public class ElevatorData implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int elevatorId;
    private final int currentFloor;
    private final int destinationFloor;
    private final LocalTime time;

    public ElevatorData(final int elevatorId, final int currentFloor, final int destinationFloor,
            final LocalTime time) {
        this.elevatorId = elevatorId;
        this.currentFloor = currentFloor;
        this.destinationFloor = destinationFloor;
        this.time = time;
    }

    public int getElevatorId() {
        return this.elevatorId;
    }

    public int getCurrentFloor() {
        return this.currentFloor;
    }

    public int getDestinationFloor() {
        return this.destinationFloor;
    }

    public LocalTime getTime() {
        return this.time;
    }
}
