package ups_wargame_client.control;

import javafx.application.Platform;
import ups_wargame_client.data_model.IGameData;
import ups_wargame_client.net_interface.MsgType;

/**
 * This class produces ticks. Each tick engine will try to execute all commands on
 * command queues and redraw the gamedata to view (if data are updated). Engine is
 * blocked when there are no commands to be executed and will get unblocked every
 * TIMEOUT or when new command is added to input/output queue. If there is now input
 * from server for 3xTIMEOUT, the engine will close the application back to connect window.
 * @author Jan Vampol
 */
public class GameEngine implements Runnable {

    private final long TIMEOUT = 1666;
    //controller reference
    private ClientController controller = null;
    //running flag
    private boolean running = true;
    //number of timeouts withou input
    private int out = 0;
    
    /**
     * Contructor links with controller reference
     */
    public GameEngine() {
        controller = ClientController.getInstance();
    }
    
    /**
     * Run method to implement Runnable. Executes the game and command execution loop.
     * Also checks fro timeouts.
     */
    @Override
    public void run() {
        Command input = null;
        Command output = null;
        long currentTime = System.currentTimeMillis();

        while (running) {
            input = controller.retrieveInput();
            output = controller.retrieveOutput();
            while (input == null && output == null) {
                if (System.currentTimeMillis() >= (currentTime + TIMEOUT)) {
                    if (out > 2) {
                        controller.sendCommand(new Command(controller.getClientID(), MsgType.DISCONNECT, (short) 0, null));
                        this.stopRunning(); //HACK: This is not a great solution...but it works ¯\_(ツ)_/¯
                        System.err.println("[ENGINE]: Timed out!");
                    }
                    if (running == false) {
                        break;
                    }
                    controller.sendCommand(new Command(controller.getClientID(), MsgType.POKE, (short) 0, null));
                    out++;
                }
                this.block();
                input = controller.retrieveInput();
                output = controller.retrieveOutput();
            }

            if (output != null) {
                System.out.print("[ENGINE]: Working on output: " + output);
                controller.sendCommand(output);
            }
            if (input != null) {
                System.out.print("[ENGINE]: Working on input: " + input);
                controller.recieveCommand(input);
                currentTime = System.currentTimeMillis();
                out = 0;
            }

            //update();
            render();
        }

        System.out.println("Result on exit: " + controller.getAckString());
    }
    
    private void update() {
        /*try {
            Thread.sleep(1);
        } catch (InterruptedException ie) {
        }*/
    }
    
    /**
     * If there are any updates in game data, this method will redraw the view.
     */
    private void render() {
        IGameData data = controller.getGameData();
        if (data != null) {
            Platform.runLater(() -> {
                if (data.isUpdated()) {
                    data.getUpdates();
                    controller.getView().redraw(data);
                }
            });
        }
    }
    
    /**
     * Block engine to wait for TIMEOUT or new command.
     */
    private void block() {
        try {
            synchronized (this) {
                this.wait(TIMEOUT);
            }
        } catch (InterruptedException ie) {
            System.err.println("[ENGINE]: Engine blocking exception!");
        }
    }
    
    /**
     * Set engine flag to stop running.
     */
    public void stopRunning() {
        this.running = false;
    }
}
