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
#include <pthread.h>

#include "server.h"
#include "lobby.h"

extern char *strdup(const char *s);

int server_socket;
server_data *server;

void *start_server(void *arg) {
    server = (server_data *) arg;

    int return_value;
    int i, running = 1;

    struct sockaddr_in my_addr, peer_addr;

    server_socket = socket(AF_INET, SOCK_STREAM, 0);

    memset(&my_addr, 0, sizeof (struct sockaddr_in));

    my_addr.sin_family = AF_INET;
    my_addr.sin_port = htons(60001);
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
        printf("Listen - ERR\n");
        return -1;
    }

    //inicializace pocatku serveru

    while (running) {

        pthread_t client_thread;
        int client_socket, fd, len_addr;
        printf("Server waiting...\n");

        len_addr = sizeof (peer_addr);
        client_socket = accept(server_socket, (struct sockaddr *) &peer_addr, &len_addr);

        if (client_socket >= 0) {
            printf("Pripojen novy klient a pridan do sady socketu: client socket = %d\n", client_socket);
        } else {
            printf("Accept - Error\n");
            break; //asi zbytecne
        }

        if (server->client_count < server->max_clients) {
            server->active_clients++;
            server->client_count++;
            for (i = 0; i < server->max_clients; i++) {
                //if position is empty
                if (!server->clients[i]) {
                    server->clients[i] = create_client(client_socket);
                    pthread_create(&client_thread, NULL, start_client, &i);
                    printf("Adding to list of sockets as %d\n", i);
                    break;
                }
            }
        } else {
            printf("Je plno.\n");
            if ((i = find_inactive_client(server->clients, server->max_clients)) >= 0) {
                destroy_client(&server->clients[i]);
                server->clients[i] = create_client(client_socket);
                pthread_create(&client_thread, NULL, start_client, &i);
                printf("Adding to list of sockets as %d\n", i);
            } else {
                //reject if client limit is reached
                printf("Limit clientu dosazen. Odpojuji.\n");
                //TODO: send NACK
                write(client_socket, "SRY FAM\n", 9);
                close(client_socket);

                printf("Klient %d se odpojil a byl odebran ze sady socketu\n", fd);
            }
        }
    }
    printf("Server thread ending\n");
    return 0;
}

