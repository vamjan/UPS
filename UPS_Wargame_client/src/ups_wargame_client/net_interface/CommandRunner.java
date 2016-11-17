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
public class CommandRunner {
    
    public CommandRunner() { }
    
    public boolean executeCommand(Command command) {
        switch(command.type) {
            case CONNECT: break;
            case MESSAGE: break;
            case ERROR: break;
            default: return false;
        }
        return true;
    }
    
    private void connect() {
        
    }
}
