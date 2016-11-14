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
    CONNECT('C') { //sync ID with server
        @Override
        public void invokeInputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void invokeOutputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }, MESSAGE('M') { //send or recieve text message to other clients
        @Override
        public void invokeInputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void invokeOutputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }, ERROR('R') { //send or recieve msg describing some error on client/server
        @Override
        public void invokeInputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.           
        }

        @Override
        public void invokeOutputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }, DISCONNECT('D') { //server or client is disconnecting client
        @Override
        public void invokeInputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void invokeOutputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }, START('S') { //command server to start game or server sending msg that game is starting
        @Override
        public void invokeInputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void invokeOutputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }, END('E') { //server or client ended the game session
        @Override
        public void invokeInputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void invokeOutputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }, MOVE('O') { //move unit from [X1, Y1] to [X2, Y2]
        @Override
        public void invokeInputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void invokeOutputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }, ATTACK('A') { //unit A attacked unit B for X dmg
        @Override
        public void invokeInputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void invokeOutputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }, CAPTURE('P') { //unit A captured zone Z
        @Override
        public void invokeInputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void invokeOutputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }, CREATE_LOBBY('L') { //create lobby/lobby is created and is waiting for you
        @Override
        public void invokeInputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void invokeOutputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }, JOIN_LOBBY('J') { //join open lobby
        @Override
        public void invokeInputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void invokeOutputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }, LEAVE_LOBBY('V') { //leave open lobby/you were kicked from lobby
        @Override
        public void invokeInputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void invokeOutputMethod() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };

    abstract public void invokeInputMethod();
    abstract public void invokeOutputMethod();

    private char name;

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
    
    public static void parseInputData(String input) {
        
    }
}
