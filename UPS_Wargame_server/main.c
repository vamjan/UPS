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
#include <sys/socket.h>
#include <unistd.h>
#include "server.h"
#include "sini_log.h"
#include "lobby.h"

/**
 * Print server data into console. First clients with their ACTIVE/INACTIVE status.
 * Second lobbies with clients inside them and their status.
 * @param server
 */
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

/**
 * User help.
 * @param argc
 */
void help(int argc) {
    printf("Invalid arguments - %d\n", argc);
    printf("Run with two arguments. Number of clients and port.\n");
}

/*
 * Starting function.
 */
int main(int argc, char** argv) {

    char input;
    char *tmp;
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
    server.running = 1;
    server.clients = calloc(sizeof (client_data *), server.max_clients);
    server.lobbies = calloc(sizeof (lobby *), server.max_lobbies);

    if (pthread_create(&thread, NULL, start_server, &server)) {

        fprintf(stderr, "Error creating thread\n");
        return EXIT_FAILURE;
    }
    
    while (server.running) {
        scanf("%c", &input);

        switch (input) {
            case 'h':
                printf("Run with two arguments. Number of clients and port.\n");
                printf("Press 'q' to exit.\n");
                printf("Press 'w' to write server info.\n");
                break;
            case 'w':
                print_server_data(&server);
                break;
            case 'q':
                server.running = 0;
                shutdown(server.server_socket, SHUT_RDWR);
                break;
            default:
                break;
        }
    }
    
    pthread_join(thread, NULL);
    
    return (EXIT_SUCCESS);
}

