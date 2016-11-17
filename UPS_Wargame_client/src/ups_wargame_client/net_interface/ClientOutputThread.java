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

    public void run() {
        /*this.start();

        

        System.out.println("Client run ended");*/
    }

    public void handle(String msg) {
        this.write(msg);
        System.out.println("Sending message: " + msg);
    }

    public void write(String msg) {
        try {
            streamOut.write(msg);
            streamOut.flush();
        } catch (IOException ioe) {
            System.err.println("Unable to send: " + ioe.getMessage());
        }
    }

    public void start() {

    }

    public void stop() {
        try {
            if (streamOut != null) {
                streamOut.close();
            }
        } catch (IOException ioe) {
            System.err.println("Error closing ..." + ioe.getMessage());
        }
        System.out.println("Stopping output ...");
    }

}
