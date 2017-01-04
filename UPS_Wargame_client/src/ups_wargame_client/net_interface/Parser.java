package ups_wargame_client.net_interface;

import ups_wargame_client.control.Command;

/**
 * Class for parsing input strings from the network.
 * Format: ID|TYPE|LENGTH|DATA|ID\n long|char|short|byte[]|long
 * @author sini
 */
public class Parser {

    private static final int MAX_MESSAGE_LENGTH = 1000;

    /**
     * Private constructor to make class static
     */
    private Parser() {
    }
    
    /**
     * Parse input from input string to command. Firstly it will look for command end symbol \n
     * and start parsing command backwards from that. It then takes 8 symbols before end as ID
     * and looks for the same sequence somewhere in the data. When found, it determines the command
     * and tries to parse it.
     * @param input
     * @return 
     */
    public static Command parseInput(String input) {
        Command retval = null;

        if (input == null) {
            System.err.println("PARSER: No input!");
            return retval;
        }

        if (input.length() > MAX_MESSAGE_LENGTH || input.length() < 24) {
            System.err.println("PARSER: Message has wrong length!");
            return retval;
        }
        try {
            String id = input.substring(input.length() - 8, input.length()); //get ID at the end of message
            int idNum = (int) Long.parseLong(id, 16);

            String com = findCommand(input, id);
            String[] tmp = com.split("\\|");

            MsgType type = MsgType.getMsgTypeByName(tmp[0].charAt(0));
            if (type == null) {
                throw new Exception();
            }

            short msgLen = Short.parseShort(tmp[1], 16);
            Object[] array = new Object[msgLen];

            if (msgLen > 0) {
                int dataCount = tmp.length - 2;
                if (dataCount > msgLen) { //more data means skipping some deliminers and leaving them in data
                    dataCount = dataCount / msgLen;
                    for(int i = 0; i < msgLen; i++) {
                        String hlp = "";
                        for(int j = 0; j < dataCount; j++) {
                            hlp += tmp[2 + j + i*dataCount];
                            hlp += "|";
                        }
                        hlp = hlp.substring(0, hlp.length()-1); //clip last delim
                        array[i] = hlp;
                    }
                } else {
                    for (int i = 2; i < tmp.length; i++) {
                        array[i - 2] = tmp[i];
                    }
                }
            }

            retval = new Command(idNum, type, msgLen, array);
        } catch (Exception e) {
            System.err.println("PARSER: Message format is wrong: " + e.getMessage());
        }
        return retval;
    }

    public static String parseOutput(Command output) {
        return output.toString();
    }

    /**
     * Look for the same sequence as id somewhere in the remaining string. From back to start.
     * @param msg
     * @param id
     * @return 
     */
    private static String findCommand(String msg, String id) {
        String retval = null;

        for (int i = 0; i < msg.length() - 16; i++) {
            String curr = msg.substring(i, i + 8);
            if (curr.equals(id)) {
                retval = msg.substring(i + 9, msg.length() - 9);
                break;
            }
        }

        return retval;
    }
}
