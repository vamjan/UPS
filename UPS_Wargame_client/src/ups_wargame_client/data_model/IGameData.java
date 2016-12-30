/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.data_model;

/**
 *
 * @author sini
 */
public interface IGameData {
    public Unit getUnitByID(int ID);
    public Unit getUnitOnCoords(int r, int q);
}
