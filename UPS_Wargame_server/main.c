/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   main.c
 * Author: Sini
 *
 * Created on 1. října 2016, 17:46
 */

#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <string.h>
#include "server.h"
#include "sini_log.h"
#include "lobby.h"

void print_server_data(server_data *server) {
    int i;
    printf("Client status:\n");
    for (i = 0; i < server->max_clients; i++) {
        if (server->clients[i])
            printf("Client: %d\t%d\t%08X\t%s\n", i, server->clients[i]->fd, server->clients[i]->id_key, (server->clients[i]->active == 1) ? "ACTIVE" : "INACTIVE");
        else
            printf("Client: %d\tEMPTY\n", i);
    }
    printf("Lobby status:\n");
    for (i = 0; i < server->max_lobbies; i++) {
        if (server->lobbies[i])
            printf("Lobby: %d\t%08X\t%08X\t%s\n", i, 
                    (server->lobbies[i]->player_one == NULL) ? 0 : server->lobbies[i]->player_one->id_key,
                    (server->lobbies[i]->player_two == NULL) ? 0 : server->lobbies[i]->player_two->id_key,
                    (server->lobbies[i]->game_in_progress == 1) ? "PLAYING" : "IDLE");
        else
            printf("Lobby: %d\tEMPTY\n", i);
    }
}

void help(int argc) {
    printf("Invalid arguments - %d\n", argc);
    printf("Run with two arguments. Number of clients and port.\n");
}
/*
 * 
 */
int main(int argc, char** argv) {

    char input;
    char *tmp;
    int run = 1, max_clients = 12;
    pthread_t thread;   
    server_data server;
    if(argc == 3) {
        server.max_clients = strtol(argv[1], &tmp, 10);
        if(strcmp(tmp, "")) {
            help(argc);
            return EXIT_FAILURE;
        }
        server.port = strtol(argv[2], &tmp, 10);
        if(strcmp(tmp, "") || (server.port < 0 && server.port > 65535)) {
            help(argc);
            return EXIT_FAILURE;
        }
    } else {
        help(argc);
        return EXIT_FAILURE;
    }
    
    server.max_lobbies = server.max_clients / 2;
    server.client_count = 0;
    server.active_clients = 0;
    server.active_lobbies = 0;
    server.clients = calloc(sizeof (client_data *), server.max_clients);
    server.lobbies = calloc(sizeof (lobby *), server.max_lobbies);

    if (pthread_create(&thread, NULL, start_server, &server)) {

        fprintf(stderr, "Error creating thread\n");
        return EXIT_FAILURE;
    }
    
    /*playfield pf = *create_playfield(10, 15);
    create_hex_map(&pf);
    print_playfield(&pf);
    
    run = 0;*/

    while (run) {
        scanf("%c", &input);

        switch (input) {
            case 'h':
                printf("Run with two arguments. Number of clients and port.");
                printf("Press 'q' to exit.\n");
                printf("Press 'w' to write server info.\n");
                break;
            case 'w':
                print_server_data(&server);
                break;
            case 'q':
                run = 0;
                break;
            default:
                break;
        }
    }
    
    return (EXIT_SUCCESS);
}

