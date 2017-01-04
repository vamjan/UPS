package ups_wargame_client;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ups_wargame_client.control.ClientController;

/**
 * Main launch class. JavaFX Application class.
 * @author Jan Vampol
 */
public class UPS_Wargame_client extends Application {
    
    /**
     * Start operation.
     * @param stage 
     */
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
     * Set default stop operation to close connection.
     * @throws Exception 
     */
    @Override
    public void stop() throws Exception {
        ClientController.getInstance().stopConnection();
        super.stop();
    }

    /**
     * Launch method.
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        ClientController.getInstance();
        launch(args);
    }
}
