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

    private int ID;
    private char type;  //S - SPG, T - TANK, I - Infantry, F - Flag ////mozna predelat na enum
    private char al;    //N - neutral, B - player1, R - player2
    private int health;
    private int damage;
    private int moveRange;
    private int attackRange;
    private boolean dead;
    private int coordX;
    private int coordZ;

    public Unit(int ID, int coord_x, int coord_z, char type) {
        this.ID = ID;
        this.coordX = coord_x;
        this.coordZ = coord_z;
        this.dead = false;
        this.type = type;
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

    public boolean isAttackable(char al) {
        return (this.al != al) && (this.type != 'F');
    }
    
    public void move(int r, int q) {
        this.setCoordX(q);
        this.setCoordZ(r);
    }
    
    public void attack(int newHealth) {
        this.setHealth(newHealth);
        if(newHealth <= 0) {
            this.setDead(true);
        }
    }
    
    public void capture(Unit target) {
        target.al = this.al;
    }

    public void setAllegiance(char al) {
        if (al == 'N' || al == 'B' || al == 'R') {
            this.al = al;
        } else {
            this.al = 'N';
        }
    }

    public char getAllegiance() {
        return this.al;
    }

    public void setType(char type) {
        if (type == 'S' || type == 'T' || type == 'I' || type == 'F') {
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
    public int getID() {
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
    public int getHealth() {
        return health;
    }

    /**
     * @param health the health to set
     */
    public void setHealth(int health) {
        this.health = health;
    }

    /**
     * @return the damage
     */
    public int getDamage() {
        return damage;
    }

    /**
     * @param damage the damage to set
     */
    public void setDamage(int damage) {
        this.damage = damage;
    }

    /**
     * @return the moveRange
     */
    public int getMoveRange() {
        return moveRange;
    }

    /**
     * @param moveRange the moveRange to set
     */
    public void setMoveRange(int moveRange) {
        this.moveRange = moveRange;
    }

    /**
     * @return the attackRange
     */
    public int getAttackRange() {
        return attackRange;
    }

    /**
     * @param attackRange the attackRange to set
     */
    public void setAttackRange(int attackRange) {
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

    @Override
    public String toString() {
        String type = (this.getType() == 'I') ? "INFANTRY" : (this.getType() == 'T') ? "TANK" : "SPG";
        return String.format("Unit %c - %s: - %d", this.getAllegiance(), type, this.getHealth());
    }
    
    public static Unit parseUnit(String args[]) {
        Unit retval = null;
        //ID|TYPE|ALLE|HP|DMG|MOVRANGE|ATKRANGE|DEAD|X|Z
        try{
            retval = new Unit(Integer.parseInt(args[0]), Integer.parseInt(args[9]),
                    Integer.parseInt(args[8]), args[1].charAt(0));
            
            retval.setAllegiance(args[2].charAt(0));
            retval.setHealth(Integer.parseInt(args[3]));
            retval.setDamage(Integer.parseInt(args[4]));
            retval.setMoveRange(Integer.parseInt(args[3]));
            retval.setAttackRange(Integer.parseInt(args[4]));
            retval.setDead(args[7].equals("T"));
        } catch(NumberFormatException nfe) {
            System.err.println("Can't create unit from arguments: " + args);
        }
        
        return retval;
    }
}
