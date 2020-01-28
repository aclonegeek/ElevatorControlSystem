package core;

import java.io.Serializable;
import java.time.LocalTime;

public class ElevatorData implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int elevatorId;
    private final int currentFloor;
    private final int floorButton;
    private final LocalTime time;

    public ElevatorData(final int elevatorId, final int currentFloor, final int floorButton,
            final LocalTime time) {
        this.elevatorId = elevatorId;
        this.currentFloor = currentFloor;
        this.floorButton = floorButton;
        this.time = time;
    }

    public int getElevatorId() {
        return this.elevatorId;
    }

    public int getCurrentFloor() {
        return this.currentFloor;
    }

    public int getFloorButton() {
        return this.floorButton;
    }

    public LocalTime getTime() {
        return this.time;
    }
}
