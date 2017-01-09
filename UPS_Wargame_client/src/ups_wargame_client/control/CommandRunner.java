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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import ups_wargame_client.data_model.Unit;
import ups_wargame_client.net_interface.ClientInputThread;
import ups_wargame_client.net_interface.ClientOutputThread;
import ups_wargame_client.net_interface.MsgType;

/**
 *
 * @author sini
 */
public class CommandRunner {

    private String playerName = null;
    private Socket clientSocket = null;
    private BufferedReader console = null;

    private ClientController controller = null;
    private ClientOutputThread output = null;
    private ClientInputThread input = null;
    private Thread inputThread = null;
    
    /**
     * Constructor sets up a controller reference
     * @param controller 
     */
    public CommandRunner(ClientController controller) {
        this.controller = controller;
    }
    
    /**
     * Method processing all the commands. Can be used to process input or output
     * commands using lever parameter. Behaves differently for each command and 
     * lever configuration.
     * @param command
     * @param lever
     * @return
     * @throws NumberFormatException 
     */
    private boolean executeCommand(Command command, boolean lever) throws NumberFormatException {
        Command tmp;

        if (lever) { //incoming
            switch (command.type) {
                case CONNECT: //never comming
                    break;
                case DISCONNECT: //my opponent left, will i wait or end?
                    Platform.runLater(() -> {
                        controller.getView().showWait();
                    });
                    break;
                case RECONNECT: //show prompt to user his opponent left
                    Platform.runLater(() -> {
                        controller.getView().showReconnect(Integer.parseInt((String) command.data[0]));
                    });
                    break;
                case MESSAGE: //show server message
                    controller.getView().showServerMessage("[Server]: ", command.toString()); //onlz message in the future
                    break;
                case ACK: //acknowledge first command from ack queue
                    tmp = controller.retrieveAck();
                    if (tmp == null) {
                        System.err.println("No ACK required");
                    } else {
                        ackCommand(tmp);
                    }
                    break;
                //case POKE: nuffin
                //    break;
                case NACK: //not acknowledge first command from ack queue
                    tmp = controller.retrieveAck();
                    if (tmp == null) {
                        System.err.println("No NACK required");
                    } else {
                        nackCommand(tmp);
                    }
                    break;
                case GET_SERVER: //set lobby list sent form server
                    Platform.runLater(() -> {
                        controller.getView().setLobbyList(parseServerData(command));
                    });
                    break;
                case CREATE_LOBBY: //not happening
                    break;
                case JOIN_LOBBY: //expand and update lobby info in viewer
                    Platform.runLater(() -> {
                        controller.getView().updateLobby((Lobby) parseServerData(command).get(0));
                    });
                    break;
                case LEAVE_LOBBY: //not happening
                    break;
                case START: //send parsed map info to game data
                    String[] stringArray = Arrays.copyOf(command.data, command.data.length, String[].class);
                    controller.setupGameData(command.length, stringArray);
                    break;
                case UNITS: //send parsed unit info to game data, start game
                    controller.setUnits(parseUnits(command));
                    break;
                case UPDATE: //send parsed turn info to gamedata
                    this.updateScore(command);
                    controller.getGameData().setUpdated(true);
                    break;
                case MOVE: //send move command to game data
                    Unit target = controller.getGameData().getUnitByID(Integer.parseInt((String) command.data[0]));
                    int r = Integer.parseInt((String) command.data[1]);
                    int q = Integer.parseInt((String) command.data[2]);
                    target.move(r, q);
                    controller.getGameData().setUpdated(true);
                    break;
                case ATTACK: //send attack command to game data
                    //Unit attacker = controller.getGameData().getUnitByID(Integer.parseInt((String) command.data[0]));
                    Unit attacked = controller.getGameData().getUnitByID(Integer.parseInt((String) command.data[1]));
                    int newHealth = Integer.parseInt((String) command.data[2]);
                    attacked.attack(newHealth);
                    controller.getGameData().setUpdated(true);
                    break;
                case CAPTURE: //send capture command to game data
                    Unit source = controller.getGameData().getUnitByID(Integer.parseInt((String) command.data[0]));
                    Unit captured = controller.getGameData().getUnitByID(Integer.parseInt((String) command.data[1]));
                    source.capture(captured);
                    controller.getGameData().setUpdated(true);
                    break;
                case END: //show winner and close the game view
                    String winner = (String) command.data[0];
                    Platform.runLater(() -> {
                        controller.getView().endGame(winner);
                    });
                    break;
                default:
                    return false;
            }
        } else { //outgoing
            if (command.type == MsgType.DISCONNECT) { //DISCONNECT command only exits the client
                Platform.runLater(() -> {
                    controller.getView().backToStart();
                });
            } else {
                sendMessage(command); //send command and add to ack queue id necessary
                if (Command.requiresAck(command)) {
                    controller.addToAckQueue(command);
                }
            }
        }
        return true;
    }
    
