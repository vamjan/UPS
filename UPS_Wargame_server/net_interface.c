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
#include "net_interface.h"

void server_stuff(void *arg) {
    int server_socket;
    int client_socket, fd;
    int return_value;
    int max_clients = 2;
    int active_clients = 0;
    int clients[10] = {0, 0}; //, 0, 0, 0, 0, 0, 0, 0, 0};
    int i;
    char cbuf[100];
    int len_addr;
    int a2read;
    struct sockaddr_in my_addr, peer_addr;
    fd_set client_socks, tests;

    server_socket = socket(AF_INET, SOCK_STREAM, 0);

    memset(&my_addr, 0, sizeof (struct sockaddr_in));

    my_addr.sin_family = AF_INET;
    my_addr.sin_port = htons(10001);
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

    for (;;) {

        tests = client_socks;
        // sada deskriptoru je po kazdem volani select prepsana sadou deskriptoru kde se neco delo
        return_value = select(FD_SETSIZE, &tests, (fd_set *) 0, (fd_set *) 0, (struct timeval *) 0);

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
                    //printf("DATA %us %s", (unsigned short)peer_addr.sin_port, inet_ntoa(peer_addr.sin_addr.s_addr));
                    if (active_clients < max_clients) {
                        active_clients++;
                        for (i = 0; i < max_clients; i++) {
                            //if position is empty
                            if (clients[i] == 0) {
                                clients[i] = client_socket;
                                printf("Adding to list of sockets as %d\n", i);

                                break;
                            }
                        }
                        write(client_socket, "Who are you?\n", 14);
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
                    if (a2read > 0) {
                        memset(&cbuf, 0, sizeof (char)*100);
                        read(fd, &cbuf, a2read);
                        printf("Prijato %s %d\n", cbuf, a2read);
                        /*read(fd, &cbuf, 1);
                        printf("Prijato %c\n", cbuf);*/
                        cbuf[strlen(cbuf)] = '\n';
                        //write(fd, cbuf, strlen(cbuf));
                        broadcast(cbuf, client_socks, max_clients, clients);
                    } else { // na socketu se stalo neco spatneho
                        close(fd);
                        FD_CLR(fd, &client_socks);

                        active_clients--;
                        for (i = 0; i < max_clients; i++) {
                            if (clients[i] == fd)
                                clients[i] = 0;
                        }

                        printf("Klient %d se odpojil a byl odebran ze sady socketu\n", fd);
                    }
                }
            }
        }

    }

    return 0;
}

void *lobby_stuff() {
    
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