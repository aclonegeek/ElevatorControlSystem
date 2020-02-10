package elevator;

import java.io.Serializable;

/*
 * Class consisting of ElevatorData and an ElevatorAction
 */
public class ElevatorEvent implements Serializable {
    private static final long serialVersionUID = 1L;
    private final ElevatorData elevatorData;
    private final ElevatorAction elevatorAction;

    public ElevatorEvent(final ElevatorData elevatorData, final ElevatorAction elevatorAction) {
        this.elevatorData = elevatorData;
        this.elevatorAction = elevatorAction;
    }

    public ElevatorData getElevatorData() {
        return this.elevatorData;
    }

    public ElevatorAction getElevatorAction() {
        return this.elevatorAction;
    }
}
