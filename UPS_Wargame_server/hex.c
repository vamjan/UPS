/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include "hex.h"
#include "sini_log.h"
#include "server.h"

/*
 *
 */
playfield *create_playfield(int rows, int columns) {
    int i, j;
    playfield *pf = malloc(sizeof(playfield));
    
    char str[100];
    sprintf(str, "Creating playfield of size -> %d x %d", rows, columns);
    logger("INFO", str);
    
    pf->rows = rows;
    pf->columns = columns;
    
    pf->terain = malloc(sizeof(char *) * rows);
    for(i = 0; i < rows; i++) {
        pf->terain[i] = malloc(sizeof(char) * columns);
        for(j = 0; j < pf->columns; j++)
            pf->terain[i][j] = 'E';
    }
    
    pf->units = malloc(sizeof(unit *) * UNIT_ARRAY);
    
    for(i = 0; i < UNIT_ARRAY; i++) {
        //pf->units[i] = malloc(sizeof(unit));
        pf->units[i] = &empty;
    }
    
    return pf;
}

int create_hex_map(playfield *pf) {
    int i, j;
    for(i = 0; i < pf->rows; i++) {
        for(j = 0; j < pf->columns; j++) {
            pf->terain[i][j] = default_map[i][j];
        }
    }
    
    for(i = 0; i < 23; i++) {
        add_unit(pf, create_unit(default_units[i][0], default_units[i][1], INFANTRY, i+1));
    }
}

char *get_hex(playfield *pf, int r, int q) {
    // r = i
    // q = j - r/2
    int i = r;
    int j = q + (r+1)/2;
    if((i >= 0 && i < pf->rows) && (j >= 0 && j < pf->columns)) {
        return &(pf->terain[i][j]);
    } else {
        char str[100];
        sprintf(str, "[GET HEX] out of range -> array: %d - %d\tfield: %d - %d", i, j, q, r);
        logger("ERROR", str);
        return NULL;
    }
}

int set_hex(playfield *pf, int r, int q, char value) {
    int i = r;
    int j = q + (r+1)/2;
    if((i >= 0 && i < pf->rows) && (j >= 0 && j < pf->columns)) {
        pf->terain[i][j] = value;
        return 1;
    } else {
        char str[100];
        sprintf(str, "[SET HEX] out of range -> array: %d - %d\tfield: %d - %d", i, j, q, r);
        logger("ERROR", str);
        return 0;
    }
}

int clear_playfield(playfield *pf){
    logger("INFO", "Clearing playfield");
    
    if(!pf) {
        logger("ERROR", "Playfield is set to NULL. Cannot clear!");
        return 0;
    }
    
    int i;
    
    for(i = 0; i < pf->rows; i++)
        memset(pf->terain[i], 0, sizeof(char)*pf->columns);
    
    for(i = 0; i < UNIT_ARRAY; i++) {
        //memset(pf->units[i], 0, sizeof(unit));
        //pf->units[i] = &empty;
        destroy_unit(&pf->units[i]);
    }
    
    return 1;
}

int destroy_playfield(playfield **pf){
    logger("INFO", "Destroying playfield");
    
    if(!*pf) {
        logger("ERROR", "Playfield is set to NULL. Cannot destroy!");
        return 0;
    }
    
    logger("INFO", "Clearing field before destroying... just because I can...");
    if(clear_playfield(*pf)) {
        int i;
        
        for(i = 0; i < (*pf)->rows; i++) {
            free((*pf)->terain[i]);
        }
        
        /*for(i = 0; i < UNIT_ARRAY; i++) { not neccesary
            destroy_unit(&(*pf)->units[i]);
        }*/
        
        free((*pf)->terain);
        free((*pf)->units); //TODO: memory leak
        free(*pf);
        *pf = NULL;
        
        return 1;
    }
}

void print_playfield(playfield *pf){
    int i, j;
    
    if(!pf) {
        logger("ERROR", "Playfield is set to NULL. Cannot print!");
        return;
    }
    
    for(i = 0; i < pf->rows; i++) {
        if(i % 2 == 0) printf("    ");
        for(j = 0; j < pf->columns; j++) {
            printf("[%c %d %d]", pf->terain[i][j], i, j-(i+1)/2);
        }
        printf("\n");
    }
    printf("\n");
}

int check_range(int start_x, int start_z, int dest_x, int dest_z, int range) {
    int start_y = -start_x - start_z;
    int dest_y = -dest_x - dest_z;
    
    if(dest_x >= (start_x - range) && dest_x <= (start_x + range))
        if(dest_y >= (start_y - range) && dest_y <= (start_y + range))
            if(dest_z >= (start_z - range) && dest_z <= (start_z + range))
                return 1;
    return 0;
}

int add_unit(playfield *pf, unit* target){
    int i = 0;
    while(i < UNIT_ARRAY) {
        if(pf->units[i]->ID == 0) {
            pf->units[i] = target;
            break;
        }
        i++;
    }
}

int rmv_unit(playfield *pf, unit* target){
    int i = 0;
    while(i < UNIT_ARRAY) {
        if(pf->units[i] == target) {
            destroy_unit(&pf->units[i]);
            pf->units[i] = &empty;
            return 1;
        }
        i++;
    }
    return 0; //not tested
}

