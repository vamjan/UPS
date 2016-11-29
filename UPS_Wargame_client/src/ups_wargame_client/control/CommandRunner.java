/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import ups_wargame_client.net_interface.ClientInputThread;
import ups_wargame_client.net_interface.ClientOutputThread;

/**
 *
 * @author sini
 */
public class CommandRunner {

    private Socket clientSocket = null;
    private BufferedReader console = null;

    private ClientController controller = null;
    private ClientOutputThread output = null;
    private ClientInputThread input = null;
    private Thread inputThread = null;

    public CommandRunner(ClientController controller) {
        this.controller = controller;
    }

    private boolean executeCommand(Command command, boolean lever) {
        if (lever) { //incoming
            switch (command.type) {
                case CONNECT:
                    break;
                case MESSAGE:
                    controller.getView().showServerMessage("[Server]: ", command.toString()); //onlz message in the future
                    break;
                case ACK:
                    Command tmp = controller.retrieveAck();
                    if(tmp == null) System.err.println("No ACK required");
                    else System.out.println(tmp.toString() + " ACK");
                    break;
                case NACK:
                    break;
                default:
                    return false;
            }
        } else { //outgoing
            switch (command.type) {
                case CONNECT:
                    break;
                case MESSAGE:
                    sendMessage(command);
                    controller.addToAckQueue(command);
                    break;
                case ACK:
                    sendMessage(command);
                    break;
                case NACK:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public boolean setupConnection(String serverName, int serverPort) {
        System.out.println("Establishing connection. Please wait ...");
        try {
            clientSocket = new Socket(serverName, serverPort);
            System.out.println("Connected: " + clientSocket);
            input = new ClientInputThread(clientSocket);
            inputThread = new Thread(input);
            output = new ClientOutputThread(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (UnknownHostException uhe) {
            System.err.println("Host unknown: " + uhe.getMessage());
            return false;
        } catch (IOException ioe) {
            System.err.println("Unexpected exception: " + ioe.getMessage());
            return false;
        }
        return true;
    }

    public void startConnection() {
        System.out.println("Starting connection...");
        inputThread.start();
    }

    public void stopConnection() {
        System.out.println("Stopping connection...");
        input.stop();
        output.stop();

        try {
            clientSocket.close();
            clientSocket = null;
        } catch (IOException ioe) {
            System.err.println("Unable to close socket: " + ioe.getMessage());
        }

        input.close();
    }

    public void runInputCommand(Command c) {
        executeCommand(c, true);
    }

    public void runOutputCommand(Command c) {
        executeCommand(c, false);
    }

    private void sendMessage(Command c) {
        output.handle(c.toString());
    }

    public Socket getSocket() {
        return this.clientSocket;
    }
}
