/*
 * This module represents the game state and contains functions to work with it 
 * and to run the game itself.
 * This module is using flat-toped even-r hexagons
 * Jan Vampol
 * UPS
 */


#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include "hex.h"
#include "sini_log.h"
#include "server.h"

extern char *strdup(const char *s);

/**
 * Allocate the memory and create playfield. Setup to default state. Also allocates
 * unit array but does not fill it with units yet.
 * @param rows
 * @param columns
 * @return 
 */
playfield *create_playfield(int rows, int columns) {
    int i, j;
    playfield *pf = malloc(sizeof (playfield));

    char str[100];
    sprintf(str, "Creating playfield of size -> %d x %d", rows, columns);
    logger("INFO", str);

    pf->rows = rows;
    pf->columns = columns;

    pf->terain = malloc(sizeof (char *) * rows);
    for (i = 0; i < rows; i++) {
        pf->terain[i] = malloc(sizeof (char) * columns);
        for (j = 0; j < pf->columns; j++)
            pf->terain[i][j] = 'E';
    }

    pf->units = calloc(sizeof (unit *), UNIT_ARRAY);

    pf->on_turn = 0;
    pf->score_one = 0;
    pf->score_two = 0;
    pf->attacking = 0;

    return pf;
}

/**
 * Create a map from premade map and save it to the playfield.
 * Fill unit array with units.
 * @param pf
 * @return 
 */
int create_hex_map(playfield *pf) {
    int i, j;
    for (i = 0; i < pf->rows; i++) {
        for (j = 0; j < pf->columns; j++) {
            pf->terain[i][j] = default_map[i][j];
        }
    }

    for (i = 0; i < 17; i++) {
        unit *tmp = malloc(sizeof (unit));
        memcpy(tmp, &default_units[i], sizeof (unit));
        add_unit(pf, tmp);
    }
}

/**
 * Get hex value with given axial coordinates. Those coordinates have to be
 * altered as following: i = r + (q + 1) / 2
 *                       j = q
 * @param pf
 * @param r
 * @param q
 * @return 
 */
char *get_hex(playfield *pf, int r, int q) {
    int i = r + (q + 1) / 2;
    int j = q;
    if ((i >= 0 && i < pf->rows) && (j >= 0 && j < pf->columns)) {
        return &(pf->terain[i][j]);
    } else {
        char str[100];
        sprintf(str, "[GET HEX] out of range -> array: %d - %d\tfield: %d - %d", i, j, q, r);
        logger("ERROR", str);
        return NULL;
    }
}

/**
 * Set hex value on given axial coordinates. Those coordinates have to be
 * altered as following: i = r + (q + 1) / 2
 *                       j = q
 * @param pf
 * @param r
 * @param q
 * @return 
 */
int set_hex(playfield *pf, int r, int q, char value) {
    int i = r + (q + 1) / 2;
    int j = q;
    if ((i >= 0 && i < pf->rows) && (j >= 0 && j < pf->columns)) {
        pf->terain[i][j] = value;
        return 1;
    } else {
        char str[100];
        sprintf(str, "[SET HEX] out of range -> array: %d - %d\tfield: %d - %d", i, j, q, r);
        logger("ERROR", str);
        return 0;
    }
}

/**
 * Clear playfield of units and set the terrain to 0.
 * @param pf
 * @return 
 */
int clear_playfield(playfield *pf) {
    logger("INFO", "Clearing playfield");

    if (!pf) {
        logger("ERROR", "Playfield is set to NULL. Cannot clear!");
        return 0;
    }

    int i;

    for (i = 0; i < pf->rows; i++)
        memset(pf->terain[i], 0, sizeof (char)*pf->columns);

    for (i = 0; i < UNIT_ARRAY; i++) {
        destroy_unit(&pf->units[i]);
    }

    return 1;
}

/**
 * Destroy playfield and free all its components.
 * @param pf
 * @return 
 */
int destroy_playfield(playfield **pf) {
    logger("INFO", "Destroying playfield");

    if (!*pf) {
        logger("ERROR", "Playfield is set to NULL. Cannot destroy!");
        return 0;
    }

    logger("INFO", "Clearing field before destroying... just because I can...");
    if (clear_playfield(*pf)) {
        int i;

        for (i = 0; i < (*pf)->rows; i++) {
            free((*pf)->terain[i]);
        }

        free((*pf)->terain);
        free((*pf)->units); //TODO: memory leak
        free(*pf);
        *pf = NULL;

        return 1;
    }
}

/**
 * Increment turn.
 * When flags are on turn, they give score to players who captured them.
 * @param pf
 * @return 
 */
