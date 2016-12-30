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
public class GameData implements IGameData {

    private Playfield playfield = null;
    private List<Unit> units = null;

    private Unit onTurn = null;
    private String playerBlue = null;
    private String playerRed = null;
    private int blueScore;
    private int redScore;

    private boolean waiting;
    private boolean attacking;
    private char userAllegiance;

    public GameData() {
        this.playfield = new Playfield(10, 15);

        for (int i = 0; i < playfield.getRows(); i++) {
            Arrays.fill(playfield.getMap()[i], 'G');
        }

        Random rnd = new Random();
        for (int i = 0; i < 10; i++) {
            int x = rnd.nextInt(10), y = rnd.nextInt(15);
            playfield.getMap()[x][y] = 'D';
        }

        this.units = new ArrayList();

        /*Unit tmp = new Unit(0, 0, 'S');
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
        this.units.add(tmp);*/

    }

    public GameData(int rows, int cols, char[][] map) {
        this.playfield = new Playfield(rows, cols);
        this.playfield.setMap(map);
    }
    
    public void updateScore(String playerBlu, String playerRed, int scoreBlu, int scoreRed, int onTurnID, char player) {
        this.playerBlue = playerBlu;
        this.playerRed = playerRed;
        this.blueScore = scoreBlu;
        this.redScore = scoreRed;
        this.userAllegiance = player;
        this.onTurn = getUnitByID(onTurnID);
    }

    public Unit getUnitOnTurn() {
        return this.onTurn;
    }
    
    public Unit incrementTurn() {
        /*Unit retval = null;
        onTurnIndex++;
        while ((retval = units.get(onTurnIndex % units.size())).getType() == 'F') {
            onTurnIndex++;
        }
        return retval;*/
        return null;
    }

    public boolean playerOnTurn(char al) {
        return this.onTurn.getAllegiance() == al;
    }

    public boolean checkOccupied(int r, int q) {
        for (Unit val : units) {
            if (val.getCoordX() == r && val.getCoordZ() == q) {
                if (val.isMovable()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public Unit getUnitOnCoords(int r, int q) {
        for (Unit val : units) {
            if (val.getCoordX() == r && val.getCoordZ() == q) {
                return val;
            }
        }
        return null;
    }
    
    @Override
    public Unit getUnitByID(int ID) {
        for (Unit val : units) {
            if (val.getID() == ID) {
                return val;
            }
        }
        return null;
    }

    public boolean play(int r, int q) {
        Unit unit = this.getUnitOnTurn();
        unit.setMoveRange(10); //temporary
        unit.setAttackRange(10);
        
        return true;
    }

    public void skip() {
        
    }

    public Playfield getPlayField() {
        return playfield;
    }

    public List<Unit> getUnits() {
        return units;
    }
    
    public void setUnits(List<Unit> units) {
        this.units = units;
    }

    /**
     * @return the playerBlue
     */
    public String getPlayerBlue() {
        return playerBlue;
    }

    /**
     * @return the playerRed
     */
    public String getPlayerRed() {
        return playerRed;
    }

    /**
     * @return the blueScore
     */
    public int getBlueScore() {
        return blueScore;
    }

    /**
     * @return the redScore
     */
    public int getRedScore() {
        return redScore;
    }

    /**
     * @return the playerAllegiance
     */
    public char getUserAllegiance() {
        return userAllegiance;
    }
}
