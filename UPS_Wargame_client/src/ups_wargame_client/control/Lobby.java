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
    private boolean inProgress;
    private int index;
    
    private GameData data;
    
    public Lobby(int index, String name) {
        this.name = name;
        this.index = index;
        this.playerOne = null;
        this.playerTwo = null;
        
        this.inProgress = false;
        this.data = null;
    }
    
    public int getIndex() {
        return this.index;
    }
    
    public String getPlayerOne() {
        return this.playerOne;
    }
    
    public String getPlayerTwo() {
        return this.playerTwo;
    }
    
    @Override
    public String toString() {
        int count = 0;
        if(playerOne != null) count++;
        if(playerTwo != null) count++;
        return String.format("%s %d/2", this.name, count);
    }
}
