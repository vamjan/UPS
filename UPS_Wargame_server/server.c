/**
 * This module serves for most of the server functions such as: accept connection, drop connection, sync client and all client commanding
 * Jan Vampol
 * UPS
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <sys/ioctl.h>
#include <sys/select.h>
#include <netinet/in.h>
#include <pthread.h>

#include "server.h"
#include "lobby.h"

extern char *strdup(const char *s);

int server_socket;
server_data *server;

/*
 * Server thread function that accepts and disconnects clients. Manages incoming and leaving clients and distributes/kills their threads.
 * @param gets a server_data structure with all the parameters of server
 * @return nothing
 */
void *start_server(void *arg) {
    server = (server_data *) arg;

    int return_value;
    int i;

    struct sockaddr_in my_addr, peer_addr;
    
    pthread_mutex_init(&server->execution_lock, NULL);
    server->server_socket = socket(AF_INET, SOCK_STREAM, 0);

    memset(&my_addr, 0, sizeof (struct sockaddr_in));

    my_addr.sin_family = AF_INET;
    my_addr.sin_port = htons(server->port);
    my_addr.sin_addr.s_addr = INADDR_ANY;

    return_value = bind(server->server_socket, (struct sockaddr *) &my_addr, \
		sizeof (struct sockaddr_in));

    if (return_value == 0)
        printf("Bind - OK\n");
    else {
        printf("Bind - ERR\n");
        server->running = 0;
    }

    return_value = listen(server->server_socket, 5);
    if (return_value == 0) {
        printf("Listen - OK\n");
    } else {
        printf("Listen - ERR\n");
        server->running = 0;
    }

    printf("Server running for %d clients on port %d.\n", server->max_clients, server->port);

    //initialise server start

    while (server->running) {

        pthread_t client_thread;
        int client_socket, fd, len_addr;
        printf("Server waiting...\n");

        len_addr = sizeof (peer_addr);
        client_socket = accept(server->server_socket, (struct sockaddr *) &peer_addr, &len_addr);

        if (client_socket >= 0) {
            printf("New client connected and added to active socket: client socket = %d\n", client_socket);
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
            printf("Full.\n");
            if ((i = find_inactive_client(server->clients, server->max_clients)) >= 0) {
                destroy_client(&server->clients[i]);
                server->clients[i] = create_client(client_socket);
                pthread_create(&client_thread, NULL, start_client, &i);
                printf("Adding to list of sockets as %d\n", i);
            } else {
                //reject if client limit is reached
                printf("Client limit reached. Disconnecting.\n");
                write(client_socket, "SRY FAM\n", 9);
                close(client_socket);

                printf("Client %d left and was removed from active socket.\n", fd);
            }
        }
    }
    printf("Server thread ending\n");
    return 0;
}

/**
 * Create a new structure to store client information
 * @param fd file descriptor for communication with client
 * @return new client_data structure
 */
client_data *create_client(int fd) {
    client_data * retval = malloc(sizeof (client_data));

    retval->fd = fd;
    retval->message_buffer = create_buffer();
    retval->read = 0;
    retval->active = 1;

    return retval;
}

/**
 * Destroy client to clear memory
 * @param client
 */
void destroy_client(client_data** client) {
    free(*client);
    *client = NULL;
}

void remove_client(client_data *client, lobby *lobby, int lobby_index) {
    if (lobby->game_in_progress) { //game in progress
        if (lobby_is_empty(lobby)) { //both players are inactive
            remove_player(lobby, lobby->player_one);
            remove_player(lobby, lobby->player_two);
            lobby->game_in_progress = 0;
        } else { //somebody is waiting
            command *retval = create_command(client->id_key, DISCONNECT, 0, NULL);
            broadcast_lobby(retval, lobby);
            destroy_command(&retval);
        }
    } else { //game not in progress
        remove_player(lobby, client);
        lobby_update(client, lobby, lobby_index);
    }
}

