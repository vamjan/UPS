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
#include "server.h"
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

    /*char *buff = create_buffer();
    char msg[100] = "aaa0FF34567|X|0002|data|data|0FF34567\naaa";
    int read = 0, i = 0;
    for (i = 0; i < 10; i++) {
        add_to_buffer(buff, msg, &read);
        printf("read = %d\tpointer = %p\n", read, buff);
        command *c = parse_input(buff, read);
        if (c) {
            flush_buffer(buff, read);
            read = 0;
            printf("%s\n", parse_output(c));
        }
    }

    destroy_buffer(&buff);*/

    char input;
    int run = 1, max_clients = 10;
    pthread_t thread;

    if (pthread_create(&thread, NULL, start_server, &max_clients)) {

        fprintf(stderr, "Error creating thread\n");
        return EXIT_FAILURE;
    }


    while (run) {
        scanf("%c", &input);

        switch (input) {
            case 'h':
                printf("Press 'q' to exit.\n");
                break;
            case 'q':
                run = 0;
                break;
            default:
                printf("Symbol %c is undefined.\n", input);
                break;
        }
    }
    
    return (EXIT_SUCCESS);
}

