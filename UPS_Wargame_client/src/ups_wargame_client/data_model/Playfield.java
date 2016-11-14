/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.data_model;

import java.util.ArrayList;

/**
 *
 * @author sini
 */
public class Playfield {
    
    private ArrayList<Unit> units;
    private char map[][];
    private int rows;
    private int columns;
    
    public Playfield(int rows, int columns) {
        map = new char[rows][columns];
        this.rows = rows;
        this.columns = columns;
        units = new ArrayList<>();
    }
    
    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public ArrayList<Unit> getUnits() {
        return units;
    }

    public char[][] getMap() {
        return map;
    }
    
    public void setMap(char map[][]) {
        this.map = map;
    }
    
    //
    
    public char getHex(int r, int q) {
        int i = r, j = q + (r+1)/2;
        char retval = 0;

        if((i >= 0 && this.rows <= i) && (j >= 0 && this.columns <= j))
            retval = map[i][j];

        return retval;
    }
    
    public void setHex(int r, int q, char value) {
        int i = r, j = q + (r+1)/2;
        
        if((i >= 0 && this.rows <= i) && (j >= 0 && this.columns <= j))
            map[i][j] = value;
    }
    
    public void print() {
        for(int i = 0; i < this.rows; i++) {
            if(i%2==0) System.out.print("\t");
            for(int j = 0; j < this.columns; j++) {
                System.out.print("[" + this.map[i][j] + " " + i + " " + 
                        (j-(i+1)/2) + "]");
            }
            System.out.println();
        }
    }
}
