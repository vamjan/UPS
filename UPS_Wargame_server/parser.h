/* 
 * File:   Parser.h
 * Author: Jan Vampol
 *
 * Created on 26 November 2016, 20:44
 */
#include "hex.h"

#ifndef PARSER_H
#define PARSER_H

#define DELIM "|"

/**
 * Message types
 */
typedef enum {
    ACK = 'X', //acknowledge message, only output
    NACK = 'Y', //refuse command, only output
    MESSAGE = 'M', //send chat messages
    CONNECT = 'C', //sync ID with server
    RECONNECT = 'R', //send to client if he can reconnect to game/client sends back R or nothing
    DISCONNECT = 'D', //send to client if opponent left
    GET_SERVER = 'G', //get available server data
    CREATE_LOBBY = 'L', //create lobby/lobby is created and is waiting for you
    JOIN_LOBBY = 'J', //join open lobby
    LEAVE_LOBBY = 'V', //leave open lobby/you were kicked from lobby
    TOGGLE_READY = 'T', //toggle ready status, only input
    START = 'S', //start game and send map layout, only output
    UNITS = 'I', //send unit data, only output
    UPDATE = 'U', //send game data, only output
    END = 'E', //server or client ended the game session
    MOVE = 'O', //move unit from [X1, Y1] to [X2, Y2]
    ATTACK = 'A', //unit A attacks unit B for X dmg
    CAPTURE = 'Z', //unit A captured unit Z
    SKIP = 'K', //player skips action
    POKE = 'P'
} msg_type;

typedef struct {
    int id_key;
    msg_type type;
    short length;
    char **data;
} command;

command *create_command(int id_key, msg_type type, short length, char **data);
command *create_ack(int id_key, int type);
int destroy_command(command **command);
command *parse_input(const char *msg, int *read);
int find_command_start(const char *msg, const char *id, const int end);
command *parse_string(const char *msg);
char *parse_output(const command *command);
char **magic_data(char const *msg, int count);
char *parse_command(const command *command);
char *parse_data(char **data, const int length);
char **parse_move(int ID, int coordX, int coordZ);
char **parse_attack(unit *attacker, unit *target);
char **parse_capture(unit *capturer, unit *captured);
msg_type get_type(const char c);

#endif /* PARSER_H */

