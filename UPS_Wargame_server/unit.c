
#include "sini_log.h"
#include "hex.h"
#include <stdlib.h>
#include <unistd.h>
#include <time.h>

/**
 * Allocate memory and create unit.
 * @param coord_x
 * @param coord_z
 * @param type
 * @param al
 * @param ID
 * @return 
 */
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

/**
 * Free unit and all its components.
 * @param u
 * @return 
 */
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

/**
 * Change allegiance of unit into a new one.
 * @param target
 * @param al
 * @return 
 */
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

/**
 * Deal damage to selected unit. Slightly randomised by 20% margin.
 * @param target
 * @param amount
 * @return 
 */
int deal_damage(unit *target, short amount) {
    srand(time(NULL)); //randomise dmg +-20%
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

/**
 * Attack selected unit. Checks range before and returns value according to that.
 * @param source
 * @param target
 * @return 
 */
int attack_unit(unit *source, unit *target) {
    if(check_range(source->coord_x, source->coord_z, target->coord_x, target->coord_z, source->attack_range) && !target->dead) {
        deal_damage(target, source->damage);
        return 1;
    } else {
        return 0;
    }
}

/**
 * Move unit to point given by axial coordinates. Checks range before and returns value according to that.
 * @param target
 * @param coord_x
 * @param coord_z
 * @return 
 */
int move_unit(unit *target, int coord_x, int coord_z) {
    if(check_range(target->coord_x, target->coord_z, coord_x, coord_z, target->move_range)) {
        target->coord_x = coord_x;
        target->coord_z = coord_z;
        return 1;
    } else {
        return 0;
    }
}
