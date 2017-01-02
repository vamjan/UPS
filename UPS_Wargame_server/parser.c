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
#include <string.h>
#include "parser.h"
#include "sini_log.h"
#include "net_interface.h"

extern char *strdup(const char *s);

command *create_command(int id_key, msg_type type, short length, char **data) {
    command *retval = malloc(sizeof (command));

    retval->id_key = id_key;
    retval->type = type;
    retval->length = length;
    retval->data = data;

    return retval;
}

command *create_ack(int id_key, int type) {
    command *retval = malloc(sizeof (command));

    retval->id_key = id_key;
    retval->type = type ? ACK : NACK;
    retval->length = 0;
    retval->data = NULL;

    return retval;
}

int destroy_command(command **command) {
    logger("INFO", "Destroying command");

    if ((*command)->data) {
        int i;
        for(i = 0; i < (*command)->length; i++) {
            free((*command)->data[i]);
        }
        free((*command)->data);
        (*command)->data = NULL;
    }
    free(*command);
    *command = NULL;

    return 1;
}

command *parse_input(const char *msg, int *read) {
    int i, start, prev_end = *read, msg_length = strlen(msg); //TODO: bordeeel
    command *retval = NULL;
    for (i = *read; i < prev_end + msg_length; i++) {
        (*read)++;
        if (msg[i] == '\n') {
            char id[9];
            id[8] = 0;
            memcpy(id, msg + (i - 8), sizeof (char) * 8);
            start = find_command_start(msg, id, i);
            int length = i - start;
            if (length < 24) {
                printf("[PARSER]: BAD!\n");
                return retval;
            }
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

    for (; i >= 0; i--) {
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
    if (token) type = get_type(token[0]);

    token = strtok(NULL, DELIM);
    if (token) length = (short) strtol(token, &err, 16); //TODO: possible overflow
    if (*err != '\0' && (length >= 0 && length < 32767)) return retval;

    data = calloc(sizeof (char *), length);
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

    memset(retval, 0, sizeof(char) * BUFFER_LENGTH);

    if (command->length) {
        data = parse_data(command->data, command->length);
        snprintf(retval, BUFFER_LENGTH, "%08X|%c|%04X|%s|%08X", command->id_key, command->type, (short) command->length, data, command->id_key);
        free(data);
    } else {
        snprintf(retval, BUFFER_LENGTH, "%08X|%c|%04X||%08X", command->id_key, command->type, (short) command->length, command->id_key);
    }

    return strdup(retval);
}

char *parse_data(char **data, const int length) {
    char retval[BUFFER_LENGTH];
    int i, size = 0;

    memset(retval, 0, sizeof(char) * BUFFER_LENGTH);

    for (i = 0; i < length; i++) {
        memcpy(retval + size, data[i], strlen(data[i]));
        size += strlen(data[i]);
        if (i != length - 1) retval[size++] = DELIM[0];
    }

    return strdup(retval);
}

char *parse_output(const command *command) {
    char retval[BUFFER_LENGTH];
    
    memset(retval, 0, sizeof(char) * BUFFER_LENGTH);
    snprintf(retval, BUFFER_LENGTH, "%s\n", parse_command(command));

    return strdup(retval);
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
        case 'C':
            retval = CONNECT;
            break;
        case 'G':
            retval = GET_SERVER;
            break;
        case 'L':
            retval = CREATE_LOBBY;
            break;
        case 'J':
            retval = JOIN_LOBBY;
            break;
        case 'V':
            retval = LEAVE_LOBBY;
            break;
        case 'T':
            retval = TOGGLE_READY;
            break;
        case 'S':
            retval = START;
            break;
        case 'U':
            retval = UPDATE;
            break;
        case 'I':
            retval = UNITS;
            break;
        case 'E':
            retval = END;
            break;
        case 'O':
            retval = MOVE;
            break;
        case 'A':
            retval = ATTACK;
            break;
        case 'Z':
            retval = CAPTURE;
            break;
        case 'K':
            retval = SKIP;
            break;
        case 'P':
            retval = POKE;
            break;
        default:
            retval = 0;
            break;
    }
    return retval;
}
