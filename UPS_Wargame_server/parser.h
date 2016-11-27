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
    ACK = 'X', NACK = 'Y', MESSAGE = 'M'
} msg_type;

typedef struct {
    int id_key;
    msg_type type;
    short length;
    char **data;
} command;

command *create_command(int id_key, msg_type type, short length, char **data);
int destroy_command(command **command);
command *parse_input(const char *msg, const int read);
int find_command_start(const char *msg, const char *id, const int end);
command *parse_string(const char *msg);
char *parse_output(const command *command);
char *parse_command(const command *command);

#endif /* PARSER_H */

