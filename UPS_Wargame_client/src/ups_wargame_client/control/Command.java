/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.control;

import ups_wargame_client.net_interface.MsgType;

/**
 *
 * @author sini ID|TYPE|LENGTH|DATA|ID\n long|char|short|byte[]|long
 */
public class Command {

    int clientID;
    MsgType type;
    short length;
    Object[] data;

    public Command(int clientID, MsgType type, short length, Object[] data) {
        this.clientID = clientID;
        this.type = type;
        this.length = length;
        if (length > 0) {
            this.data = new Object[data.length];
            this.data = data.clone();
        }
    }

    public Command(int clientID) {
        this.clientID = clientID;
        this.type = MsgType.ACK;
        this.length = 0;
        this.data = new Object[0];
    }
    /**
     * 
     * @return 
     */
    public String dataToString() {
        String retval = "";
        if (this.length > 0) {
            for (Object val : data) {
                retval += (val.toString() + "|");
            }

            if (!retval.equals("")) {
                retval = retval.substring(0, retval.length() - 1); //clip last carka
            }
        }
        return retval;
    }

    @Override
    public String toString() {
        return String.format("%08X|%c|%04X|%s|%08X\n", this.clientID, this.type.getName(), this.length, this.dataToString(), this.clientID);
    }

    public static boolean requiresAck(Command c) {
        switch(c.type) {
            case CREATE_LOBBY:
                return true;
            case JOIN_LOBBY:
                return true;
            case LEAVE_LOBBY:
                return true;
            case MOVE:
                return true;
            case ATTACK:
                return true;
            case CAPTURE:
                return true;
            default:
                return false;
        }
    }
}
