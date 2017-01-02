/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.data_model;

import java.util.List;

/**
 *
 * @author sini
 */
public interface IGameData {
    public Unit getUnitByID(int ID);
    public Unit getUnitOnCoords(int r, int q);
    public boolean isUpdated();
    public void setUpdated(boolean val);
    public GameData getUpdates();
    public List<Unit> getUnits();
    public void setUnitOnTurn(Unit val);
    public Unit getUnitOnTurn();
    public boolean playerOnTurn();
    public Playfield getPlayField();
    public String getPlayerBlue();
    public String getPlayerRed();
    public int getBlueScore();
    public int getRedScore();
    public boolean play(int r, int q);
    public void setAttacking(boolean val);
    public boolean getAttacking();
}
