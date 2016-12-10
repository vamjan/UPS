/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   net_interface.h
 * Author: sini
 *
 * Created on 29 October 2016, 21:58
 */

#ifndef NET_INTERFACE_H
#define NET_INTERFACE_H

#include "net_interface.h"
#include "parser.h"
#include "hex.h"
#include "sini_log.h"

#define NAME_LENGTH 30

typedef struct { 
    int fd;
    int id_key;
    char player_name[NAME_LENGTH];
    char *message_buffer;
    int read;
    int active;
} client_data;

typedef struct {
    client_data *player_one;
    client_data *player_two;
    client_data *on_turn;
    int game_in_progress;
    int running;
    playfield *pf;
    char lobby_name[NAME_LENGTH];
} lobby;

typedef struct {
    int max_clients;
    int max_lobbies;
    int active_clients;
    int active_lobbies;
    client_data **clients;
    lobby **lobbies;
} server_data;

static client_data unoc_client = {0};
static lobby unoc_lobby = {0};

void *start_server(void *arg);
client_data *create_client(int fd);
void destroy_client(client_data **client);
void *start_client(void *arg);
int find_client_by_fd(client_data *clients[], int fd, int max_clients);
int init_lobby(lobby *lobbies[], int max_lobbies, char *name);
command *execute_command(command *c, client_data *client, server_data *server);
int send_command(command *c);

#endif /* NET_INTERFACE_H */

