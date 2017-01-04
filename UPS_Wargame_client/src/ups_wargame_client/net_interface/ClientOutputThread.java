/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.net_interface;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 *
 * @author sini
 */
public class ClientOutputThread {

    private OutputStreamWriter streamOut = null;

    public ClientOutputThread(OutputStreamWriter writer) {
        streamOut = writer;
    }

    public void handle(String msg) {
        this.write(msg);
        //System.out.println("[OUT]: Sending message: " + msg);
    }

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