/**
 * Client thread function. Periodically reads from client-data file descriptor and tries to parse the incoming
 * message into command. If this fails 5 times in a row, client is disconnected.
 * When the client is disconnected, interrupted or silent for 10 seconds, this thread is terminated.
 * @param arg index of client_data structure in server_data->clients
 * @return nothing
 */
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
    int wrong = 0;

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
            int read_bytes = (a2read > DROP) ? DROP : a2read; //read max DROP or less
            memset(&cbuf, 0, sizeof (char)*DROP); //might be redundant
            read(data->fd, &cbuf, read_bytes);
            add_to_buffer(data->message_buffer, cbuf, &data->read);

            while (strlen(data->message_buffer) > 0) {
                command *c = parse_input(data->message_buffer, &data->read);
                if (c) {
                    flush_buffer(data->message_buffer, data->read);
                    data->read = 0; //TODO: asi pridat do flush_buffer
                    //logger("INFO", );

                    //zpracuj zpravu
                    command *response = execute_command(c, data, &client_index, &lobby_index);
                    if (response) {
                        char *com_msg = parse_output(response);
                        write(data->fd, com_msg, strlen(com_msg));
                        free(com_msg);
                        destroy_command(&response);
                    }
                    wrong = 0;
                    destroy_command(&c);
                } else {
                    wrong++;
                    if (wrong > 5) running = 0;
                    break;
                }
            }
        } else { // na socketu se stalo neco spatneho
            char str[100];
            sprintf(str, "Client %d disconnected. FD %d is now free.", client_index, data->fd);
            logger("WARN", str);

            running = 0;
        }
    }

    server->active_clients--;
    data->active = 0;

    //remove client from lobbies and active games
    if (lobby_index >= 0) {
        remove_client(data, server->lobbies[lobby_index], lobby_index);
    }

    close(data->fd);
    
    if (data->id_key == 0) {
        destroy_client(&server->clients[client_index]);
        server->client_count--;
    }

    printf("Thread %d ending\n", client_index);
}

/**
 * Broadcast command to every client connected to this server.
 * @param c command to broadcast
 */
void broadcast(command *command) {
    int i = 0;
    char *com_msg = parse_output(command);
    for (i = 0; i < server->max_clients; i++) {
        if (server->clients[i] && server->clients[i]->active) {
            write(server->clients[i]->fd, com_msg, strlen(com_msg));
        }
    }
    free(com_msg);
}

/**
 * Broadcast command to every client connected to selected lobby
 * @param c command to broadcast
 * @param l selected lobby
 */
void broadcast_lobby(command *command, lobby *lobby) {
    char *com_msg = parse_output(command);
    if (lobby->player_one) write(lobby->player_one->fd, com_msg, strlen(com_msg));
    if (lobby->player_two) write(lobby->player_two->fd, com_msg, strlen(com_msg));
    free(com_msg);
}

/**
 * Find client in given array of clients by his ID. Used for reconnecting disconnected clients.
 * @param clients client array
 * @param id_key client id
 * @param max_clients count of clients
 * @return index of client on given ID
 */
int find_client_by_id(client_data * clients[], int id_key, int max_clients) {
    int i;
    /*if(id_key == 0) {
        return -1;
    }*/
    for (i = 0; i < max_clients; i++) {
        if (clients[i] && clients[i]->id_key == id_key)
            return i;
    }
    return -1;
}

/**
 * Search client array for inactive clients. Used to make place for new clients when the server is full.
 * @param clients client array
 * @param max_clients
 * @param max_clients count of clients
 * @return index of inactive client
 */
int find_inactive_client(client_data * clients[], int max_clients) {
    int i;
    for (i = 0; i < max_clients; i++) {
        if (clients[i] && !clients[i]->active)
            return i;
    }
    return -1;
}

void reconnect_to_lobby(server_data *server, client_data *client) {
    int i;
    for (i = 0; i < server->max_lobbies; i++) {
        printf("Looking for player %X\n", client->id_key);
        if (server->lobbies[i] && player_present(server->lobbies[i], client)) {
            printf("Found! Sending reconnect to %d!\n", client->fd);
            char **tmp = malloc(sizeof (char *));
            tmp[0] = calloc(sizeof (char), 4);
            snprintf(tmp[0], 4, "%d", i);
            command *command = create_command(client->id_key, RECONNECT, 1, tmp);
            char *com_msg = parse_output(command);
            write(client->fd, com_msg, strlen(com_msg));
            destroy_command(&command);
            break;
        }
    }
}

