/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.control;

import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import ups_wargame_client.data_model.GameData;
import ups_wargame_client.data_model.IGameData;
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
    private GameData gameData = null;

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
        if (instance != null) {
            return instance;
        } else {
            System.out.println("Neni");
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
    
    @Override
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

    @Override
    public IGameData getGameData() {
        return this.gameData;
    }
    
    public void setupGameData(int rows, String[] map) {
        int cols = map[0].length();
        char[][] tmp = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            tmp[i] = map[i].toCharArray();
        }
        this.gameData = new GameData(rows, cols, tmp);
    }

    public void setUnits(List list) {
        this.gameData.setUnits(list);
        viewController.startGame(this.gameData);
    }

    public void updatePlayers(String playerBlu, String playerRed, int scoreBlu, int scoreRed, int unitID, char player) {
        this.gameData.updateScore(playerBlu, playerRed, scoreBlu, scoreRed, unitID, player);
    }
}
