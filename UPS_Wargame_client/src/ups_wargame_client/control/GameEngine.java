/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.control;

import ups_wargame_client.net_interface.MsgType;

/**
 *
 * @author sini
 */
public class GameEngine implements Runnable {

    final long TIMEOUT = 1500;

    private ClientController controller = null;
    private boolean running = true;
    private int out = 0;

    public GameEngine() {
        controller = ClientController.getInstance();
    }

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
                    System.err.println("[ENGINE]: Timed out!");
                    if (out >= 5) {
                        controller.sendCommand(new Command(controller.getClientID(), MsgType.DISCONNECT, (short) 0, null));
                        this.stopRunning(); //TODO: fuj
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

            update();
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
            System.err.println("[ENGINE]: Engine blocking exception!");
        }
    }

    public void stopRunning() {
        this.running = false;
    }
}
