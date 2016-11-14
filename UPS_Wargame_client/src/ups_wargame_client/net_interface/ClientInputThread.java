/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.net_interface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 *
 * @author sini
 */
public class ClientInputThread implements Runnable {

    private Socket clientSocket = null;
    private ClientOutputThread client = null;
    private BufferedReader streamIn = null;
    private boolean running;

    public ClientInputThread(ClientOutputThread client, Socket socket) {
        this.clientSocket = socket;
        this.client = client;
        this.running = true;
        this.open();
    }

    @Override
    public void run() {
        /*try {
            byte[] messageBuffer = null;
            short bytesToRead, bytesRead;
            String incomingMsg = null;

            while (true) {
                messageBuffer = new byte[1024];
                incomingMsg = new String();
                bytesRead = 0;
                bytesToRead = streamIn.readShort();
                while (bytesRead < bytesToRead) {
                    int tmp = streamIn.read(messageBuffer, 0, messageBuffer.length);
                    incomingMsg += new String(messageBuffer);
                    if (tmp > 0) {
                        bytesRead += tmp;
                    } else {
                        break;
                    }
                }
                System.out.println(incomingMsg);
            }

        } catch (IOException e) {

        }*/
        System.out.println("Input thread started ...");
        while (running) {
            try {
                String tmp = streamIn.readLine();
                client.handle(tmp);
            } catch (IOException ioe) {
                System.out.println("Listening error: " + ioe.getMessage());
            }
        }
    }

    public void open() {
        try {
            streamIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException ioe) {
            System.out.println("Error getting input stream: " + ioe);
            client.stop();
        }
    }

    public void close() {
        try {
            streamIn.close();
        } catch (IOException ioe) {
            System.out.println("Error closing input stream: " + ioe);
        }
    }

    public void stop() {
        System.out.println("Stopping input thread ...");
        this.running = false;
    }
}
