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

typedef struct { 
    int fd;
    long id_key;
    char player_name[16];
} client_data;

void server_stuff(void *arg);

#endif /* NET_INTERFACE_H */

