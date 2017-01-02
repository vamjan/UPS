/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.views;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ups_wargame_client.control.ClientController;
import ups_wargame_client.control.GameEngine;

/**
 *
 * @author sini
 */
public class MainWindowLayoutController implements Initializable {

    @FXML //  fx:id="myButton"
    private Button goButton; // Value injected by FXMLLoader

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
                String serverName = serverTF.getText(); //TODO: input control
                String port = portTF.getText();
                String name = playerTF.getText();
                int portNum = -1;

                try {
                    portNum = Integer.parseInt(port);

                    if (!ClientController.getInstance().setupConnection(serverName, portNum, name.replace("|", ""))) {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Connection not possible");
                        alert.setHeaderText("I was unable to connect");
                        alert.setContentText("I am sorry... don't close me pls :(");

                        alert.showAndWait();
                    } else {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("GameWindowLayout.fxml"));
                            Parent root = loader.load();
                            Stage stage = new Stage();
                            stage.setTitle("Wargame");
                            stage.setScene(new Scene(root));

                            GameWindowLayoutController controller = (GameWindowLayoutController) loader.getController();
                            controller.setPrimaryStage(stage);

                            stage.show();

                            GameEngine e = new GameEngine();
                            ClientController.getInstance().setupEngine(e);
                            ClientController.getInstance().startConnection();
                            // Hide this current window
                            ((Node) (event.getSource())).getScene().getWindow().hide();
                        } catch (IOException ioe) {
                            System.err.println("JavaFX Error! " + ioe);
                            System.exit(0);
                        }
                    }
                } catch (NumberFormatException nfe) {
                    System.err.println("Wrong port input!");
                } catch (IllegalArgumentException iae) {
                    System.err.println("Port number is out or range!");
                }
            }
        });
        
        playerTF.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(newValue.length() > 30) {
                    playerTF.setText(oldValue);
                }
            }
        });
    }
}
