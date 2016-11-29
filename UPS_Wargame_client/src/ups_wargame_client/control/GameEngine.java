/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.control;

/**
 *
 * @author sini
 */
public class GameEngine implements Runnable {

    final long TIMEOUT = 2000;

    private ClientController controller = null;
    private boolean running = true;

    public GameEngine() {
        controller = ClientController.getInstance();
    }

    @Override
    public void run() {
        Command input = null;
        Command output = null;

        while (running) {
            input = controller.retrieveInput();
            output = controller.retrieveOutput();
            long currentTime = System.currentTimeMillis();
            while (input == null && output == null) {
                this.block();
                if (System.currentTimeMillis() >= (currentTime + TIMEOUT)) {
                    System.err.println("[ENGINE]: Timed out!");
                    if(running == false) break;
                }
                input = controller.retrieveInput();
                output = controller.retrieveOutput();
            }
            
            if(output != null) {
                System.out.println("[ENGINE]: Working on input: " + output);
                controller.sendCommand(output);
            }
            if(input != null){
                System.out.println("[ENGINE]: Working on input: " + input);
                controller.recieveCommand(input);
                currentTime = System.currentTimeMillis();
            }
            
            update();
            render();
        }
    }

    private void update() {
        /*try {
            Thread.sleep(1);
        } catch (InterruptedException ie) {
        }*/
    }

    private void render() {
        /*try {
            Thread.sleep(10);
        } catch (InterruptedException ie) {
        }*/
    }

    private void block() {
        try {
            synchronized (this) {
                this.wait(TIMEOUT);
            }
        } catch (InterruptedException ie) {
            System.err.println("Engine exception!");
        }
    }
    
    public void stopRunning() {
        this.running = false;
    }
}
