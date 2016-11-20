/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client;

import java.io.IOException;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ups_wargame_client.net_interface.ClientController;

/**
 *
 * @author sini
 */
public class UPS_Wargame_client extends Application{
    
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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ClientController.getInstance();
        launch(args);
    }

    /*public static void main(String args[]) {
        //ClientOutputThread client = new ClientOutputThread();
        Command c = new Command(-6565121, MsgType.ATTACK, (short)5, 1, 2, 3, 4);
        System.out.println(c);
        
        c = Parser.parseInput(c.toString());
        System.out.println(c);
    }  */
}
