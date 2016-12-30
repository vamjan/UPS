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

    public CommandRunner(ClientController controller) {
        this.controller = controller;
    }

    private boolean executeCommand(Command command, boolean lever) {
        Command tmp;

        if (lever) { //incoming
            switch (command.type) {
                case CONNECT:
                    break;
                case MESSAGE:
                    controller.getView().showServerMessage("[Server]: ", command.toString()); //onlz message in the future
                    break;
                case ACK:
                    tmp = controller.retrieveAck();
                    if (tmp == null) {
                        System.err.println("No ACK required");
                    } else {
                        ackCommand(tmp);
                    }
                    break;
                //case POKE: nuffin
                //    break;
                case NACK:
                    tmp = controller.retrieveAck();
                    if (tmp == null) {
                        System.err.println("No ACK required");
                    } else {
                        nackCommand();
                    }
                    break;
                case GET_SERVER:
                    Platform.runLater(() -> {
                        controller.getView().setLobbyList(parseServerData(command));
                    });
                    break;
                case CREATE_LOBBY:
                    break;
                case JOIN_LOBBY:
                    Platform.runLater(() -> {
                        controller.getView().updateLobby((Lobby) parseServerData(command).get(0));
                    });
                    break;
                case LEAVE_LOBBY:
                    //kick
                    break;
                case START:
                    String[] stringArray = Arrays.copyOf(command.data, command.data.length, String[].class);
                    controller.getView().setupGameData(command.length, stringArray);
                    break;
                case UNITS:
                    controller.getView().setUnits(parseUnits(command));
                    break;
                case UPDATE:
                    this.updateScore(command);
                    break;
                case SKIP:
                    break;
                case END:
                    controller.getView().endGame();
                    break;
                default:
                    return false;
            }
        } else { //outgoing
            if (command.type == MsgType.DISCONNECT) {
                Platform.runLater(() -> {
                    controller.getView().backToStart();
                });
            } else {
                sendMessage(command);
                if (Command.requiresAck(command)) {
                    controller.addToAckQueue(command);
                }
            }
        }
        return true;
    }

    private boolean ackCommand(Command command) {
        switch (command.type) {
            case CREATE_LOBBY:
                controller.getView().acknowledge();
                break;
            case JOIN_LOBBY:
                Platform.runLater(() -> {
                    controller.getView().showLobby();
                    controller.getView().toggleConnected();
                });
                break;
            case LEAVE_LOBBY:
                Platform.runLater(() -> {
                    controller.getView().hideLobby();
                    controller.getView().toggleConnected();
                });
                break;
            default:
                return false;
        }
        return true;
    }

    private void nackCommand() {
        Platform.runLater(() -> {
            controller.getView().refuse();
        });
    }

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

    public void startConnection() {
        System.out.println("Starting connection...");
        inputThread.start();
        Object o[] = {this.playerName};
        executeCommand(new Command(controller.getClientID(), MsgType.CONNECT, (short) 1, o), false);
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

    private List parseServerData(Command c) {
        List retval = new ArrayList();

        for (int i = 0; i < c.length; i++) {
            String[] tmp = ((String) c.data[i]).split("\\|");
            retval.add(Lobby.parseLobby(tmp));
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
        try{
            controller.getView().updatePlayers(
                    (String)c.data[0],
                    (String)c.data[1],
                    Integer.parseInt((String)c.data[2]),
                    Integer.parseInt((String)c.data[3]),
                    Integer.parseInt((String)c.data[4]),
                    ((String)c.data[5]).charAt(0));
        } catch(NumberFormatException | ClassCastException nfe) {
            System.err.println("I can't work with this: " + c);
        }
    }
}
