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
#include "hex.h"
#include "net_interface.h"
#include "sini_log.h"

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
    
    server_stuff(NULL);
    
    return (EXIT_SUCCESS);
}

