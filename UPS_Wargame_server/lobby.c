/**
 * This module contains functions for lobby management
 * Jan Vampol
 * UPS
 */

#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "server.h"
#include "lobby.h"

extern char *strdup(const char *s);

/**
 * Allocates memory and creates a lobby structure
 * @param name
 * @return 
 */
lobby *create_lobby(char *name) {
    lobby *retval = calloc(sizeof (lobby), 1);

    strncpy(retval->lobby_name, name, NAME_LENGTH);
    retval->game_in_progress = 0;

    return retval;
}

/**
 * Destroys target lobby and frees its components
 * @param target
 */
void destroy_lobby(lobby **target) {
    destroy_playfield(&(*target)->pf);
    free(*target);
    *target = NULL;
}

/**
 * Adds a player on a free spot in the lobby. Player-one first.
 * @param target
 * @param player
 * @return 
 */
int add_player(lobby *target, client_data *player) {
    if ((target->player_one && target->player_one->id_key == player->id_key)
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

/**
 * Removes given player from lobby.
 * @param target
 * @param player
 * @return 
 */
int remove_player(lobby *target, client_data *player) {
    if (target->player_two && target->player_two->id_key == player->id_key) {
        target->player_two = NULL;
        target->ready_two = 0;
        return 1;
    }//success
    else if (target->player_one && target->player_one->id_key == player->id_key) {
        target->player_one = NULL;
        target->ready_one = 0;
        return 1;
    }//success
    else return 0; //failure
}

/**
 * Determines if players in lobby are active
 * @param target
 * @return 
 */
int lobby_is_empty(lobby *target) {
    if (target->player_one && target->player_two) {
        if(!target->player_one->active && !target->player_two->active) {
            return 1;
        } else {
            return 0;
        }
    } else {
        return 0; 
    }
}

/**
 * Determines if given player is present in the lobby
 * @param target
 * @param player
 * @return 
 */
int player_present(lobby *target, client_data *player) {
    if (target->player_two && target->player_two->id_key == player->id_key) {
        return 1;
    }//success
    else if (target->player_one && target->player_one->id_key == player->id_key) {
        return 1;
    }//success
    else return 0; //failure
}

/**
 * Kick inactive players from the lobby
 * @param target
 */
void kick_inactive(lobby *target) {
    if (target->player_two && !target->player_two->active) {
        remove_player(target, target->player_two);
    }
    if (target->player_one && !target->player_one->active) {
        remove_player(target, target->player_one);
    }
}

/**
 * Parse lobby data to be send via network to clients
 * INDEX|NAME|GAMEPROGRESS|PLONE|RDYONE|PLTWO|RDYTWO
 * @param target
 * @param index
 * @return 
 */
char **parse_lobby(lobby *target, int index) {
    char **retval = malloc(sizeof(char *) * 7);
    retval[0] = calloc(sizeof (char), 4);
    snprintf(retval[0], 4, "%d", index);
    retval[1] = strdup(target->lobby_name);
    retval[2] = calloc(sizeof(char), 2);
    retval[2][0] = (target->game_in_progress) ? 'T' : 'F';
    retval[3] = (target->player_one) ? strdup(target->player_one->player_name) : strdup(FREE);
    retval[4] = calloc(sizeof(char), 2);
    retval[4][0] = (target->ready_one) ? 'T' : 'F';
    retval[5] = (target->player_two) ? strdup(target->player_two->player_name) : strdup(FREE);
    retval[6] = calloc(sizeof(char), 2);
    retval[6][0] = (target->ready_two) ? 'T' : 'F';

    return retval;
}

/**
 * Toggle ready status of the given player
 * @param target
 * @param player
 * @return 
 */
int toggle_ready(lobby *target, client_data *player) {
    if (target->player_two && target->player_two->id_key == player->id_key) {
        target->ready_two = (target->ready_two == 0) ? 1 : 0;
        return 1;
    }//success
    else if (target->player_one && target->player_one->id_key == player->id_key) {
        target->ready_one = (target->ready_one == 0) ? 1 : 0;
        return 1;
    }//success
    else return 0; //failure
}

/**
 * Check if both players are ready
 * @param target
 * @return 
 */
int check_ready(lobby *target) {
    if (target->player_two && target->player_one && target->ready_two  && target->ready_one) {
        return 1;
    }//success
    else return 0; //failure
}

/**
 * Reset lobby to the default state
 * @param target
 * @return 
 */
int reset_lobby(lobby *target) {
    if(target->pf)
        destroy_playfield(&(target->pf));
    target->pf = create_playfield(DEFAULT_ROWS, DEFAULT_COLS);
}