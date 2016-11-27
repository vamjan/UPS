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
                printf("BAD!\n");

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
    char *token, *tmp = strdup(msg);
    command *retval = NULL;
    token = strtok(tmp, DELIM);

    while (token != NULL) {
        printf("%s\t%p\n", token, token);

        token = strtok(NULL, DELIM);
    }
    
    free(tmp);
    free(token);
    
    return retval;
}

char *parse_command(const command *command) {

}

char *parse_output(const command *command) {

}