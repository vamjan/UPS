/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdlib.h>
#include <sys/ioctl.h>
#include <sys/select.h>
#include <string.h>

#include "server.h"
#include "lobby.h"

extern char *strdup(const char *s);
char **parse_server_data(lobby *lobbies[], int max_lobby, int active_lobby);

void *start_server(void *arg) {
    int server_socket;
    int client_socket, fd;
    int return_value;
    server_data server;
    server.max_clients = *(int *) arg;
    server.max_lobbies = server.max_clients / 2;
    server.active_clients = 0;
    server.active_lobbies = 0;
    server.clients = malloc(sizeof(client_data *) * server.max_clients);
    server.lobbies = malloc(sizeof(lobby *) * server.max_lobbies);
    int i, running = 1;
    char cbuf[DROP];

    int len_addr;
    int a2read;
    struct sockaddr_in my_addr, peer_addr;
    fd_set client_socks, tests;

    server_socket = socket(AF_INET, SOCK_STREAM, 0);

    memset(&my_addr, 0, sizeof (struct sockaddr_in));

    my_addr.sin_family = AF_INET;
    my_addr.sin_port = htons(10002);
    my_addr.sin_addr.s_addr = INADDR_ANY;

    return_value = bind(server_socket, (struct sockaddr *) &my_addr, \
		sizeof (struct sockaddr_in));

    if (return_value == 0)
        printf("Bind - OK\n");
    else {
        printf("Bind - ERR\n");
        return -1;
    }

    return_value = listen(server_socket, 5);
    if (return_value == 0) {
        printf("Listen - OK\n");
    } else {
        printf("Listen - ER\n");
    }

    // vyprazdnime sadu deskriptoru a vlozime server socket
    FD_ZERO(&client_socks);
    FD_SET(server_socket, &client_socks);

    //inicializace pocatku serveru

    while (running) {

        tests = client_socks;
        // sada deskriptoru je po kazdem volani select prepsana sadou deskriptoru kde se neco delo
        return_value = select(FD_SETSIZE, &tests, (fd_set *) 0, (fd_set *) 0, (struct timeval *) 0);

        printf("and they don't stop commin'\n");

        if (return_value < 0) {
            printf("Select - ERR\n");
            return -1;
        }
        // vynechavame stdin, stdout, stderr
        for (fd = 3; fd < FD_SETSIZE; fd++) {
            // je dany socket v sade fd ze kterych lze cist ?
            if (FD_ISSET(fd, &tests)) {
                // je to server socket ? prijmeme nove spojeni
                if (fd == server_socket) {
                    client_socket = accept(server_socket, (struct sockaddr *) &peer_addr, &len_addr);
                    FD_SET(client_socket, &client_socks);
                    printf("Pripojen novy klient a pridan do sady socketu: client socket = %d\n", client_socket);
                    if (server.active_clients < server.max_clients) {
                        server.active_clients++;
                        for (i = 0; i < server.max_clients; i++) {
                            //if position is empty
                            if (!server.clients[i]) {
                                server.clients[i] = create_client(client_socket);
                                printf("Adding to list of sockets as %d\n", i);

                                break;
                            }
                        }
                        command *c = create_command(0, ACK, 0, NULL);
                        char *com_msg = strdup(parse_output(c));
                        write(client_socket, com_msg, strlen(com_msg));
                        free(com_msg);
                        free(c);
                    } else {
                        //reject if client limit is reached
                        printf("Limit clientu dosazen. Odpojuji.\n");

                        write(client_socket, "SRY FAM\n", 9);

                        close(client_socket);
                        FD_CLR(client_socket, &client_socks);

                        printf("Klient %d se odpojil a byl odebran ze sady socketu\n", fd);
                    }

                } else { // je to klientsky socket ? prijmem data
                    // pocet bajtu co je pripraveno ke cteni
                    ioctl(fd, FIONREAD, &a2read);
                    // mame co cist
                    // souhlasi fd?
                    int client_index = find_client_by_fd(server.clients, fd, server.max_clients);
                    if (client_index == -1) {
                        printf("No client found\n"); //error 
                        continue;
                    }

                    if (a2read > 0) {

                        memset(&cbuf, 0, sizeof (char)*DROP); //might be redundant
                        read(fd, &cbuf, a2read);
                        add_to_buffer(server.clients[client_index]->message_buffer, cbuf, &server.clients[client_index]->read);

                        command *c = parse_input(server.clients[client_index]->message_buffer, &server.clients[client_index]->read);
                        if (c) {
                            flush_buffer(server.clients[client_index]->message_buffer, server.clients[client_index]->read);
                            server.clients[client_index]->read = 0; //TODO: asi pridat do flush_buffer
                            //logger("INFO", );

                            //zpracuj zpravu
                            command *response = execute_command(c, server.clients[client_index], &server);
                            if (response) {
                                char *com_msg = parse_output(response);
                                printf("Posilam %s", com_msg);
                                write(client_socket, com_msg, strlen(com_msg));
                                free(com_msg);
                                destroy_command(&response);
                            }
                            destroy_command(&c);
                        } else {
                            printf("Chyba prekladu zpravy\n");
                        }
                        //write(fd, cbuf, strlen(cbuf));
                        //broadcast(cbuf, client_socks, max_clients, clients);
                    } else { // na socketu se stalo neco spatneho
                        close(fd);
                        FD_CLR(fd, &client_socks);

                        server.active_clients--;
                        server.clients[client_index]->active = 0;

                        printf("Klient %d se odpojil a byl odebran ze sady socketu\n", fd);
                    }
                }
            }
        }

    }

    return 0;
}

client_data *create_client(int fd) {
    client_data * retval = malloc(sizeof (client_data));

    retval->fd = fd;
    retval->message_buffer = create_buffer();
    retval->read = 0;
    retval->active = 1;

    return retval;
}

void destroy_client(client_data** client) {
    free(*client);
    *client = NULL;
}

void broadcast(char msg[], fd_set clients, int max, int clientss[]) {
    int i, tmp;
    for (i = 0; i < max; i++) {
        tmp = clientss[i];
        if (FD_ISSET(tmp, &clients)) {
            printf("Posilam zpravu %s na %d\n", msg, tmp);
            write(tmp, msg, strlen(msg));
        }
    }
}

int find_client_by_fd(client_data *clients[], int fd, int max_clients) {
    int i;
    for (i = 0; i < max_clients; i++) {
        if (clients[i] && clients[i]->fd == fd)
            return i;
    }
    return -1;
}

char **parse_server_data(lobby *lobbies[], int max_lobby, int active_lobby) {
    char **retval = malloc(sizeof(char *) * active_lobby);
    
    int i, j = 0;
    
    for(i = 0; i < max_lobby; i++) {
        if(lobbies[i]) {
            retval[j++] = parse_lobby(lobbies[i], j);
        }
    }
    
    return retval;
}

command *execute_command(command *c, client_data *client, server_data *server) { //vraci response
    command *retval = NULL;
    char **tmp;
    
    switch (c->type) {
        case(ACK):
            break;
        case(NACK):
            break;
        case(MESSAGE):
            retval = create_command(client->id_key, ACK, 0, NULL);
            break;
        case(CONNECT):
            client->id_key = c->id_key;
            retval = create_ack(client->id_key, 0);
            break;
        case(GET_SERVER):
            tmp = parse_server_data(server->lobbies, server->max_lobbies, server->active_lobbies);
            retval = create_command(client->id_key, GET_SERVER, server->active_lobbies * 5, tmp);
            break;
    }

    return retval;
}

int send_command(command *c) {

}