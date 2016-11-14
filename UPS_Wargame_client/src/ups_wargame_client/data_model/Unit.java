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
public class Unit {
    private short ID;
    //unittype type;
    //allegiance al;
    private short health;
    private short damage;
    private short move_range;
    private short attack_range;
    private short view;
    private boolean dead;
    private int coord_x;
    private int coord_z;
    
    public Unit(int coord_x, int coord_z) {
        this.coord_x = coord_x;
        this.coord_z = coord_z;
        this.dead = false;
    }
    
    public void print() {
        System.out.println("Unit:" + this.hashCode() + " - " + coord_x + "|" + coord_z);
    }
}
