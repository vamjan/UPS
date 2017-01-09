package ups_wargame_client.control;

import ups_wargame_client.net_interface.MsgType;

/**
 * Class used to store command data and methods to manage/parse them.
 * Format: ID|TYPE|LENGTH|DATA|ID\n long|char|short|byte[]|long
 * @author Jan Vampol
 */
public class Command {
    //sender ID
    int clientID;
    //type of message
    MsgType type;
    //number of data entries
    short length;
    //data can be of any type and have to be converteed afterwards
    Object[] data;
    
    /**
     * Full constructor to setup all properties.
     * @param clientID
     * @param type
     * @param length
     * @param data 
     */
    public Command(int clientID, MsgType type, short length, Object[] data) {
        this.clientID = clientID;
        this.type = type;
        this.length = length;
        if (length > 0) {
            this.data = new Object[data.length];
            this.data = data.clone();
        }
    }
    
    /**
     * A quick way to create ACK command
     * @param clientID 
     */
    public Command(int clientID) {
        this.clientID = clientID;
        this.type = MsgType.ACK;
        this.length = 0;
        this.data = new Object[0];
    }
    /**
     * Parse command data to string.
     * @return 
     */
    public String dataToString() {
        String retval = "";
        if (this.length > 0) {
            for (Object val : data) {
                String tmp = val.toString();
                tmp = tmp.replace("~", "~~");
                tmp = tmp.replace("|", "~|");
                retval += tmp + "|";
            }

            if (!retval.equals("")) {
                retval = retval.substring(0, retval.length() - 1); //clip last deliminer
            }
        }
        
        return retval;
    }
    
    /**
     * toString override used for sending/writing
     * @return 
     */
    @Override
    public String toString() {
        return String.format("%08X|%c|%04X|%s|%08X\n", this.clientID, this.type.getName(), this.length, this.dataToString(), this.clientID);
    }
    
    /**
     * Returns true for message type requiring ACK/NACK from server.
     * @param c
     * @return 
     */
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
            case CONNECT:
                return true;
            default:
                return false;
        }
    }
}