/**
 * Initialise lobby when client requests a new lobby. Finds a viable place and creates the lobby.
 * @param lobbies array of available lobbies
 * @param max_lobby count of lobbies
 * @param name of the new lobby
 * @return index of new lobby
 */
int init_lobby(lobby * lobbies[], int max_lobbies, char *name) {
    int retval = -1, i;

    for (i = 0; i < max_lobbies; i++) {
        if (!lobbies[i]) {
            lobbies[i] = create_lobby(name);

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

/**
 * Parse server data to be send as additional data to client.
 * @param lobbies array of available lobbies
 * @param max_lobby count of lobbies
 * @param active_lobby count of active lobbies
 * @return array of strings representing each lobby
 */
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

/**
 * Parse unit data to be send as additional data to client.
 * @param units array of units
 * @param count of unit
 * @return array of strings representing each unit
 */
char **parse_units(unit *units[], int count) {
    char **retval = malloc(sizeof (char *) * count);
    int i;

    for (i = 0; i < count; i++) {
        retval[i] = parse_unit(units[i]);
    }

    return retval;
}

/**
 * Parse game information to string to be send as additional information to client
 * @param l selected lobby
 * @param c selected client
 * @return array of strings representing game info
 */
char **parse_game_info(lobby *lobby) {
    char **retval = malloc(sizeof (char *) * 7);

    if (lobby->player_one) retval[0] = strdup(lobby->player_one->player_name);
    if (lobby->player_one) retval[1] = strdup(lobby->player_two->player_name);
    retval[2] = calloc(sizeof (char), 4);
    snprintf(retval[2], 4, "%d", lobby->pf->score_one);
    retval[3] = calloc(sizeof (char), 4);
    snprintf(retval[3], 4, "%d", lobby->pf->score_two);
    retval[4] = calloc(sizeof (char), 4);
    snprintf(retval[4], 4, "%d", lobby->pf->units[lobby->pf->on_turn % UNIT_ARRAY]->ID);
    retval[5] = calloc(sizeof (char), 2);
    retval[5][0] = (lobby->pf->attacking) ? 'T' : 'F';
    retval[6] = calloc(sizeof (char), 2); //left empty for allegiance in turn_update

    return retval;
}

/**
 * Parse move command information to string to be send as additional information to client
 * @param ID of unit
 * @param coordX of unit
 * @param coordZ of unit
 * @return array of strings as move info
 */
char **parse_move(int ID, int coordX, int coordZ) {
    char **retval = malloc(sizeof (char *) * 3);

    retval[0] = calloc(sizeof (char), 4);
    snprintf(retval[0], 4, "%d", ID);
    retval[1] = calloc(sizeof (char), 4);
    snprintf(retval[1], 4, "%d", coordX);
    retval[2] = calloc(sizeof (char), 4);
    snprintf(retval[2], 4, "%d", coordZ);

    return retval;
}

/**
 * Parse attack command information to string to be send as additional information to client
 * @param attacker unit
 * @param target unit
 * @return array of strings representing attack info
 */
char **parse_attack(unit *attacker, unit *target) {
    char **retval = malloc(sizeof (char *) * 3);

    retval[0] = calloc(sizeof (char), 4);
    snprintf(retval[0], 4, "%d", attacker->ID);
    retval[1] = calloc(sizeof (char), 4);
    snprintf(retval[1], 4, "%d", target->ID);
    retval[2] = calloc(sizeof (char), 4);
    snprintf(retval[2], 4, "%d", target->health);

    return retval;
}

/**
 * Parse capture command information to string to be send as additional information to client
 * @param capturer capturing unit
 * @param captured captured unit
 * @return array of strings representing capture info
 */
char **parse_capture(unit *capturer, unit *captured) {
    char **retval = malloc(sizeof (char *) * 2);

    retval[0] = calloc(sizeof (char), 4);
    snprintf(retval[0], 4, "%d", capturer->ID);
    retval[1] = calloc(sizeof (char), 2);
    snprintf(retval[1], 4, "%d", captured->ID);

    return retval;
}

/**
 * Send update to all clients in lobby. Each client receives different message,
 * according to its assigned player.
 * @param c output command
 * @param l selected lobby
 */
void turn_update(command *command, lobby *lobby) {
    command->data[6][0] = BLU;
    char *com_msg = parse_output(command);
    if (lobby->player_one)
        write(lobby->player_one->fd, com_msg, strlen(com_msg));
    free(com_msg);

    command->data[6][0] = RED;
    com_msg = parse_output(command);
    if (lobby->player_two)
        write(lobby->player_two->fd, com_msg, strlen(com_msg));
    free(com_msg);
}

/**
 * Send lobby information to all clients in lobby.
 * @param client
 * @param client_lobby
 * @param index
 */
void lobby_update(client_data *client, lobby *client_lobby, int index) {
    char **tmp = malloc(sizeof (char *));
    tmp[0] = parse_lobby(client_lobby, index);
    command *command = create_command(client->id_key, JOIN_LOBBY, 1, tmp); //used to update lobby information
    broadcast_lobby(command, client_lobby);
    destroy_command(&command);
}

/**
 * Send server information to selected client
 * @param server
 * @param client
 */
void server_update(server_data *server, client_data *client) {
    char **tmp = parse_server_data(server->lobbies, server->max_lobbies, server->active_lobbies);
    command *command = create_command(client->id_key, GET_SERVER, server->active_lobbies, tmp);
    char *com_msg = parse_output(command);
    write(client->fd, com_msg, strlen(com_msg));
    destroy_command(&command);
    free(com_msg);
}

/**
 * Send game update to both players in lobby. Each of the players gets message with their allegiance.
 * @param client
 * @param client_lobby
 */
void game_update(client_data *client, lobby *client_lobby) {
    char **tmp = parse_game_info(client_lobby);
    command *command = create_command(client->id_key, UPDATE, 7, tmp);
    turn_update(command, client_lobby);
    destroy_command(&command);
}

/**
 * Send game end notification to all players in lobby.
 * @param client
 * @param client_lobby
 */
void game_end(client_data *client, lobby *client_lobby) {
    client_lobby->game_in_progress = 0;
    char **tmp = malloc(sizeof (char *));
    tmp[0] = (client_lobby->pf->score_one > client_lobby->pf->score_two)
            ? strdup(client_lobby->player_one->player_name)
            : (client_lobby->pf->score_one == client_lobby->pf->score_two)
            ? strdup("DRAW")
            : strdup(client_lobby->player_two->player_name);
    command *command = create_command(client->id_key, END, 1, tmp);
    broadcast_lobby(command, client_lobby);
    destroy_command(&command);
    kick_inactive(client_lobby);
}

/**
 * Notify both players to start the game.
 * @param client
 * @param lobby
 */
void notify_start(client_data *client, lobby *lobby) {
    //send playfield
    char **tmp = parse_map(lobby->pf);
    command *command = create_command(client->id_key, START, lobby->pf->rows, tmp);
    char *com_msg = parse_output(command);
    write(client->fd, com_msg, strlen(com_msg));
    destroy_command(&command);
    free(com_msg);
    //send units
    tmp = parse_units(lobby->pf->units, UNIT_ARRAY); //number of units
    command = create_command(client->id_key, UNITS, UNIT_ARRAY, tmp);
    com_msg = parse_output(command);
    write(client->fd, com_msg, strlen(com_msg));
    destroy_command(&command);
    free(com_msg);
}

/**
 * Function to excecute commands from clients. Each message has its own command type which determines what kind of command is it.
 * The command is then executed according to this type. Function returns a command which will be executed as server response.
 * Server can send more messages inside the command execution according to the type of command.
 * 
 * This function also handles synchronizing clients with the server. It is the reason for *client_index and *lobby_index.
 * Those variables are stored in client thread and can be changed in this function (for types CONNECT, RECONNECT and JOIN_LOBBY).
 * 
 * @param c
 * @param client
 * @param client_index
 * @param lobby_index
 * @return 
 */
command *execute_command(command *c, client_data *client, int *client_index, int *lobby_index) {
    command *retval = NULL;
    char **tmp;
    int index, coordX, coordZ;
    unit *curr;
    
    pthread_mutex_lock(&server->execution_lock);
    //if (c->type != POKE) printf("Client ID %X - lobby %d - Client index %d - fd %d\n", client->id_key, *lobby_index, *client_index, client->fd);

    if (client->id_key == 0) { //client not authorised, needs synchronization
        if (c->type == CONNECT) {
            if ((index = find_client_by_id(server->clients, c->id_key, server->max_clients)) >= 0) { //user is present
                if (server->clients[index]->active) { //id is taken
                    retval = create_command(client->id_key, NACK, 0, NULL);
                } else { //id is present but user inactive
                    printf("Client found on index %d. Reconnecting...\n", index);
                    server->clients[*client_index]->active = 0;
                    server->clients[index]->fd = server->clients[*client_index]->fd;
                    server->clients[index]->active = 1;
                    destroy_client(&server->clients[*client_index]);
                    server->client_count--;
                    *client_index = index;
                    server_update(server, server->clients[*client_index]);
                    reconnect_to_lobby(server, server->clients[*client_index]);
                    retval = create_command(server->clients[*client_index]->id_key, ACK, 0, NULL);
                    client->fd = server->clients[index]->fd;
                }
            } else { //new user
                client->id_key = c->id_key;
                memset(client->player_name, 0, sizeof (char) * NAME_LENGTH);
                strncpy(client->player_name, c->data[0], strlen(c->data[0])); //only here to enforce matching names
                server_update(server, server->clients[*client_index]);
                retval = create_command(client->id_key, ACK, 0, NULL);
            }
        } else { //client unauthorised and sending wrong messages
            retval = create_command(client->id_key, NACK, 0, NULL);
            printf("Unauthorised user\n");
        }
    } else { //user authorised
        if (*lobby_index >= 0) { //client in lobby
            lobby *client_lobby = server->lobbies[*lobby_index];
            switch (c->type) {
                case(LEAVE_LOBBY): //client wants to leave lobby he joined previously
                    if (client_lobby && remove_player(client_lobby, client)) {
                        lobby_update(client, client_lobby, *lobby_index);
                        *lobby_index = -1;
                        retval = create_command(client->id_key, ACK, 0, NULL);
                    } else {
                        retval = create_command(client->id_key, NACK, 0, NULL);
                    }
                    break;
                case(TOGGLE_READY): //client wants to toggle his ready status
                    if (toggle_ready(client_lobby, client)) {
                        lobby_update(client, client_lobby, *lobby_index);

                        //start game procedure
                        if (check_ready(client_lobby)) {
                            client_lobby->ready_one = 0;
                            client_lobby->ready_two = 0;
                            client_lobby->game_in_progress = 1;
                            //send map layout
                            reset_lobby(client_lobby);
                            create_hex_map(client_lobby->pf);
                            notify_start(client_lobby->player_one, client_lobby);
                            notify_start(client_lobby->player_two, client_lobby);
                            //set player info
                            game_update(client, client_lobby);
                        }
                    }
                    break;
                case(END): //client wants to finish the game
                    game_end(client, client_lobby);
                    break;
                case(MOVE): //client wants to move unit given by ID data[0] to coordinates given by data[1] and data[2]
                    index = (int) strtol(c->data[0], NULL, 10); //TODO: check
                    coordX = (int) strtol(c->data[1], NULL, 10); //TODO: check
                    coordZ = (int) strtol(c->data[2], NULL, 10); //TODO: check
                    curr = get_unit(client_lobby->pf, index);
                    printf("moving unit %d from %d-%d to %d-%d\n", curr->ID, curr->coord_x, curr->coord_z, coordX, coordZ);
                    if (curr && get_unit_on_coords(client_lobby->pf, coordX, coordZ) == NULL && move_unit(curr, coordX, coordZ)) { //unit can be moved
                        client_lobby->pf->attacking = 1;
                        tmp = parse_move(curr->ID, coordX, coordZ);
                        retval = create_command(client->id_key, MOVE, 3, tmp);
                        broadcast_lobby(retval, client_lobby);
                        destroy_command(&retval);
                        retval = create_command(client->id_key, ACK, 0, NULL);
                    } else { //unit cant be moved
                        retval = create_command(client->id_key, NACK, 0, NULL);
                    }
                    break;
                case(ATTACK): //client wants unit given by ID data[0] to attack unit by ID[1]
                    index = (int) strtol(c->data[0], NULL, 10); //TODO: check   //attacker
                    coordX = (int) strtol(c->data[1], NULL, 10); //TODO: check  //attacked
                    printf("unit %d attacked unit %d\n", get_unit(client_lobby->pf, index)->ID, get_unit(client_lobby->pf, coordX)->ID);
                    if ((curr = get_unit(client_lobby->pf, index)) != NULL && (curr = get_unit(client_lobby->pf, coordX)) != NULL && attack_unit(get_unit(client_lobby->pf, index), curr)) {
                        tmp = parse_attack(get_unit(client_lobby->pf, index), curr);
                        retval = create_command(client->id_key, ATTACK, 3, tmp);
                        broadcast_lobby(retval, client_lobby);
                        destroy_command(&retval);
                        retval = create_command(client->id_key, ACK, 0, NULL);
                    } else {
                        retval = create_command(client->id_key, NACK, 0, NULL);
                    }
                    break;
                case(CAPTURE): //client wants unit given by ID data[0] to capture unit by ID[1]
                    index = (int) strtol(c->data[0], NULL, 10); //TODO: check //
                    coordX = (int) strtol(c->data[1], NULL, 10); //TODO: check //captured
                    printf("unit %d captured unit %d\n", get_unit(client_lobby->pf, index)->ID, get_unit(client_lobby->pf, coordX)->ID);
                    if ((curr = get_unit(client_lobby->pf, index)) != NULL && (curr = get_unit(client_lobby->pf, coordX)) != NULL) {
                        change_allegiance(curr, get_unit(client_lobby->pf, index)->al);
                        tmp = parse_capture(get_unit(client_lobby->pf, index), curr);
                        retval = create_command(client->id_key, CAPTURE, 2, tmp);
                        broadcast_lobby(retval, client_lobby);
                        destroy_command(&retval);
                        retval = create_command(client->id_key, ACK, 0, NULL);
                    } else {
                        retval = create_command(client->id_key, NACK, 0, NULL);
                    }
                    break;
                case(SKIP): //client wants to skip his turn and proceed to next turn
                    if ((curr = next_turn(client_lobby->pf)) != NULL) {
                        printf("turn %d: unit %d\n", client_lobby->pf->on_turn, curr->ID);
                        client_lobby->pf->attacking = 0;
                        game_update(client, client_lobby);
                    } else {
                        game_end(client, client_lobby);
                    }
                    break;
            }
        } else { //user not in lobby
            switch (c->type) {
                case(CREATE_LOBBY): //client wants to create lobby on server
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
                case(JOIN_LOBBY): //client wants to join lobby on index data[0]
                    index = (int) strtol(c->data[0], NULL, 10); //TODO: check
                    if (server->lobbies[index] && add_player(server->lobbies[index], client)) {
                        *lobby_index = index;
                        lobby_update(client, server->lobbies[*lobby_index], *lobby_index);
                        retval = create_command(client->id_key, ACK, 0, NULL);
                    } else {
                        retval = create_command(client->id_key, NACK, 0, NULL);
                    }
                    break;
                case(RECONNECT): //client wants to reconnect to lobby on index data[0]
                    index = (int) strtol(c->data[0], NULL, 10); //TODO: check
                    notify_start(client, server->lobbies[index]);
                    game_update(client, server->lobbies[index]);
                    *lobby_index = index;
                    break;
            }
        }
    }

    switch (c->type) {
        case(MESSAGE):
            retval = create_command(client->id_key, ACK, 0, NULL);
            break;
        case(GET_SERVER): //might be redundant
            tmp = parse_server_data(server->lobbies, server->max_lobbies, server->active_lobbies);
            retval = create_command(client->id_key, GET_SERVER, server->active_lobbies, tmp);
            break;
        case(POKE):
            retval = create_command(client->id_key, POKE, 0, NULL);
            break;
        default:
            //printf("Unknown command\n");
            break;
    }
    
    pthread_mutex_unlock(&server->execution_lock);

    return retval;
}