package ups_wargame_client.control;

import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import javafx.application.Platform;
import ups_wargame_client.data_model.GameData;
import ups_wargame_client.data_model.IGameData;
import ups_wargame_client.views.IViewable;

/**
 * This is a static client controller. Only one instance of this class will be created every
 * time user opens a client. Instance of this controller is available in every module.
 * It serves to connect all modules together and control their work.
 * @author Jan Vampol
 */
public class ClientController implements IController {
    //static reference to ClientCOntroller only instance
    private static ClientController instance = null;
    //reference to CommandRunner used to run commands
    private final CommandRunner commandRunner;
    //reference to game viewer (GUI controller class)
    private IViewable viewController = null;
    //linked lists used as command queues
    private final LinkedList<Command> inputQueue;
    private final LinkedList<Command> outputQueue;
    //acknowledge queue works as a way to store commands needing acknowledgement
    //from the server, commands are retrieved when server responds with ACK or NACK
    private final LinkedList<Command> ackQueue;
    //reference to game engine, ticks of game engine are crucial for the rest of the application
    private GameEngine engine = null;
    //thread running the game engine
    private Thread engineThread = null;
    //unique ID of this client
    private int clientID;
    //instance of gamedata representing a game
    private GameData gameData = null;
    
    /**
     * Private constructor ensures only one instance can be created.
     */
    private ClientController() {
        commandRunner = new CommandRunner(this);
        inputQueue = new LinkedList<>();
        outputQueue = new LinkedList<>();
        ackQueue = new LinkedList<>();
        clientID = (new SecureRandom()).nextInt(Integer.MAX_VALUE - 1) + 1;
    }

    //static block which creates the instance at the start of runtime
    static {
        try {
            instance = new ClientController();
        } catch (Exception e) {
            throw new RuntimeException("Exception occured in creating ClientController singleton instance");
        }
    }
	
    /**
     * Method used to access the client controller from other classes.
     * @return 
     */
    public static ClientController getInstance() {
        if (instance != null) {
            return instance;
        } else {
            System.out.println("Nope");
            return null;
        }
    }
    
    /**
     * Method to connect controller with view.
     * Invoked at the creation of game window.
     * @param controller 
     */
    @Override
    public void setupView(IViewable controller) {
        this.viewController = controller;
        System.out.print("Controller setup!");
    }
    
    /**
     * Connects controller with instance of game engine.
     * Invoked at the creation of game window.
     * @param e 
     */
    public void setupEngine(GameEngine e) {
        this.engine = e;
        this.engineThread = new Thread(e);
        this.engineThread.start();

        this.inputQueue.clear();
        this.ackQueue.clear();
        this.outputQueue.clear();

        System.out.println("Engine setup!");
    }
    
    /**
     * Sets up the connection socket in command runner.
     * @param serverName
     * @param serverPort
     * @param playerName
     * @return 
     */
    public boolean setupConnection(String serverName, int serverPort, String playerName) {
        if (commandRunner.getSocket() == null) {
            return commandRunner.setupConnection(serverName, serverPort, playerName);
        } else {
            return false;
        }
    }
    
    /**
     * Sets up ID if user wishes to use custom ID
     * @param ID 
     */
    public void setupID(int ID) {
        this.clientID = ID;
    }
    
    /**
     * Starts the socket and input/output threads in command runner
     */
    public void startConnection() {
        commandRunner.startConnection();
    }
    
    /**
     * Stops connection socket and threads. Stops engine.
     * @return 
     */
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

    @Override
    public IGameData getGameData() {
        return this.gameData;
    }
    
    public IViewable getView() {
        return this.viewController;
    }
    /**
     * Get not-acked commands at the end of connection.
     * @return 
     */
    public String getAckString() {
        String s = "";

        while (!ackQueue.isEmpty()) {
            s += ackQueue.poll().toString();
            s += '\n';
        }

        return s;
    }
    
    /**
     * Creates game map from data sent by the server.
     * @param rows
     * @param map 
     */
    public void setupGameData(int rows, String[] map) {
        int cols = map[0].length();
        char[][] tmp = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            tmp[i] = map[i].toCharArray();
        }
        this.gameData = new GameData(rows, cols, tmp);
    }
    
    /**
     * Creates units from the data sent by the server
     * @param list 
     */
    public void setUnits(List list) {
        this.gameData.setUnits(list);
        Platform.runLater(() -> { //must be here otherwise it causes random nullpointer exceptions
            viewController.startGame(this.gameData);
        });
    }
    
    /**
     * Update player data from data sent by the server
     * @param playerBlu
     * @param playerRed
     * @param scoreBlu
     * @param scoreRed
     * @param unitID
     * @param attacking
     * @param player 
     */
    public void updatePlayers(String playerBlu, String playerRed, int scoreBlu, int scoreRed, int unitID, boolean attacking, char player) {
        this.gameData.updateScore(playerBlu, playerRed, scoreBlu, scoreRed, unitID, attacking, player);
    }
}
