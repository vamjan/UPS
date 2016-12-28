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
    private char type;  //S - SPG, T - TANK, I - Infantry, F - Flag ////mozna predelat na enum
    private char al;    //N - neutral, B - player1, R - player2
    private short health;
    private short damage;
    private short moveRange;
    private short attackRange;
    private boolean dead;
    private int coordX;
    private int coordZ;
    
    public Unit(int coord_x, int coord_z, char type) {
        this.coordX = coord_x;
        this.coordZ = coord_z;
        this.dead = false;
        this.type = type;
    }
    
    public void setAllegiance(char al) {
        if(al == 'N' || al == 'B' || al == 'R') {
            this.al = al;
        } else {
            this.al = 'N';
        }
    }
    
    public char getAllegiance() {
        return this.al;
    }
    
    public void setType(char type) {
        if(type == 'S' || type == 'T' || type == 'I' || type == 'F') {
            this.type = type;
        } else {
            this.type = 'I';
        }
    }
    
    public char getType() {
        return this.type;
    }
    
    public void setCoordX(int x) {
        this.coordX = x;
    }
    
    public int getCoordX() {
        return this.coordX;
    }
    
    public void setCoordZ(int z) {
        this.coordZ = z;
    }
    
    public int getCoordZ() {
        return this.coordZ;
    }

    /**
     * @return the ID
     */
    public short getID() {
        return ID;
    }

    /**
     * @param ID the ID to set
     */
    public void setID(short ID) {
        this.ID = ID;
    }

    /**
     * @return the health
     */
    public short getHealth() {
        return health;
    }

    /**
     * @param health the health to set
     */
    public void setHealth(short health) {
        this.health = health;
    }

    /**
     * @return the damage
     */
    public short getDamage() {
        return damage;
    }

    /**
     * @param damage the damage to set
     */
    public void setDamage(short damage) {
        this.damage = damage;
    }

    /**
     * @return the moveRange
     */
    public short getMoveRange() {
        return moveRange;
    }

    /**
     * @param moveRange the moveRange to set
     */
    public void setMoveRange(short moveRange) {
        this.moveRange = moveRange;
    }

    /**
     * @return the attackRange
     */
    public short getAttackRange() {
        return attackRange;
    }

    /**
     * @param attackRange the attackRange to set
     */
    public void setAttackRange(short attackRange) {
        this.attackRange = attackRange;
    }

    /**
     * @return the dead
     */
    public boolean isDead() {
        return dead;
    }

    /**
     * @param dead the dead to set
     */
    public void setDead(boolean dead) {
        this.dead = dead;
    }
    
    public boolean checkMove(int destX, int destZ) {
        return this.checkRange(destX, destZ, this.getMoveRange());
    }
    
    public boolean checkAttack(Unit target) {
        return this.checkRange(target.getCoordX(), target.getCoordZ(), this.getAttackRange());
    }

    private boolean checkRange(int destX, int destZ, int range) {
        int startY = -this.getCoordX() - this.getCoordZ();
        int destY = -destX - destZ;

        if (destX >= (this.getCoordX() - range) && destX <= (this.getCoordX() + range)) {
            if (destY >= (startY - range) && destY <= (startY + range)) {
                if (destZ >= (this.getCoordZ() - range) && destZ <= (this.getCoordZ() + range)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean isMovable() {
        return this.type != 'F';
    }
    
    public boolean isCapturable(char al) {
        return (this.al != al) && (this.type == 'F');
    }
    
    @Override
    public String toString() {
        return String.format("Unit: %d - %d|%d", this.hashCode(), this.getCoordX(), this.getCoordZ());
    }
}
