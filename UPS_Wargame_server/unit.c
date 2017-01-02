/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

#include "sini_log.h"
#include "hex.h"
#include <stdlib.h>
#include <unistd.h>
#include <time.h>

unit *create_unit(int coord_x, int coord_z, unittype type, allegiance al, short ID) {
    unit *tmp = malloc(sizeof(unit));
    
    logger("INFO", "Creating unit");
    
    tmp->coord_x = coord_x;
    tmp->coord_z = coord_z;
    tmp->type = type;
    tmp->al = al;
    tmp->ID = ID;
    tmp->dead = 1;
    
    return tmp;
}

int destroy_unit(unit **u) {
    logger("INFO", "Destroying unit");
    
    if(!(*u)) {
        logger("WARN", "Unit is empty and can't be destroyed");
        return 0;
    }
    
    free(*u);
    *u = NULL;
    
    return 1;
}

int change_allegiance(unit* target, char al) {
    switch(al) {
        case 'B':
            target->al = BLU;
            return 1;
        case 'R':
            target->al = RED;
            return 1;
        default:
            return 0;
    }
}

int deal_damage(unit *target, short amount) {
    //TODO: randomise
    srand(time(NULL));
    int r = rand()%(int)(amount*0.4);
    r -= r/2;
    target->health -= (amount + r);
    logger("INFO", "Dealing damage");
    if(target->health <= 0) {
        target->dead = 1;
        logger("INFO", "Unit is dead");
    }
    return target->health;
}

int attack_unit(unit *source, unit *target) {
    if(check_range(source->coord_x, source->coord_z, target->coord_x, target->coord_z, source->attack_range) && !target->dead) {
        deal_damage(target, source->damage);
        return 1;
    } else {
        return 0;
    }
}

int move_unit(unit *target, int coord_x, int coord_z) {
    if(check_range(target->coord_x, target->coord_z, coord_x, coord_z, target->move_range)) {
        target->coord_x = coord_x;
        target->coord_z = coord_z;
        return 1;
    } else {
        return 0;
    }
}
