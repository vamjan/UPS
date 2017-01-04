package ups_wargame_client.data_model;

import java.util.List;
import ups_wargame_client.control.ClientController;
import ups_wargame_client.control.Command;
import ups_wargame_client.control.IController;
import ups_wargame_client.net_interface.MsgType;

/**
 * Class to store and handle game data. Contains methods for game logic.
 * @author Jan Vampol
 */
public class GameData implements IGameData {

    private Playfield playfield = null;
    private List<Unit> units = null;
    //unit currently on turn, also determines player on turn
    private Unit onTurn = null;
    private String playerBlue = null;
    private String playerRed = null;
    private int blueScore;
    private int redScore;
    //flag telling engine if view should be updated
    private boolean updated = false;
    //flag determines if player is moving or attacking
    private boolean attacking;
    //this client allegiance on game filed - RED or BLU
    private Allegiance userAllegiance;
    
    /**
     * Create game data from server data. Units are missing and not yet available.
     * @param rows
     * @param cols
     * @param map 
     */
    public GameData(int rows, int cols, char[][] map) {
        this.playfield = new Playfield(rows, cols);
        this.playfield.setMap(map);
    }
    
    /**
     * Update player names, score, unit and player on turn, and attacking flag.
     * @param playerBlu
     * @param playerRed
     * @param scoreBlu
     * @param scoreRed
     * @param onTurnID
     * @param attacking
     * @param player 
     */
    public void updateScore(String playerBlu, String playerRed, int scoreBlu, int scoreRed, int onTurnID, boolean attacking, char player) {
        this.playerBlue = playerBlu;
        this.playerRed = playerRed;
        this.blueScore = scoreBlu;
        this.redScore = scoreRed;
        this.userAllegiance = Allegiance.getAllegianceByName(player);
        this.setUnitOnTurn(getUnitByID(onTurnID));
        this.attacking = attacking;
    }

    @Override
    public void setUnitOnTurn(Unit val) {
        //if (onTurn != null) {
        //    Unit first = units.remove(0);
        //    units.add(first);
        //}
        this.onTurn = val;
    }

    @Override
    public Unit getUnitOnTurn() {
        return this.onTurn;
    }

    /**
     * Check if player is able to play this turn.
     * @return 
     */
    @Override
    public boolean playerOnTurn() {
        return this.onTurn.getAllegiance() == this.userAllegiance;
    }
    
    /**
     * Check if there is a unit on target hex set by axial coordinates.
     * @param r
     * @param q
     * @return 
     */
    public boolean checkOccupied(int r, int q) {
        for (Unit val : units) {
            if (val.getCoordX() == q && val.getCoordZ() == r) {
                if (val.isMovable()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get unit on hex set by axial coordinates.
     * @param r
     * @param q
     * @return unit/null
     */
    @Override
    public Unit getUnitOnCoords(int r, int q) {
        for (Unit val : units) {
            if (val.getType() != Unit.UnitType.FLAG && !val.isDead() && val.getCoordX() == q && val.getCoordZ() == r) {
                return val;
            }
        }
        return null;
    }
    
    /**
     * Get unit with type FLAG on hex set by axial coordinates.
     * @param r
     * @param q
     * @return unit/null
     */
    public Unit getFlagOnCoords(int r, int q) {
        for (Unit val : units) {
            if (val.getType() == Unit.UnitType.FLAG && val.getCoordX() == q && val.getCoordZ() == r) {
                return val;
            }
        }
        return null;
    }

    /**
     * Get unit by its ID.
     * @param ID
     * @return unit/null
     */
    @Override
    public Unit getUnitByID(int ID) {
        for (Unit val : units) {
            if (val.getID() == ID) {
                return val;
            }
        }
        return null;
    }
    
    @Override
    public boolean isUpdated() {
        return this.updated;
    }
    
    @Override
    public void setUpdated(boolean val) {
        this.updated = val;
    }

    /**
     * Get game data and set updated flag to false.
     * @return 
     */
    @Override
    public GameData getUpdates() {
        this.updated = false;
        return this;
    }

    /**
     * Execute action on target hex set by axial coordinates according to the game state.
     * Unit will either attack or move (or do nothing if the move is forbiden by rules)
     * @param row
     * @param col
     * @return I played/I can't do that
     */
    @Override
    public boolean play(int row, int col) {
        Unit unit = this.getUnitOnTurn();
        IController control = ClientController.getInstance(); //HACK - this should not be dependent on control package

        if (this.playerOnTurn()) {
            if (attacking) {
                //TODO: check attack range
                Unit attacked = getUnitOnCoords(row, col);
                if (attacked != null && attacked.isAttackable(userAllegiance)) {
                    Object o[] = {unit.getID(), attacked.getID()};
                    control.addToOutputQueue(new Command(control.getClientID(), MsgType.ATTACK, (short) 2, o));
                }
            } else {
                //TODO: check move range
                Unit occupied = getUnitOnCoords(row, col); //check occupied
                if (occupied == null) {
                    Object o[] = {unit.getID(), row, col};
                    control.addToOutputQueue(new Command(control.getClientID(), MsgType.MOVE, (short) 3, o));
                } else {
                    return false; //occupied
                }
            }
        } else {
            return false;
        }

        return true;
    }
    
    /**
     * Try to capture unit on axial coordinates and send result to server.
     * @param capturerID
     * @param flagRow
     * @param flagCol 
     */
    @Override
    public void capture(int capturerID, int flagRow, int flagCol) {
        Unit captured = getFlagOnCoords(flagRow, flagCol);
        IController control = ClientController.getInstance(); //HACK - this should not be dependent on control package
        if (captured != null && captured.isCapturable(this.userAllegiance)) {
            Object p[] = {capturerID, captured.getID()};
            control.addToOutputQueue(new Command(control.getClientID(), MsgType.CAPTURE, (short) 2, p));
        }
    }

    @Override
    public Playfield getPlayField() {
        return playfield;
    }

    @Override
    public List<Unit> getUnits() {
        return units;
    }

    public void setUnits(List<Unit> units) {
        this.units = units;
    }

    /**
     * @return the playerBlue
     */
    @Override
    public String getPlayerBlue() {
        return playerBlue;
    }

    /**
     * @return the playerRed
     */
    @Override
    public String getPlayerRed() {
        return playerRed;
    }

    /**
     * @return the blueScore
     */
    @Override
    public int getBlueScore() {
        return blueScore;
    }

    /**
     * @return the redScore
     */
    @Override
    public int getRedScore() {
        return redScore;
    }

    /**
     * @return the playerAllegiance
     */
    public Allegiance getUserAllegiance() {
        return userAllegiance;
    }

    @Override
    public void setAttacking(boolean val) {
        this.attacking = val;
    }

    @Override
    public boolean getAttacking() {
        return this.attacking;
    }
}
