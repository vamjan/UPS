/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.net_interface;

import com.sun.javafx.image.impl.IntArgb;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author sini
 */
public class ClientOutputThread {

    private Socket socket = null;
    private BufferedReader console = null;
    private OutputStreamWriter streamOut = null;
    private ClientInputThread client = null;
    private Thread inputThread = null;
    private boolean running = true;
    private String serverName = null;
    private int serverPort = 0;

    public ClientOutputThread() {
        console = new BufferedReader(new InputStreamReader(System.in));
        this.run();
    }

    public void run() {
        String tmp = null;
        try {
            tmp = console.readLine();
            //kontrola hosta
            serverName = tmp;
            tmp = console.readLine();
            serverPort = Integer.parseInt(tmp);
        } catch (IOException ioe) {
            System.out.println("Input error" + ioe.getMessage());
            System.exit(0);
        } catch (NumberFormatException nfe) {
            System.out.println("Not a number" + nfe.getMessage());
            System.exit(0);
        } finally {
            this.start();
        }

        while (running) {
            try {
                tmp = console.readLine();
                if (!tmp.equals("exit")) {
                    streamOut.write(tmp);
                    streamOut.flush();
                } else {
                    this.stop();
                }
            } catch (IOException ioe) {
                System.out.println("Sending error: " + ioe.getMessage());
                //this.stop();
            }
        }
    
        
        System.out.println("Client run ended");
    }

    public void handle(String msg) throws IOException{
        if (msg == null || msg.equals("exit")) {
            System.out.println("Shutting down ...");
            this.stop();
        } else if (msg.equals("Who are you?")) {
            streamOut.write("I am your doom!");
            streamOut.flush();
        } else {
            System.out.println("Incoming messsage: " + msg);
        }
    }

    public void start() {
        System.out.println("Establishing connection. Please wait ...");
        try {
            socket = new Socket(serverName, serverPort);
            System.out.println("Connected: " + socket);
            client = new ClientInputThread(this, socket);
            (inputThread = new Thread(client)).start();
            streamOut = new OutputStreamWriter(socket.getOutputStream());
        } catch (UnknownHostException uhe) {
            System.out.println("Host unknown: " + uhe.getMessage());
        } catch (IOException ioe) {
            System.out.println("Unexpected exception: " + ioe.getMessage());
        }

        running = true;
    }

    public void stop() {
        running = false;

        try {
            if (streamOut != null) {
                streamOut.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ioe) {
            System.out.println("Error closing ...");
        }

        client.stop();
        client.close();
    }

}