unit *next_turn(playfield *pf) {
    unit *retval = NULL;

    do {
        pf->on_turn++;
        retval = pf->units[pf->on_turn % UNIT_ARRAY];
        if (retval->type == FLAG && retval->al != NEUTRAL)
            (retval->al == BLU) ? pf->score_one++ : pf->score_two++;
        if (pf->on_turn > UNIT_ARRAY * ROUND_COUNT) {
            retval = NULL;
            break;
        }
    } while (retval->type == FLAG || retval->dead);

    return retval;
}

/**
 * Print playfield into the console
 * @param pf
 */
void print_playfield(playfield *pf) {
    int i, j;

    if (!pf) {
        logger("ERROR", "Playfield is set to NULL. Cannot print!");
        return;
    }

    for (i = 0; i < pf->rows; i++) {
        if (i % 2 == 0) printf("    ");
        for (j = 0; j < pf->columns; j++) {
            printf("[%c %d %d]", pf->terain[i][j], i - (j + 1) / 2, j);
        }
        printf("\n");
    }
    printf("\n");
    
    for(i = 0; i < UNIT_ARRAY; i++) {
        printf("[%d %d %d]\n", pf->units[i]->ID, pf->units[i]->coord_x, pf->units[i]->coord_z);
    }
}

/**
 * Parse map into the string to be send to clients
 * @param pf
 * @return 
 */
char **parse_map(playfield *pf) {
    char **retval = malloc(sizeof (char *) * pf->rows);

    int i;
    for (i = 0; i < pf->rows; i++) {
        retval[i] = calloc(sizeof (char), (pf->columns + 1));
        memcpy(retval[i], pf->terain[i], sizeof (char) * pf->columns);
    }

    return retval;
}

/**
 * Check range of two points on the map with given range
 * @param start_x
 * @param start_z
 * @param dest_x
 * @param dest_z
 * @param range
 * @return 
 */
int check_range(int start_x, int start_z, int dest_x, int dest_z, int range) {
    int start_y = -start_x - start_z;
    int dest_y = -dest_x - dest_z;

    if (dest_x >= (start_x - range) && dest_x <= (start_x + range))
        if (dest_y >= (start_y - range) && dest_y <= (start_y + range))
            if (dest_z >= (start_z - range) && dest_z <= (start_z + range))
                return 1;
    return 0;
}

/**
 * Add unit to the empty place in unit array.
 * @param pf
 * @param target
 * @return 
 */
int add_unit(playfield *pf, unit* target) {
    int i = 0;
    while (i < UNIT_ARRAY) {
        if (!pf->units[i]) {
            pf->units[i] = target;
            break;
        }
        i++;
    }
}

/**
 * Remove unit from the array and make empty place.
 * @param pf
 * @param target
 * @return 
 */
int rmv_unit(playfield *pf, unit* target) {
    int i = 0;
    while (i < UNIT_ARRAY) {
        if (pf->units[i] == target) {
            destroy_unit(&pf->units[i]);
            return 1;
        }
        i++;
    }
    return 0;
}

/**
 * Get unit on index in array.
 * @param pf
 * @param index
 * @return 
 */
unit *get_unit(playfield *pf, int index) {
    unit *retval = NULL;
    int i;
    
    for(i = 0; i < UNIT_ARRAY; i++) {
        if(pf->units[i]->ID == index) {
            retval = pf->units[i];
            break;
        }
    }
    
    return retval;
}

/**
 * Get unit on target axial coordinates
 * @param pf
 * @param x
 * @param z
 * @return 
 */
unit *get_unit_on_coords(playfield *pf, int x, int z) {
    unit *retval = NULL;
    int i;
    
    for(i = 0; i < UNIT_ARRAY; i++) {
        if(pf->units[i]->coord_x == x && pf->units[i]->coord_z == z) {
            if(pf->units[i]->type != FLAG && !pf->units[i]->dead) {
                retval = pf->units[i];
                break;
            } 
        }
    }
    
    return retval;
}

/**
 * Parse unit into string to be send via network.
 * @param target
 * @return 
 */
char *parse_unit(unit *target) {
    char retval[BUFFER_LENGTH];
    memset(retval, 0, sizeof(char) * BUFFER_LENGTH);
    //ID|TYPE|ALLE|HP|DMG|MOVRANGE|ATKRANGE|DEAD|X|Z
    snprintf(retval, BUFFER_LENGTH, "%d|%c|%c|%d|%d|%d|%d|%c|%d|%d", target->ID, target->type, target->al,
            target->health, target->damage, target->move_range, target->attack_range, (target->dead) ? 'T' : 'F',
            target->coord_x, target->coord_z);

    return strdup(retval);
}

