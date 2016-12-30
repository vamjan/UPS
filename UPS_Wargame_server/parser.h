/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   Parser.h
 * Author: sini
 *
 * Created on 26 November 2016, 20:44
 */

#ifndef PARSER_H
#define PARSER_H

#define DELIM "|"

typedef enum {
    ACK = 'X',
    NACK = 'Y',
    MESSAGE = 'M',
    CONNECT = 'C', //sync ID with server
    GET_SERVER = 'G', //get available server data
    CREATE_LOBBY = 'L', //create lobby/lobby is created and is waiting for you
    JOIN_LOBBY = 'J', //join open lobby
    LEAVE_LOBBY = 'V', //leave open lobby/you were kicked from lobby
    TOGGLE_READY = 'T',
    START = 'S',
    UNITS = 'I',
    UPDATE = 'U',
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
char *parse_command(const command *command);
char *parse_data(char **data, const int length);
msg_type get_type(const char c);

#endif /* PARSER_H */

