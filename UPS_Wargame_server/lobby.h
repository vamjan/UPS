/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   lobby.h
 * Author: sini
 *
 * Created on 30 November 2016, 00:10
 */

#ifndef LOBBY_H
#define LOBBY_H

#include "server.h"

#define FREE "FREE"
#define READY "RDY"
#define NOT_READY "NOT"

lobby *create_lobby(char *name);
void destroy_lobby(lobby **target);

int add_player(lobby *target, client_data *player);
int remove_player(lobby *target, client_data *player);
int lobby_is_empty(lobby *target);
int player_present( lobby *target, client_data *player);
void kick_inactive(lobby *target);
char **parse_lobby(lobby *target, int index);
int toggle_ready(lobby *target, client_data *player);
int check_ready(lobby *target);
int start_game(lobby *target);
int reset_lobby(lobby *target);

#endif /* LOBBY_H */

