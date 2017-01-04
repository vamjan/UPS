/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.net_interface;

/**
 *
 * @author sini
 */
public enum MsgType {
    CONNECT('C'), //sync ID with server
    MESSAGE('M'), //send or recieve text message to other clients
    RECONNECT('R'), //recieve a message saying you are able to reconnect/say you want to reconnect to active game
    DISCONNECT('D'), //server or client is disconnecting client
    GET_SERVER('G'), //get available server data
    
    START('S'), //command server to start game or server sending msg that game is starting
    UNITS('I'), //send units from server
    UPDATE('U'), //get score and player update
    
    END('E'), //server or client ended the game session
    MOVE('O'), //move unit from [X1, Y1] to [X2, Y2]
    ATTACK('A'), //unit A attacks unit B for X dmg
    CAPTURE('Z'), //unit A captured unit Z
    SKIP('K'), //player skips action
    
    GET_LOBBY('G'), //get available lobby data
    CREATE_LOBBY('L'), //create lobby/lobby is created and is waiting for you
    JOIN_LOBBY('J'), //join open lobby/update lobby info
    TOGGLE_READY('T'),
    LEAVE_LOBBY('V'), //leave open lobby/you were kicked from lobby
    
    ACK('X'), //acknowledge success
    NACK('Y'), //
    POKE('P');
    

    private char name;

    public char getName() {
        return this.name;
    }

    private MsgType(char val) {
        name = val;
    }

    public static MsgType getMsgTypeByName(final char name) {
        for (MsgType type : MsgType.values()) {
            if (type.name == name) {
                return type;
            }
        }
        return null;
    }
}
