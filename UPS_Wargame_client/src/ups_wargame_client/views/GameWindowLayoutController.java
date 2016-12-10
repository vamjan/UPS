/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.views;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import ups_wargame_client.control.ClientController;
import ups_wargame_client.control.Command;
import ups_wargame_client.control.IController;
import ups_wargame_client.net_interface.MsgType;

/**
 * FXML Controller class
 *
 * @author sini
 */
public class GameWindowLayoutController implements Initializable, IViewable {

    private IController controller = null;

    @FXML
    private TabPane chatPane;

    @FXML
    private Button disconnectButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button addLobbyButton;

    @FXML
    private TextArea serverChatTextArea;
    @FXML
    private TextArea lobbyChatTextArea;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        controller = ClientController.getInstance();
        controller.setupView(this);

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
    }

    @Override
    public void showServerMessage(String data, String msg) {
        System.out.println(data + msg + '\n');
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
            //((Node) (event.getSource())).getScene().getWindow().hide();
            ((Stage)chatPane.getScene().getWindow()).hide();
        } catch (IOException ioe) {
            System.err.println("Error!" + ioe.getMessage());
            System.exit(0);
        }
    }
}
