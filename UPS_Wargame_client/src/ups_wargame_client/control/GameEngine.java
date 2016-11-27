/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.control;

import ups_wargame_client.net_interface.Command;

/**
 *
 * @author sini
 */
public class GameEngine implements Runnable {

    final long TIMEOUT = 100000;
    final int TICKS_PER_SECOND = 25;
    final int SKIP_TICKS = 1000 / TICKS_PER_SECOND;
    final int MAX_FRAMESKIP = 10;

    private ClientController controller = null;
    private boolean running = true;

    public GameEngine() {
        controller = ClientController.getInstance();
    }

    /*@Override
    public void run() {
        long nextGameTick = System.currentTimeMillis(), sleepTime = 0, currentTime = nextGameTick;
        int loops, hlp = 0;
        boolean gameIsRunning = true;
        //game loop

        while (gameIsRunning) {

            loops = 0;
            //currentTime = System.currentTimeMillis();

            while (currentTime < nextGameTick) {
                update();

                nextUpdateTick += SKIP_TICKS / 2;
                currentTime = System.currentTimeMillis();
                sleepTime = nextUpdateTick - currentTime - 1;

                try {
                    if(sleepTime > 0)
                        Thread.sleep(sleepTime);
                } catch (InterruptedException ie) {
                }

                loops++;

                System.out.println("Engine: " + System.currentTimeMillis() + " " + nextUpdateTick + " " + loops);
                
                currentTime = System.currentTimeMillis();
            //}

            nextGameTick += SKIP_TICKS;
            update();
            render();

            System.out.println("Engine: " + System.currentTimeMillis() + " " + nextGameTick + " " + loops);

            sleepTime = nextGameTick - System.currentTimeMillis();

            try {
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                }
            } catch (InterruptedException ie) {
            }

            hlp++;
            if (hlp > 300) {
                gameIsRunning = false;
                System.out.println("Engine: stop");
            }
        }
    }*/
    @Override
    public void run() {
        String c = null;

        while (running) {
            c = controller.retrieveInput();
            while (c == null) {
                long currentTime = System.currentTimeMillis();
                this.block();
                if (System.currentTimeMillis() >= (currentTime + TIMEOUT)) {
                    System.err.println("Timed out!");
                }
                c = controller.retrieveInput();
            }

            System.out.println("Working on msg: " + c);
            controller.sendCommand((new Command(154665655)).toString());
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
