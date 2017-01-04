package ups_wargame_client.net_interface;

import ups_wargame_client.control.Command;
import ups_wargame_client.control.ClientController;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Implements runnable and runs as its own thread.
 * Listens to messages form the server and tries to parse them.
 * Sends parsed commands to controller.
 * @author Jan Vampol
 */
public class ClientInputThread implements Runnable {

    private Socket clientSocket = null;
    private BufferedReader streamIn = null;
    private boolean running;

    public ClientInputThread(Socket socket) {
        this.clientSocket = socket;
        this.running = true;
        this.open();
    }

    @Override
    public void run() {
        System.out.println("Input thread started ...");
        while (running) {
            try {
                String tmp = streamIn.readLine();
                this.handle(tmp);
            } catch (IOException ioe) {
                System.err.println("[IN]: Listening error: " + ioe.getMessage());
            }
        }
        System.out.println("Input thread stopped ...");
    }
    /**
     * Parses input string and sends it to controller as command if possible.
     * @param msg 
     */
    private void handle(String msg) {
        if(msg != null) {
            //System.out.println("[IN]: Incoming message: " + msg);
            Command tmp = Parser.parseInput(msg);
            ClientController.getInstance().addToInputQueue(tmp);
        }
    }

    public void open() {
        try {
            streamIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException ioe) {
            System.err.println("[IN]: Error getting input stream: " + ioe.getMessage());
        }
    }

    public void close() {
        try {
            if (streamIn != null) {
                streamIn.close();
            }
        } catch (IOException ioe) {
            System.err.println("[IN]: Error closing input stream: " + ioe.getMessage());
        }
    }

    public void stop() {
        System.out.println("Stopping input thread ...");
        this.running = false;
    }
}
