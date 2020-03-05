package floor;

import java.net.DatagramSocket;
import java.net.SocketException;

class FloorSystem {
    private DatagramSocket receiveSocket;
    private DatagramSocket sendSocket;

    public static void main(String args[]) {
        new FloorSystem().run();
    }

    public FloorSystem() {
        try {
            this.receiveSocket = new DatagramSocket(Globals.FLOOR_PORT);
            this.sendSocket = new DatagramSocket();
        } catch (SocketException e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    private void run() {

    }
}
