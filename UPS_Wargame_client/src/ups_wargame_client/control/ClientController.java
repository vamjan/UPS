/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.control;

import java.util.LinkedList;
import java.util.Queue;
import ups_wargame_client.net_interface.Command;
import ups_wargame_client.net_interface.CommandRunner;
import ups_wargame_client.net_interface.IController;
import ups_wargame_client.views.IViewable;

/**
 *
 * @author sini
 */
public class ClientController implements IController {

    private static ClientController instance = null;

    private CommandRunner commandRunner = null;

    private IViewable viewController = null;

    private LinkedList<String> inputQueue = null;
    //private LinkedList<Command> inputQueue = null;
    private LinkedList<Command> outputQueue = null;

    GameEngine engine = null;
    Thread engineThread = null;

    private ClientController() {
        commandRunner = new CommandRunner();
        inputQueue = new LinkedList<>();
        outputQueue = new LinkedList<>();
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
        System.out.println("YOU GET AN INSTANCE " + instance.hashCode());
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

    public boolean startConnection(String serverName, int serverPort) {
        System.out.println("Thread: " + Thread.currentThread().toString());
        if (commandRunner.getSocket() == null) {
            return commandRunner.setupConnection(serverName, serverPort);
        } else {
            return false;
        }
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
    public void sendCommand(String msg) { //placeholder
        System.out.println("Thread: " + Thread.currentThread().toString());
        commandRunner.output.write(msg);
    }

    public void recieveMessage(String msg) {
        if (viewController != null) {
            viewController.showServerMessage("[Server]: ", msg);
        }
    }

    public synchronized void addToInputQueue(String c) { //command c
        inputQueue.add(c);
        synchronized (engine) {
            engine.notifyAll();
        }
    }

    public synchronized void addToOutputQueue(Command c) {
        outputQueue.add(c);
        synchronized (engine) {
            engine.notifyAll();
        }
    }

    public synchronized String retrieveInput() {

        return inputQueue.poll();

    }

    public synchronized Command retrieveOutput() {

        return outputQueue.poll();

    }
}
