/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import ups_wargame_client.net_interface.ClientController;

/**
 *
 * @author sini
 */
public class MainWindowLayoutController implements Initializable {

    @FXML //  fx:id="myButton"
    private Button goButton; // Value injected by FXMLLoader
    @FXML
    private Button sendButton;
    @FXML
    private Button disconnectButton;

    @FXML
    private TextField playerTF;
    @FXML
    private TextField serverTF;
    @FXML
    private TextField portTF;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        goButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                String serverName = serverTF.getText();
                String port = portTF.getText();
                if (!ClientController.getInstance().startConnection(serverName, Integer.parseInt(port))) {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Connection not possible");
                    alert.setHeaderText("I was unable to connect");
                    alert.setContentText("I am sorry... don't close me pls :(");

                    alert.showAndWait();
                }
            }
        });

        sendButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                ClientController.getInstance().sendCommand(playerTF.getText());
            }
        });

        disconnectButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                ClientController.getInstance().stopConnection();
            }
        });
    }
}
