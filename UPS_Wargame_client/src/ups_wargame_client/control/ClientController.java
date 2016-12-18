/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.control;

import java.security.SecureRandom;
import java.util.LinkedList;
import ups_wargame_client.views.IViewable;

/**
 *
 * @author sini
 */
public class ClientController implements IController {

    private static ClientController instance = null;

    private final CommandRunner commandRunner;

    private IViewable viewController = null;

    private final LinkedList<Command> inputQueue;
    private final LinkedList<Command> ackQueue;
    private final LinkedList<Command> outputQueue;

    private GameEngine engine = null;
    private Thread engineThread = null;
    private final int clientID;

    private ClientController() {
        commandRunner = new CommandRunner(this);
        inputQueue = new LinkedList<>();
        outputQueue = new LinkedList<>();
        ackQueue = new LinkedList<>();
        clientID = (new SecureRandom()).nextInt(Integer.MAX_VALUE - 1) + 1;
    }

    //static block initialization for exception handling
    static {
        try {
            instance = new ClientController();
        } catch (Exception e) {
            throw new RuntimeException("Exception occured in creating ClientController singleton instance");
        }
    }

    public static ClientController getInstance() {
        //System.out.println("YOU GET AN INSTANCE " + instance.hashCode());
        if (instance != null) {
            return instance;
        } else {
            return null;
        }
    }

    @Override
    public void setupView(IViewable controller) {
        this.viewController = controller;
        System.out.print("Controller setup!");
    }

    public void setupEngine(GameEngine e) {
        this.engine = e;
        this.engineThread = new Thread(e);
        this.engineThread.start();

        System.out.println("Engine setup!");
    }

    public boolean setupConnection(String serverName, int serverPort, String playerName) {
        if (commandRunner.getSocket() == null) {
            return commandRunner.setupConnection(serverName, serverPort, playerName);
        } else {
            return false;
        }
    }
    
    public void startConnection() {
        commandRunner.startConnection();
    }

    public boolean stopConnection() {
        if (commandRunner.getSocket() != null) {
            commandRunner.stopConnection();
            engine.stopRunning();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void sendCommand(Command c) { //from client to server
       commandRunner.runOutputCommand(c);
    }

    public void recieveCommand(Command c) { //from server to client
        commandRunner.runInputCommand(c);
    }
    
    public void addToAckQueue(Command c) {
        ackQueue.add(c);
    }

    @Override
    public synchronized void addToInputQueue(Command c) {
        inputQueue.add(c);
        synchronized (engine) {
            engine.notifyAll();
        }
    }

    @Override
    public synchronized void addToOutputQueue(Command c) {
        outputQueue.add(c);
        synchronized (engine) {
            engine.notifyAll();
        }
    }
    
    public Command retrieveAck() {
        return ackQueue.poll();
    }

    public synchronized Command retrieveInput() {

        return inputQueue.poll();

    }

    public synchronized Command retrieveOutput() {

        return outputQueue.poll();

    }
    
    public int getClientID() {
        return this.clientID;
    }
    
    public IViewable getView() {
        return this.viewController;
    }
    
    public String getAckString() {
        String s = "";
        
        while(!ackQueue.isEmpty()) {
            s += ackQueue.poll().toString();
            s += '\n';
        }
        
        return s;
    }
}
