/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ups_wargame_client.control.ClientController;

/**
 *
 * @author sini
 */
public class UPS_Wargame_client extends Application {

    @Override
    public void start(Stage stage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("views/MainWindowLayout.fxml"));
            Scene scene = new Scene(root);

            stage.setTitle("WARGAME");
            stage.setScene(scene);
            stage.show();
        } catch (IOException ioe) {
            System.err.println("Error!" + ioe.getMessage());
            System.exit(0);
        }
    }
    
    @Override
    public void stop() throws Exception {
        ClientController.getInstance().stopConnection();
        super.stop();
    }

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        ClientController.getInstance();
        launch(args);
    }
}
