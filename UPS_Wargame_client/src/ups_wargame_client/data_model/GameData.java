/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.data_model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * @author sini
 */
public class GameData {

    private Playfield playfield = null;
    private List<Unit> units = null;

    public GameData() {
        this.playfield = new Playfield(10, 15);
        
        for(int i = 0; i < playfield.getRows(); i++)
            Arrays.fill(playfield.getMap()[i], 'G');
        
        Random rnd = new Random();
        for(int i = 0; i < 10; i++) {
            int x = rnd.nextInt(10), y = rnd.nextInt(15);
            playfield.getMap()[x][y] = 'D';
        }
        
        this.units = new ArrayList();
        
        Unit tmp = new Unit(0, 0, 'S');
        tmp.setAllegiance('B');
        this.units.add(tmp);
        tmp = new Unit(1, 0, 'S');
        tmp.setAllegiance('R');
        this.units.add(tmp);
        tmp = new Unit(2, 0, 'I');
        tmp.setAllegiance('R');
        this.units.add(tmp);
        tmp = new Unit(3, 0, 'T');
        tmp.setAllegiance('B');
        this.units.add(tmp);
        tmp = new Unit(4, 0, 'T');
        tmp.setAllegiance('B');
        this.units.add(tmp);
        tmp = new Unit(5, 0, 'T');
        tmp.setAllegiance('R');
        this.units.add(tmp);
        tmp = new Unit(6, 0, 'T');
        tmp.setAllegiance('B');
        this.units.add(tmp);
        tmp = new Unit(7, 0, 'S');
        tmp.setAllegiance('R');
        this.units.add(tmp);
        tmp = new Unit(8, 0, 'S');
        tmp.setAllegiance('B');
        this.units.add(tmp);
        tmp = new Unit(9, 0, 'S');
        tmp.setAllegiance('R');
        this.units.add(tmp);
    }

    public Playfield getPlayField() {
        return playfield;
    }
    
    public List<Unit> getUnits() {
        return units;
    }
}
