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
    public static final int SUCCESS = -1;
    public static final int FAILURE = 0;
    public static final int FROM_FLOOR = 1;
    public static final int FROM_ELEVATOR = 2;

    // Floor globals.
    public static final int MAX_FLOORS = 99;

    // Elevator globals.
    public static final int MAX_ELEVATORS = 9;
   
    static {
        try {
            IP = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.out.println(e);
            System.exit(1);
        }
    }
}
