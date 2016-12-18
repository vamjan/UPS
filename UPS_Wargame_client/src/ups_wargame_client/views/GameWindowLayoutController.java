/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.views;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ups_wargame_client.control.ClientController;
import ups_wargame_client.control.Command;
import ups_wargame_client.control.IController;
import ups_wargame_client.control.Lobby;
import ups_wargame_client.net_interface.MsgType;

/**
 * FXML Controller class
 *
 * @author sini
 */
public class GameWindowLayoutController implements Initializable, IViewable {

    private IController controller = null;
    private Lobby selectedLobby = null;

    @FXML
    private AnchorPane anchorPane;
    private Stage mainStage;
    @FXML
    private TabPane chatPane;
    @FXML
    private TitledPane lobbyInfo;

    @FXML
    private ListView lobbyList;
    private ObservableList<Lobby> obsList = FXCollections.observableArrayList();

    @FXML
    private Button disconnectButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button addLobbyButton;
    @FXML
    private Button connectLobbyButton;
    @FXML
    private Button leaveLobbyButton;

    @FXML
    private TextArea serverChatTextArea;
    @FXML
    private TextArea lobbyChatTextArea;

    @FXML
    private Label playerOneLabel;
    @FXML
    private Label playerTwoLabel;
    @FXML
    private Label readyOneLabel;
    @FXML
    private Label readyTwoLabel;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        controller = ClientController.getInstance();
        controller.setupView(this);

        lobbyList.setItems(obsList);
        lobbyInfo.setExpanded(false);

        disconnectButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                backToStart();
            }
        });

        refreshButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                controller.addToOutputQueue(new Command(controller.getClientID(), MsgType.GET_SERVER, (short) 0, null));
            }
        });

        addLobbyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                Object o[] = {"LOBBYNAME"};
                controller.addToOutputQueue(new Command(controller.getClientID(), MsgType.CREATE_LOBBY, (short) 1, o));
            }
        });

        connectLobbyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Lobby tmp = null;
                if ((tmp = (Lobby) lobbyList.getSelectionModel().getSelectedItem()) != null) {
                    Object o[] = {tmp.getIndex()};
                    controller.addToOutputQueue(new Command(controller.getClientID(), MsgType.JOIN_LOBBY, (short) 1, o));
                    selectedLobby = tmp;
                }
            }
        });

        leaveLobbyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (selectedLobby != null) {
                    Object o[] = {selectedLobby.getIndex()};
                    controller.addToOutputQueue(new Command(controller.getClientID(), MsgType.LEAVE_LOBBY, (short) 1, o));
                    selectedLobby = null; //TODO: might be a problem
                }
            }
        });
    }

    @Override
    public void showServerMessage(String data, String msg) {
        serverChatTextArea.appendText(data + msg + '\n');
    }

    @Override
    public void showLobbyMessage(String data, String msg) {
        lobbyChatTextArea.appendText(data + msg + '\n');
    }

    @Override
    public void backToStart() {
        ClientController.getInstance().stopConnection();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("MainWindowLayout.fxml"));
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("WARGAME");
            stage.setScene(scene);
            stage.show();
            mainStage.hide();
        } catch (IOException ioe) {
            System.err.println("Error!" + ioe.getMessage());
            System.exit(0);
        }
    }

    @Override
    public void setLobbyList(List list) {
        obsList.clear();
        obsList.addAll(list);
    }

    @Override
    public void showLobby() {
        lobbyInfo.setExpanded(true);
        playerOneLabel.setText(selectedLobby.getPlayerOne());
        playerTwoLabel.setText(selectedLobby.getPlayerTwo());
    }

    @Override
    public void hideLobby() {
        lobbyInfo.setExpanded(false);
    }

    @Override
    public void refuse() {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Refused");
        alert.setHeaderText("Unable to comply");
        alert.setContentText("Action was refused by the server.");

        alert.showAndWait();
    }

    @Override
    public void acknowledge() {
        System.out.println("Action acknowledged.");
    }

    public void setPrimaryStage(Stage stage) {
        this.mainStage = stage;
    }
}
