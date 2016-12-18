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
import java.util.List;
import javafx.application.Platform;
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
                        ackCommand(command);
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
                    System.out.println("I create lobby " + command.data[0]);
                    break;
                case JOIN_LOBBY:
                    //lobby update
                    break;
                case LEAVE_LOBBY:
                    //kick
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
                });
                break;
            case LEAVE_LOBBY:
                Platform.runLater(() -> {
                    controller.getView().hideLobby();
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
        Object o[] = { this.playerName };
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
        int i;

        for (i = 0; i < c.length; i++) {
            String[] tmp = ((String) c.data[i]).split("\\|");
            retval.add(new Lobby(Integer.parseInt(tmp[0]), (String) tmp[1])); //TODO: tady to muze spadnout
        }

        return retval;
    }
}
