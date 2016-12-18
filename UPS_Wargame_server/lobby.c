/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "server.h"
#include "lobby.h"

extern char *strdup(const char *s);

lobby *create_lobby(char *name) {
    lobby *retval = calloc(sizeof (lobby), 1);

    strncpy(retval->lobby_name, name, NAME_LENGTH);
    retval->running = 0;
    retval->game_in_progress = 0;
    //retval->pf = create_playfield(DEFAULT_COLS, DEFAULT_ROWS);

    return retval;
}

void destroy_lobby(lobby **target) {
    destroy_playfield(&(*target)->pf);
    free(*target);
    *target = NULL;
}

int add_player(lobby *target, client_data *player) {
    if((target->player_one && target->player_one->id_key == player->id_key) 
            || (target->player_two && target->player_two->id_key == player->id_key)) {
        return 0;
    }
    if (!target->player_one) {
        target->player_one = player;
        return 1;
    }//success
    else if (!target->player_two) {
        target->player_two = player;
        return 1;
    }//success
    else return 0; //failure
}

int remove_player(lobby *target, client_data *player) {
    if (target->player_two) {
        target->player_two = NULL;
        return 1;
    }//success
    else if (target->player_one) {
        target->player_one = NULL;
        return 1;
    }//success
    else return 0; //failure
}

int lobby_is_empty(lobby *target) {
    if (!target->player_one && !target->player_two)
        return 1;
    else
        return 0;
}

char *parse_lobby(lobby *target, int index) {
    char retval[BUFFER_LENGTH];

    memset(retval, 0, sizeof (retval));

    char *player_one;
    char *player_two;
    if (target->player_one) player_one = target->player_one->player_name;
    else player_one = FREE;
    if (target->player_two) player_two = target->player_two->player_name;
    else player_two = FREE;

    snprintf(retval, BUFFER_LENGTH, "%d|%s|%c|%s|%s", index, target->lobby_name, target->game_in_progress ? 'T' : 'F', player_one, player_two);

    return strdup(retval);
}