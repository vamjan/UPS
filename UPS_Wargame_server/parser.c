/*
 * This module represents the game state and contains functions to work with it 
 * and to run the game itself.
 * This module is using flat-toped even-r hexagons
 * Jan Vampol
 * UPS
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "parser.h"
#include "sini_log.h"
#include "net_interface.h"

extern char *strdup(const char *s);

/**
 * Allocates memory and creates command.
 * @param id_key
 * @param type
 * @param length
 * @param data
 * @return 
 */
command *create_command(int id_key, msg_type type, short length, char **data) {
    command *retval = malloc(sizeof (command));

    retval->id_key = id_key;
    retval->type = type;
    retval->length = length;
    retval->data = data;

    return retval;
}

/**
 * Create acknowledge command
 */
command *create_ack(int id_key, int type) {
    command *retval = malloc(sizeof (command));

    retval->id_key = id_key;
    retval->type = type ? ACK : NACK;
    retval->length = 0;
    retval->data = NULL;

    return retval;
}

/**
 * Destroy command and free its components
 * @param command
 * @return 
 */
int destroy_command(command **command) {
    logger("INFO", "Destroying command");

    if ((*command)->data) {
        int i;
        for (i = 0; i < (*command)->length; i++) {
            free((*command)->data[i]);
        }
        free((*command)->data);
        (*command)->data = NULL;
    }
    free(*command);
    *command = NULL;

    return 1;
}

/**
 * Takes the input string from net_interface buffer and tries to parse it into 
 * viable command. If succesful it returns the command.
 * Function goes through the string until it finds the \n character, takes the 8 
 * characters behind this character and tries to parse them into a integer. Then
 * goes through the function backwards and looks for the matching number. Argument
 * read makes sure this function does not read characters which were already read.
 * 
 * @param msg
 * @param read
 * @return command or NULL
 */
command *parse_input(const char *msg, int *read) {
    int i, start, prev_end = *read, msg_length = strlen(msg);
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

/**
 * Check characters in string until it finds the \n character.
 * @param msg
 * @param id
 * @param end
 * @return 
 */
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

/**
 * Try to parse incoming message into command structure.
 * Split the message with DELIMINER | and parse each argument.
 * Command ID|TYPE|LENGTH|DATA|ID\n int|char|short|char[][]|int
 * @param msg
 * @return command or NULL
 */
command *parse_string(const char *msg) {
    int id;
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

    //start parsing data
    memset(tmp, 0, strlen(msg));
    memcpy(tmp, msg + 16, strlen(msg + 16) - 9);

    data = magic_data(tmp, length);

    retval = create_command(id, type, length, data);

    free(tmp);

    return retval;
}

/**
 * Parse message data and escape characters
 * @param msg
 * @param count
 * @return 
 */
char **magic_data(char const *msg, int count) {
    char **retval = calloc(sizeof (char *), count);
    int i = 0, read = 0, data_count = 0, max_length = strlen(msg), escaped = 0;
    char tmp[max_length+1];
    memset(tmp, 0, max_length+1);

    if (count > 0) {
        while (read < max_length && data_count < count) {
            if (msg[read] == '~') {
                read++;
                escaped = 1;
            }

            if (msg[read] == DELIM[0] && !escaped) {
                retval[data_count++] = strdup(tmp);
                memset(tmp, 0, max_length+1);
                escaped = 0;
                i = 0;
                read++;
            } else {
                tmp[i++] = msg[read++];
            }
        }
        
        retval[data_count] = strdup(tmp);
    }

    return retval;
}

/**
 * Parse command into string which can be send via network to clients.
 * @param command
 * @return 
 */
char *parse_command(const command *command) {
    char retval[BUFFER_LENGTH], *data;

    memset(retval, 0, sizeof (char) * BUFFER_LENGTH);

    if (command->length) {
        data = parse_data(command->data, command->length);
        snprintf(retval, BUFFER_LENGTH, "%08X|%c|%04X|%s|%08X", command->id_key, command->type, (short) command->length, data, command->id_key);
        free(data);
    } else {
        snprintf(retval, BUFFER_LENGTH, "%08X|%c|%04X||%08X", command->id_key, command->type, (short) command->length, command->id_key);
    }

    return strdup(retval);
}

/**
 * Every data entry gets a deliminer | and is added to output string.
 * @param data
 * @param length
 * @return 
 */
char *parse_data(char **data, const int length) {
    char retval[BUFFER_LENGTH];
    int i, j, size = 0;

    memset(retval, 0, sizeof (char) * BUFFER_LENGTH);

    for (i = 0; i < length; i++) {
        int escaped = 0;
        memcpy(retval + size, data[i], strlen(data[i]));
        for(j = size; j < strlen(retval); j++) {
            if(retval[j] == '~' || retval[j] == '|') {
                memmove(retval + j + 1, retval + j, strlen(data[i]) - j + size + escaped); //make room for escape character
                retval[j++] = '~';
                escaped++;
            }
        }
        size = strlen(retval);
        if (i != length - 1) retval[size++] = DELIM[0]; //add delim between each data strings
    }

    return strdup(retval);
}

/**
 * Takes the parsed command and saves it as string
 * @param command
 * @return 
 */
char *parse_output(const command *command) {
    char retval[BUFFER_LENGTH];

    memset(retval, 0, sizeof (char) * BUFFER_LENGTH);
    snprintf(retval, BUFFER_LENGTH, "%s\n", parse_command(command));

    return strdup(retval);
}

/**
 * Parse move command information to string to be send as additional information to client
 * @param ID of unit
 * @param coordX of unit
 * @param coordZ of unit
 * @return array of strings as move info
 */
char **parse_move(int ID, int coordX, int coordZ) {
    char **retval = malloc(sizeof (char *) * 3);

    retval[0] = calloc(sizeof (char), 4);
    snprintf(retval[0], 4, "%d", ID);
    retval[1] = calloc(sizeof (char), 4);
    snprintf(retval[1], 4, "%d", coordX);
    retval[2] = calloc(sizeof (char), 4);
    snprintf(retval[2], 4, "%d", coordZ);

    return retval;
}

/**
 * Parse attack command information to string to be send as additional information to client
 * @param attacker unit
 * @param target unit
 * @return array of strings representing attack info
 */
char **parse_attack(unit *attacker, unit * target) {
    char **retval = malloc(sizeof (char *) * 3);

    retval[0] = calloc(sizeof (char), 4);
    snprintf(retval[0], 4, "%d", attacker->ID);
    retval[1] = calloc(sizeof (char), 4);
    snprintf(retval[1], 4, "%d", target->ID);
    retval[2] = calloc(sizeof (char), 4);
    snprintf(retval[2], 4, "%d", target->health);

    return retval;
}

/**
 * Parse capture command information to string to be send as additional information to client
 * @param capturer capturing unit
 * @param captured captured unit
 * @return array of strings representing capture info
 */
char **parse_capture(unit *capturer, unit * captured) {
    char **retval = malloc(sizeof (char *) * 2);

    retval[0] = calloc(sizeof (char), 4);
    snprintf(retval[0], 4, "%d", capturer->ID);
    retval[1] = calloc(sizeof (char), 4);
    snprintf(retval[1], 4, "%d", captured->ID);

    return retval;
}

/**
 * Get type according to first character in command
 * Each type is described in execute_command function and in the header file. 
 * @param c
 * @return 
 */
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
        case 'R':
            retval = RECONNECT;
            break;
        case 'D':
            retval = DISCONNECT;
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
