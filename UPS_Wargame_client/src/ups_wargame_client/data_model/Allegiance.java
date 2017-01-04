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
public enum Allegiance {
    BLU('B'),
    RED('R'),
    NEUTRAL('N');

    private char name;

    public char getName() {
        return this.name;
    }

    private Allegiance(char val) {
        name = val;
    }

    public static Allegiance getAllegianceByName(char name) {
        for (Allegiance type : Allegiance.values()) {
            if (type.name == name) {
                return type;
            }
        }
        return null;
    }
}
