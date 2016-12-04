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

lobby *create_lobby(char *name);
void destroy_lobby(lobby **target);
void *start_lobby(void *params);
void stop_lobby(lobby *target);

int add_player(lobby *target, client_data *player);
int remove_player(lobby *target, client_data *player);
char *parse_lobby(lobby *target, int index);

#endif /* LOBBY_H */

