/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   net_interface.h
 * Author: sini
 *
 * Created on 26 November 2016, 23:06
 */

#ifndef NETINTERFACE_H
#define NETINTERFACE_H

#define BUFFER_LENGTH 1000
#define DROP 200

char *create_buffer();
int destroy_buffer(char **buffer);
int add_to_buffer(char *buffer, const char *msg, int *read);
int flush_buffer(char *buffer, const int read);

#endif /* NETINTERFACE_H */