client_data * create_client(int fd) {
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

void *start_client(void *arg) {
    // je to klientsky socket ? prijmem data
    // pocet bajtu co je pripraveno ke cteni
    int client_index = *(int *) arg;
    int a2read = 0, running = 1, return_value;
    client_data *data = server->clients[client_index];
    int lobby_index = -1;
    lobby *game_lobby = NULL;
    fd_set client_socks;
    struct timeval timeout;
    char cbuf[DROP];

    FD_ZERO(&client_socks);
    FD_SET(data->fd, &client_socks);

    while (running) {
        data = server->clients[client_index];
        timeout.tv_sec = 10;
        timeout.tv_usec = 0;

        return_value = select(data->fd + 1, &client_socks, NULL, NULL, &timeout);
        if (return_value == -1) {
            printf("Select - Error\n"); //error 
            break;
        } else if (return_value == 0) {
            printf("Select - Timeout\n"); //timeout
            break;
        } //no problem... time to read!
        ioctl(data->fd, FIONREAD, &a2read);
        // mame co cist
        if (a2read > 0) {
            int read_bytes = a2read > DROP ? DROP : a2read; //read max DROP or less
            memset(&cbuf, 0, sizeof (char)*DROP); //might be redundant
            read(data->fd, &cbuf, read_bytes);
            add_to_buffer(data->message_buffer, cbuf, &data->read);

            command *c = parse_input(data->message_buffer, &data->read);
            if (c) {
                flush_buffer(data->message_buffer, data->read);
                data->read = 0; //TODO: asi pridat do flush_buffer
                //logger("INFO", );

                //zpracuj zpravu
                command *response = execute_command(c, data, &client_index, &lobby_index);
                if (response) {
                    char *com_msg = parse_output(response);
                    //printf("Posilam %s", com_msg);
                    write(data->fd, com_msg, strlen(com_msg));
                    free(com_msg);
                    destroy_command(&response);
                }
                destroy_command(&c);
            } else {
                //printf("Chyba prekladu zpravy\n");
            }
        } else { // na socketu se stalo neco spatneho
            char str[100];
            sprintf(str, "Client %d disconnected. FD %d is now free.", client_index, data->fd);
            logger("WARN", str);

            break;
        }
    }
    /*command *retval = create_command(client->id_key, JOIN_LOBBY, 1, tmp); //used to update lobby information
    broadcast_lobby(retval, server->lobbies[*lobby_index]);
    destroy_command(&retval);*/
    if(lobby_index >= 0) remove_player(server->lobbies[lobby_index], server->clients[client_index]);

    close(data->fd);

    server->active_clients--;
    server->clients[client_index]->active = 0;

    printf("Thread %d ending\n", client_index);
}

void broadcast(command *c) {
    int i = 0;
    char *com_msg = parse_output(c);
    for (i = 0; i < server->max_clients; i++) {
        if (server->clients[i] && server->clients[i]->active) {
            write(server->clients[i]->fd, com_msg, strlen(com_msg));
        }
    }
    free(com_msg);
}

void broadcast_lobby(command *c, lobby *l) {
    int i = 0;
    char *com_msg = parse_output(c);
    if (l->player_one) write(l->player_one->fd, com_msg, strlen(com_msg));
    if (l->player_two) write(l->player_two->fd, com_msg, strlen(com_msg));
    free(com_msg);
}

int find_client_by_id(client_data * clients[], int id_key, int max_clients) {
    int i;
    for (i = 0; i < max_clients; i++) {
        if (clients[i] && clients[i]->id_key == id_key)
            return i;
    }
    return -1;
}

int find_inactive_client(client_data * clients[], int max_clients) {
    int i;
    for (i = 0; i < max_clients; i++) {

        if (clients[i] && !clients[i]->active)
            return i;
    }
    return -1;
}

char **parse_server_data(lobby * lobbies[], int max_lobby, int active_lobby) {
    char **retval = malloc(sizeof (char *) * active_lobby);

    int i, j = 0;

    for (i = 0; i < max_lobby; i++) {
        if (lobbies[i]) {
            retval[j] = parse_lobby(lobbies[i], j);
            j++;
        }
    }

    return retval;
}

int init_lobby(lobby * lobbies[], int max_lobbies, char *name) {
    int retval = -1, i;

    for (i = 0; i < max_lobbies; i++) {
        if (!lobbies[i]) {
            lobbies[i] = create_lobby(name);
            lobbies[i]->running = 1;

            printf("Creating lobby on position %d\n", i);

            retval = i;
            break;
        }
        //if() {
        //TODO: clear server of empty lobbies
        //}
    }

    return retval;
}

command *execute_command(command *c, client_data *client, int *client_index, int *lobby_index) { //vraci response
    command *retval = NULL;
    char **tmp;
    int index;

    switch (c->type) {
        case(ACK):
            break;
        case(NACK):
            break;
        case(MESSAGE):
            retval = create_command(client->id_key, ACK, 0, NULL);
            break;
        case(CONNECT):
            if ((index = find_client_by_id(server->clients, c->id_key, server->max_clients)) >= 0) {
                printf("Client found on index %d. Reconnecting...\n", index);
                server->clients[*client_index]->active = 0;
                server->clients[index]->fd = server->clients[*client_index]->fd;
                server->clients[index]->active = 1;
                destroy_client(&server->clients[*client_index]);
                *client_index = index;
            } else {
                client->id_key = c->id_key;
                strncpy(client->player_name, c->data[0], sizeof (c->data[0])); //only here to enforce matching names
            }
            retval = create_ack(client->id_key, 1);
            break;
        case(GET_SERVER): //might be redundant
            tmp = parse_server_data(server->lobbies, server->max_lobbies, server->active_lobbies);
            retval = create_command(client->id_key, GET_SERVER, server->active_lobbies, tmp);
            break;
        case(CREATE_LOBBY):
            if ((index = init_lobby(server->lobbies, server->max_lobbies, c->data[0])) >= 0) {
                server->active_lobbies++;
                tmp = parse_server_data(server->lobbies, server->max_lobbies, server->active_lobbies);
                retval = create_command(client->id_key, GET_SERVER, server->active_lobbies, tmp);
                broadcast(retval);
                destroy_command(&retval);
                retval = create_command(client->id_key, ACK, 0, NULL);
            } else {
                retval = create_command(client->id_key, NACK, 0, NULL);
            }
            break;
        case(JOIN_LOBBY):
            index = (int) strtol(c->data[0], NULL, 10);
            *lobby_index = index;
            if (server->lobbies[*lobby_index]) {
                if (add_player(server->lobbies[*lobby_index], client)) {
                    tmp = malloc(sizeof (char *));
                    tmp[0] = parse_lobby(server->lobbies[*lobby_index], *lobby_index);
                    retval = create_command(client->id_key, JOIN_LOBBY, 1, tmp);
                    broadcast_lobby(retval, server->lobbies[*lobby_index]);
                    destroy_command(&retval);
                    retval = create_command(client->id_key, ACK, 0, NULL);
                } else {
                    retval = create_command(client->id_key, NACK, 0, NULL);
                }
            } else {
                *lobby_index = -1;
                retval = create_command(client->id_key, NACK, 0, NULL);
            }
            break;
        case(LEAVE_LOBBY):
            if (server->lobbies[*lobby_index] && remove_player(server->lobbies[*lobby_index], client)) {
                tmp = malloc(sizeof (char *));
                tmp[0] = parse_lobby(server->lobbies[*lobby_index], *lobby_index);
                retval = create_command(client->id_key, JOIN_LOBBY, 1, tmp); //used to update lobby information
                broadcast_lobby(retval, server->lobbies[*lobby_index]);
                destroy_command(&retval);
                *lobby_index = -1;
                retval = create_command(client->id_key, ACK, 0, NULL);
            } else {
                retval = create_command(client->id_key, NACK, 0, NULL);
            }
            break;
        case(TOGGLE_READY):
            if(toggle_ready(server->lobbies[*lobby_index], client)) {
                tmp = malloc(sizeof (char *));
                tmp[0] = parse_lobby(server->lobbies[*lobby_index], *lobby_index);
                retval = create_command(client->id_key, JOIN_LOBBY, 1, tmp); //used to update lobby information
                broadcast_lobby(retval, server->lobbies[*lobby_index]);
                destroy_command(&retval);
                
                if(check_ready(server->lobbies[*lobby_index]))
                    printf("BOTH PLAYERS ARE READY\n");
            }
            break;
        case(POKE):
            retval = create_command(client->id_key, POKE, 0, NULL);
            break;
    }

    return retval;
}