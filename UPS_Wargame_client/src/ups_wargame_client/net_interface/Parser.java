/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ups_wargame_client.net_interface;

/**
 *
 * @author sini
 * 
 * ID|TYPE|LENGTH|DATA|ID\n
 * long|char|short|byte[]|long
 */
public class Parser {
    
    /**
     * Private constructor to make class static
     */
    private Parser() { }
    
    public static Command parseInput(String input) {
        Command retval = null;
        //placeholder
        String[] tmp = input.split("\\|");
        
        retval = new Command((int)Long.parseLong(tmp[0], 16), MsgType.getMsgTypeByName(tmp[1].charAt(0)), 
                            Short.parseShort(tmp[2]), tmp[3], tmp[4], tmp[5], tmp[6]);
        //end of placeholder
        return retval;
    }
    
    public static String parseOutput(Command output) {
        return output.toString();
    }
}
