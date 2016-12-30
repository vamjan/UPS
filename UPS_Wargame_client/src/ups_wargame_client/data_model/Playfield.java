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

    private char map[][];
    private int rows;
    private int columns;

    public Playfield(int rows, int columns) {
        map = new char[rows][columns];
        this.rows = rows;
        this.columns = columns;
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

    public char[][] getMap() {
        return map;
    }

    public void setMap(char map[][]) {
        this.map = map;
    }

    //
    public char getHex(int r, int q) {
        int i = r + (q + 1) / 2, j = q;
        char retval = 0;

        if (this.contains(i, j)) {
            retval = map[i][j];
        }

        return retval;
    }

    public void setHex(int r, int q, char value) {
        int i = r + (q + 1) / 2, j = q;

        if (this.contains(i, j)) {
            map[i][j] = value;
        }
    }

    public boolean contains(int i, int j) {
        if ((i >= 0 && this.rows > i) && (j >= 0 && this.columns > j)) {
            return true;
        } else {
            return false;
        }
    }
    
    public static int getRow(int i, int j) {
        return i - (j + 1) / 2;
    }

    public static int convertRow(int r, int q) {
        return r + (q + 1) / 2;
    }

    public void print() { //might be broken
        for (int i = 0; i < this.rows; i++) {
            if (i % 2 == 0) {
                System.out.print("\t");
            }
            for (int j = 0; j < this.columns; j++) {
                System.out.print("[" + this.map[i][j] + " " + (i - (j + 1) / 2) + " "
                        + j + "]");
            }
            System.out.println();
        }
    }
}
