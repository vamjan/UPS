package ups_wargame_client.net_interface;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Sends output to server.
 * @author sini
 */
public class ClientOutputThread { //this name is confusing since this is not running its own thread... for now

    private OutputStreamWriter streamOut = null;

    public ClientOutputThread(OutputStreamWriter writer) {
        streamOut = writer;
    }

    public void handle(String msg) {
        this.write(msg);
        //System.out.println("[OUT]: Sending message: " + msg);
    }
    /**
     * Send string to server imediately.
     * @param msg 
     */
    public void write(String msg) {
        try {
            streamOut.write(msg);
            streamOut.flush();
        } catch (IOException ioe) {
            System.err.println("[OUT]: Unable to send: " + ioe.getMessage());
        }
    }

    public void stop() {
        try {
            if (streamOut != null) {
                streamOut.close();
            }
        } catch (IOException ioe) {
            System.err.println("[OUT]: Error closing ..." + ioe.getMessage());
        }
        System.out.println("[OUT]: Stopping output ...");
    }

}
