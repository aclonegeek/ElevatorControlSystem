package floor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

import global.Globals;

public class FloorSystem {
    private DatagramSocket sendSocket;
    private FloorReader floorReader;
    private ArrayList<Floor> floors;
    private ArrayList<FloorData> requests;
    private int sleepTime;

    public static void main(String args[]) {
        new FloorSystem("/floorData.txt", 5000).run();
    }

    public FloorSystem(final String filename, final int sleepTime) {
        this.sleepTime = sleepTime;
        
        try {
            this.sendSocket = new DatagramSocket();
            this.floorReader = new FloorReader();

            // Create Floors.
            this.floors = new ArrayList<>();
            for (int i = 0; i < Globals.MAX_FLOORS; i++) {
                this.floors.add(new Floor(i));
            }

            // Read in FloorData from file.
            this.floorReader = new FloorReader();
            this.requests = this.floorReader.readFile(this.getClass().getResource(filename).getFile());
        } catch (final SocketException e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    public void run() {
        // Simulate a Floor handling the next request.
        // TODO: Handle requests in order based on their timestamp.
        while (requests.size() > 0) {
            final FloorData request = requests.remove(0);

            // Update Floor state.
            floors.get(request.getFloor()).setButtonState(request.getButtonState());

            // Construct byte array to send to Scheduler.
            final byte[] sendData = new byte[5];
            sendData[0] = Globals.FROM_FLOOR;
            sendData[1] = (byte) request.getFloor();
            sendData[2] = (byte) Floor.Request.REQUEST.ordinal();
            sendData[3] = (byte) request.getDestination();
            sendData[4] = (byte) request.getButtonState().ordinal();
            this.sendData(sendData);
            
            Globals.sleep(sleepTime);
        }
    }

    // Send data to Scheduler.
    public void sendData(final byte[] sendData) {
        final DatagramPacket sendPacket =
                new DatagramPacket(sendData, sendData.length, Globals.IP, Globals.SCHEDULER_PORT);

        System.out.println("Sending to port " + sendPacket.getPort() + ": " + Arrays.toString(sendData));

        try {
            this.sendSocket.send(sendPacket);
        } catch (final IOException e) {
            System.err.println(e);
            System.exit(1);
        }

        // Block until receive response from Scheduler.
        // TODO: Handle success/failure cases.
//        final byte[] receiveData = new byte[1];
//        final DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//        try {
//            this.receiveSocket.receive(receivePacket);
//        } catch (IOException e) {
//            System.err.println(e);
//            System.exit(1);
//        }
//
//        System.out.println("Received: " + Arrays.toString(receiveData) + "\n");
    }
    
    public ArrayList<Floor> getFloors() {
        return this.floors;
    }
    
    public ArrayList<FloorData> getRequests() {
        return this.requests;
    }
    
    /* METHODS USED FOR TESTING */
    public void closeSockets() {
        this.sendSocket.close();
    }
}
