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
 * Connect window which starts the application.
 * Lets user to enter IP, port, name and optionaly ID. 
 * @author Jan Vampol
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
    @FXML
    private TextField idTF;

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
                    
                    if(!idTF.getText().equals("")) {
                        int ID = (int)Long.parseLong(idTF.getText(), 16);
                        ClientController.getInstance().setupID(ID);
                    }

                    if (!ClientController.getInstance().setupConnection(serverName, portNum, name)) { //try to connect
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

                            GameWindowLayoutController controller = (GameWindowLayoutController) loader.getController(); //initialize game window controller
                            controller.setPrimaryStage(stage);

                            stage.show();

                            GameEngine e = new GameEngine(); //create engine
                            ClientController.getInstance().setupEngine(e); //and add its reference to controller
                            ClientController.getInstance().startConnection(); //start the connection to server
                            // Hide this current window
                            ((Node) (event.getSource())).getScene().getWindow().hide();
                        } catch (IOException ioe) {
                            System.err.println("JavaFX Error! " + ioe);
                            System.exit(0);
                        }
                    }
                } catch (NumberFormatException nfe) {
                    System.err.println("Wrong port or ID input!");
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
