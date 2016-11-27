/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.net_interface;

/**
 *
 * @author sini
 * ID|TYPE|LENGTH|DATA|ID\n
 * long|char|short|byte[]|long
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
        
        this.data = new Object[data.length];
        this.data = data.clone();
    }
    
    public Command(int clientID) {
        this.clientID = clientID;
        this.type = MsgType.ACK;
        this.length = 0;
        this.data = new Object[0];
    }
    
    public String dataToString() {
        String retval = "";
        
        for(Object val : data)
            retval += (val.toString() + "|");
        
        if(!retval.equals(""))
            retval = retval.substring(0, retval.length()-1); //clip last carka
        
        return retval;
    }
    
    @Override
    public String toString() {
        return String.format("%08X|%c|%04X|%s|%08X\n", this.clientID, this.type.getName(), this.length, this.dataToString(), this.clientID);
    }
}