    /**
     * Shows server positive reaction to command.
     * @param command 
     */
    private void ackCommand(Command command) {
        switch (command.type) {
            case CREATE_LOBBY: //lobby created
                controller.getView().acknowledge();
                break;
            case JOIN_LOBBY: //lobby joined
                Platform.runLater(() -> {
                    controller.getView().showLobby();
                    controller.getView().toggleConnected();
                });
                break;
            case LEAVE_LOBBY: //lobby left
                Platform.runLater(() -> {
                    controller.getView().hideLobby();
                    controller.getView().toggleConnected();
                });
                break;
            case MOVE: //unit moved, you can now attack... also check for capture
                controller.getGameData().setAttacking(true);
                controller.getGameData().setUpdated(true);
                controller.getGameData().capture((int)command.data[0], (int)command.data[1], (int)command.data[2]); //safe conversion since command never left client
                break;
            case ATTACK: //unit attacked, advance to next turn
                controller.getGameData().setAttacking(false);
                controller.addToOutputQueue(new Command(controller.getClientID(), MsgType.SKIP, (short) 0, null));
                break;
        }
    }
    
    /**
     * Shows server negative reaction to command.
     * @param command 
     */
    private void nackCommand(Command command) {
        switch (command.type) {
            case MOVE: //cant move, try again and show negative message
                controller.getGameData().setAttacking(false);
                controller.getGameData().setUpdated(true);
                Platform.runLater(() -> {
                    controller.getView().showLobbyMessage("[Server]: ", "Can't move there.");
                });
                break;
            case ATTACK: //cant attack
                controller.getGameData().setAttacking(true);
                controller.getGameData().setUpdated(true);
                Platform.runLater(() -> {
                    controller.getView().showLobbyMessage("[Server]: ", "Can't attack there.");
                });
                break;
            case CONNECT: //client tried to connect with invalid ID
                System.out.println("Invalid ID!");
                Platform.runLater(() -> {
                    controller.getView().refuse();
                    controller.getView().backToStart();
                });
                break;
            default:
                Platform.runLater(() -> {
                    controller.getView().refuse();
                });
        }
    }

    /**
     * Setup connectin socket, input and output threads/classes and set player name.
     * @param serverName
     * @param serverPort
     * @param playerName
     * @return connection ok/bad
     */
    public boolean setupConnection(String serverName, int serverPort, String playerName) {
        System.out.println("Establishing connection. Please wait ...");
        try {
            clientSocket = new Socket(serverName, serverPort);
            System.out.println("Connected: " + clientSocket);
            this.playerName = playerName;
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
    
    /**
     * Start connection and input thread.
     */
    public void startConnection() {
        System.out.println("Starting connection...");
        inputThread.start();
        Object o[] = {this.playerName};
        executeCommand(new Command(controller.getClientID(), MsgType.CONNECT, (short) 1, o), false);
    }
    
    /**
     * Stop and close connection socket and input/output threads
     */
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
    
    /**
     * Execute command as input.
     * @param c 
     */
    public void runInputCommand(Command c) {
        executeCommand(c, true);
    }
    
    /**
     * Execute command as output.
     * @param c 
     */
    public void runOutputCommand(Command c) {
        executeCommand(c, false);
    }
    
    /**
     * Send command as string to server.
     * @param c 
     */
    private void sendMessage(Command c) {
        output.handle(c.toString());
    }
    
    public Socket getSocket() {
        return this.clientSocket;
    }

    private List parseServerData(Command c) {
        List retval = new ArrayList();
        int added = 0;
        String[] tmp = new String[7];
        for (int i = 0; i < c.length; i++) {
            tmp[added++] = (String)c.data[i];
            if(added == 7) {
                retval.add(Lobby.parseLobby(tmp));
                tmp = new String[7];
                added = 0;
            }
        }

        return retval;
    }

    private List parseUnits(Command c) {
        List retval = new ArrayList();

        for (int i = 0; i < c.length; i++) {
            String[] tmp = ((String) c.data[i]).split("\\|");
            retval.add(Unit.parseUnit(tmp));
        }

        return retval;
    }

    private void updateScore(Command c) {
        try {
            controller.updatePlayers(
                    (String) c.data[0],
                    (String) c.data[1],
                    Integer.parseInt((String) c.data[2]),
                    Integer.parseInt((String) c.data[3]),
                    Integer.parseInt((String) c.data[4]),
                    ((String) c.data[5]).charAt(0) == 'T',
                    ((String) c.data[6]).charAt(0));
        } catch (NumberFormatException | ClassCastException nfe) {
            System.err.println("I can't work with this: " + c);
        }
    }
}
