package global;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Globals {
    // Networking port/IP globals.
    public static final int SCHEDULER_PORT = 6969;
    public static final int ELEVATOR_PORT = 6971;
    public static final int FLOOR_PORT = 6972;
    public static InetAddress IP;

    // Networking data transfer globals.
    public static final int SUCCESS = 0;
    public static final int FAILURE = -1;
    public static final int FROM_FLOOR = 1;
    public static final int FROM_ELEVATOR = 2;
    public static final int FROM_SCHEDULER = 3;
    public static final int FROM_ARRIVAL_SENSOR = 4;

    // Floor globals.
    public static final int MAX_FLOORS = 22;
    public static final int FLOOR_HEIGHT = 260; // centimetres

    // Elevator globals.
    public static final int MAX_ELEVATORS = 4;

    static {
        try {
            IP = InetAddress.getLocalHost();
        } catch (final UnknownHostException e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    public static void sleep(final int ms) {
        try {
            Thread.sleep(ms);
        } catch (final InterruptedException e) {
            System.err.print(e);
        }
    }
}
