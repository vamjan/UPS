/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.control;

import ups_wargame_client.data_model.GameData;

/**
 *
 * @author sini
 */
public class Lobby {

    private String name;
    private String playerOne;
    private String playerTwo;
    private boolean readyOne;
    private boolean readyTwo;
    private boolean inProgress;
    private int index;

    private GameData data;

    public Lobby(int index, String name) {
        this.name = name;
        this.index = index;
        this.playerOne = null;
        this.playerTwo = null;
        this.readyOne = false;
        this.readyTwo = false;

        this.inProgress = false;
        this.data = null;
    }

    public int getIndex() {
        return this.index;
    }

    public String getPlayerOne() {
        return this.playerOne;
    }

    public void setPlayerOne(String s) {
        this.playerOne = s;
    }

    public String getPlayerTwo() {
        return this.playerTwo;
    }

    public void setPlayerTwo(String s) {
        this.playerTwo = s;
    }

    public boolean getReadyOne() {
        return this.readyOne;
    }

    public void setReadyOne(boolean val) {
        this.readyOne = val;
    }

    public boolean getReadyTwo() {
        return this.readyTwo;
    }

    public void setReadyTwo(boolean val) {
        this.readyTwo = val;
    }

    @Override
    public String toString() {
        int count = 0;
        if (!playerOne.equals("FREE")) {
            count++;
        }
        if (!playerTwo.equals("FREE")) {
            count++;
        }
        return String.format("%s %d/2", this.name, count);
    }

    public static Lobby parseLobby(String[] args) {
        Lobby retval = null;
        //INDEX|NAME|GAMEINPROGRESS|PLONE|RDY/NOT|PLTWO|RDY/NOT
        try {
            retval = new Lobby(Integer.parseInt(args[0]), args[1]);
            retval.setPlayerOne(args[3]);
            retval.setReadyOne(args[4].equals("RDY"));
            retval.setPlayerTwo(args[5]);
            retval.setReadyTwo(args[6].equals("RDY"));
        } catch (NumberFormatException nfe) {
            System.err.println("Can't create lobby from arguments: " + args);
        }

        return retval;
    }
}
