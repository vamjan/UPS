/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.net_interface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Queue;

/**
 *
 * @author sini
 */
public class CommandRunner {
    
    private Socket clientSocket = null;
    private BufferedReader console = null;
    private String serverName = null;
    private int serverPort = 0;
    
    private Queue<Command> commandQueue = null;
    public ClientOutputThread output = null; //placeholder private
    private ClientInputThread input = null;
    private Thread inputThread = null;
    
    public CommandRunner() { }
    
    public boolean executeCommand(Command command) {
        switch(command.type) {
            case CONNECT: break;
            case MESSAGE: break;
            case ERROR: break;
            default: return false;
        }
        return true;
    }
    
    public boolean setupConnection(String serverName, int serverPort) {
        System.out.println("Establishing connection. Please wait ...");
        try {
            clientSocket = new Socket(serverName, serverPort);
            System.out.println("Connected: " + clientSocket);
            input = new ClientInputThread(clientSocket);
            (inputThread = new Thread(input)).start();
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
    
    public void stopConnection() {
        System.out.println("Thread: " + Thread.currentThread().toString());
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
    
    public void runInputCommand() {
        
    }
    
    public void runOutputCommand() {
        
    }
    
    public Socket getSocket() {
        return this.clientSocket;
    }
}
