/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

#include "sini_log.h"
#include "hex.h"
#include <stdlib.h>
#include <unistd.h>

unit *create_unit(int coord_x, int coord_z, unittype type, short ID) {
    unit *tmp = malloc(sizeof(unit));
    
    logger("INFO", "Creating unit");
    
    tmp->coord_x = coord_x;
    tmp->coord_z = coord_z;
    tmp->type = type;
    tmp->al = NEUTRAL;
    tmp->ID = ID;
    
    return tmp;
}

int destroy_unit(unit **u) {
    logger("INFO", "Destroying unit");
    
    if((*u)->ID == 0) {
        logger("WARN", "Unit is empty and can't be destroyed");
        return 0;
    }
    
    free(*u);
    *u = NULL;
    
    return 1;
}

int change_allegiance(unit* target, allegiance al) {
    logger("INFO", "Changing allegiance");
    target->al = al;
}

int deal_damage(unit *target, short amount) {
    target->health -= amount;
    logger("INFO", "Dealing damage");
    if(target->health <= 0) {
        target->dead = 1;
        logger("INFO", "Unit is dead");
    }
}

int move_unit(unit *target, int coord_x, int coord_z) {
    target->coord_x = coord_x;
    target->coord_z = coord_z;
    
    //send some network stuff
}
