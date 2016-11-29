/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   parser.c
 * Author: sini
 *
 * Created on 26 November 2016, 20:58
 */

#include <stdio.h>
#include <stdlib.h>
#include "parser.h"
#include "net_interface.h"

command *create_command(int id_key, msg_type type, short length, char **data) {
    command *retval = malloc(sizeof (command));

    retval->id_key = id_key;
    retval->type = type;
    retval->length = length;
    retval->data = data;

    return retval;
}

int destroy_command(command **command) {
    logger("INFO", "Destroying command");

    if ((*command)->data) free((*command)->data);
    free(*command);
    *command = NULL;

    return 1;
}

command *parse_input(const char *msg, const int read) {
    int i, start, msg_length = strlen(msg);
    command *retval = NULL;
    for (i = read; i < msg_length; i++) {
        if (msg[i] == '\n') {
            char id[9];
            id[8] = 0;
            memcpy(id, msg + (i - 8), sizeof (char) * 8);
            start = find_command_start(msg, id, i);
            int length = i - start;
            if (length < 24)
                printf("BAD!\n"); //TODO: return bad

            char tmp[length + 1];
            tmp[length] = 0;
            memcpy(tmp, msg + start, sizeof (char) * length);

            retval = parse_string(tmp);

            break;
        }
    }

    return retval;
}

int find_command_start(const char *msg, const char *id, const int end) {
    int i = end - 8 - 1;

    for (; i > 0; i--) {
        char tmp[9];
        tmp[8] = 0;
        memcpy(tmp, msg + i, sizeof (char) * 8);
        if (!strcmp(id, tmp)) {
            return i;
        }
    }

    return end;
}

command *parse_string(const char *msg) {
    int id, i;
    short length;
    msg_type type;
    char *token, *tmp = strdup(msg), **data, *err;
    command *retval = NULL;

    token = strtok(tmp, DELIM);
    if (token)id = (int) strtol(token, &err, 16);
    if (*err != '\0') return retval;

    token = strtok(NULL, DELIM);
    if(token) type = get_type(token[0]);

    token = strtok(NULL, DELIM);
    if (token) length = (short) strtol(token, &err, 16); //TODO: possible overflow
    if (*err != '\0' && (length >= 0 && length < 32767)) return retval;

    data = malloc(sizeof (char *) * (length));
    i = 0; //reuse

    token = strtok(NULL, DELIM);
    while (token && i < length) {
        data[i++] = strdup(token);
        token = strtok(NULL, DELIM);
    }

    retval = create_command(id, type, length, data);

    free(tmp);

    return retval;
}

char *parse_command(const command *command) {
    char retval[BUFFER_LENGTH], *data;
    
    if(command->data) {
        data = strdup(parse_data(command->data, command->length));
        snprintf(retval, BUFFER_LENGTH, "%08X|%c|%04X|%s|%08X", command->id_key, command->type, (short)command->length, data, command->id_key);
        free(data);
    } else {
        snprintf(retval, BUFFER_LENGTH, "%08X|%c|%04X||%08X", command->id_key, command->type, (short)command->length, command->id_key);
    }
    
    return retval;
}

char *parse_data(const char **data, const int length) {
    char retval[BUFFER_LENGTH];
    int i, size = 0;
    
    for(i = 0; i < length; i++) {
        memcpy(retval+size, data[i], strlen(data[i]));
        size += strlen(data[i]);
        if(i != length-1) retval[size++] = DELIM[0];
    }
    
    return retval;
}

char *parse_output(const command *command) {
    char retval[BUFFER_LENGTH];
    
    snprintf(retval, BUFFER_LENGTH, "%s\n", parse_command(command));
    
    return retval;
}

msg_type get_type(const char c) {
    msg_type retval;
    switch (c) {
        case 'X': 
            retval = ACK;
            break;
        case 'Y':
            retval = NACK;
            break;
        case 'M':
            retval = MESSAGE;
            break;
        default: 
            retval = 0;
            break;
    }
    return retval;
}
