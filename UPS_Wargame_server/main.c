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
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <string.h>
#include "hex.h"
#include "server.h"
#include "sini_log.h"
#include "net_interface.h"
#include "parser.h"

/*
 * 
 */
int main(int argc, char** argv) {
    /*playfield* pf = create_playfield(25, 30);
    create_hex_map(pf);
    
    int i, j;
    for(i = 0; i < 3; i++) {
        for(j = 0; j < 5; j++) {
            set_hex(pf, i, j, 'H');
        }
    }
    
    if(check_range(0, 0, 1, -3, 3)) {
        printf("Range is good\n");
    } else {
        printf("Range is bad\n");
    }
    
    print_playfield(pf);
    
    destroy_playfield(&pf);*/

    //server_stuff(NULL);

    char *buff = create_buffer();
    char msg[100] = "aaa01234567|datdatadatadata|01234567\naaa";
    int read = 0, i = 0;
    for (i = 0; i < 10; i++) {
        add_to_buffer(buff, msg, &read);
        printf("read = %d\tpointer = %p\n", read, buff);
        command *c = parse_input(buff, read);
        if (c) {
            flush_buffer(buff);
            read = 0;
        }
    }

    destroy_buffer(&buff);

    return (EXIT_SUCCESS);
}

