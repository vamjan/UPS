/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.net_interface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Queue;

/**
 *
 * @author sini
 */
public class ClientController {
    
    private static ClientController instance = null;
    
    private Socket clientSocket = null;
    private BufferedReader console = null;
    private String serverName = null;
    private int serverPort = 0;
    
    private CommandRunner commandRunner = null;
    private Queue<Command> commandQueue = null;
    private ClientOutputThread output = null;
    private ClientInputThread input = null;
    private Thread inputThread = null;
    
    
    private ClientController() {
        console = new BufferedReader(new InputStreamReader(System.in));
    }
    
    //static block initialization for exception handling
    static{
        try{
            instance = new ClientController();
        }catch(Exception e){
            throw new RuntimeException("Exception occured in creating singleton instance");
        }
    }
    
    public static ClientController getInstance() {
        System.out.println("YOU GET AN INSTANCE " + instance.hashCode());
        if(instance != null)
            return instance;
        else 
            return null;
    }
    
    public boolean startConnection(String serverName, int serverPort) {
        if(clientSocket == null)
            return setupConnection(serverName, serverPort);
        else
            return false;
    }
    
    private boolean setupConnection(String serverName, int serverPort) {
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
        input.stop();
        output.stop();
        
        try {
            clientSocket.close();
        } catch (IOException ioe) {
            System.err.println("Unable to close socket: " + ioe.getMessage());
        }
        
        input.close();
    }
    
    public void sendCommand(String s) { //placeholder
        output.write(s);
    }
}
