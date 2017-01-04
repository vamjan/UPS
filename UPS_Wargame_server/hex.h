/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/* 
 * File:   hex.h
 * Author: Sini
 *
 * Created on 8. října 2016, 15:15
 */

#ifndef HEX_H
#define HEX_H

#define UNIT_ARRAY 17
#define ROUND_COUNT 5
#define DEFAULT_ROWS 10
#define DEFAULT_COLS 15

/**
 * Unit types
 */
typedef enum {
    INFANTRY = 'I', TANK = 'T', SPG = 'S', FLAG = 'F'
} unittype;

/**
 * Player and unit allegiances
 */
typedef enum {
    BLU = 'B', RED = 'R', NEUTRAL = 'N'
} allegiance;

/**
 * Structure to store unit data
 */
typedef struct {
    short ID;
    unittype type;
    allegiance al;
    short health;
    short damage;
    short move_range;
    short attack_range;
    int dead;
    int coord_x;
    int coord_z;
} unit;

//static const unit empty = {0};

/**
 * Structure to store playfield data
 */
typedef struct {
    char** terain;
    unit** units;
    int rows;
    int columns;
    int on_turn;
    int attacking;
    int score_one;
    int score_two;
} playfield;

static const char default_map[DEFAULT_ROWS][DEFAULT_COLS] = {
    {'D', 'D', 'D', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G'},
    {'D', 'D', 'D', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G'},
    {'G', 'D', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G'},
    {'G', 'G', 'G', 'G', 'G', 'G', 'D', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G'},
    {'G', 'G', 'G', 'G', 'G', 'D', 'D', 'D', 'G', 'G', 'G', 'G', 'G', 'G', 'G'},
    {'G', 'G', 'G', 'G', 'G', 'G', 'D', 'D', 'D', 'G', 'G', 'G', 'G', 'G', 'G'},
    {'G', 'G', 'G', 'G', 'G', 'G', 'G', 'D', 'G', 'G', 'G', 'G', 'G', 'G', 'G'},
    {'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'D', 'G'},
    {'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'D', 'D', 'D'},
    {'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'G', 'D', 'D', 'D'},
};                                 

static const unit default_units[] = {
    {4, TANK, BLU, 30, 10, 4, 3, 0, 1, 6},
    {11, TANK, RED, 30, 10, 4, 3, 0, 13, -4},
    {5, TANK, BLU, 30, 10, 4, 3, 0, 2, 7},
    {12, TANK, RED, 30, 10, 4, 3, 0, 12, -5},
    
    {7, INFANTRY, RED, 10, 5, 2, 1, 0, 13, -3},
    {0, INFANTRY, BLU, 10, 5, 2, 1, 0, 1, 5},
    {8, INFANTRY, RED, 10, 5, 2, 1, 0, 12, -3},
    {1, INFANTRY, BLU, 10, 5, 2, 1, 0, 2, 5},
    {9, INFANTRY, RED, 10, 5, 2, 1, 0, 11, -3},
    {2, INFANTRY, BLU, 10, 5, 2, 1, 0, 3, 5},
    {10, INFANTRY, RED, 10, 5, 2, 1, 0, 11, -4},
    {3, INFANTRY, BLU, 10, 5, 2, 1, 0, 3, 6},
    
    {6, SPG, BLU, 15, 12, 2, 5, 0, 2, 6},
    {13, SPG, RED, 15, 12, 2, 5, 0, 12, -4},
    
    {14, FLAG, NEUTRAL, 1, 1, 0, 0, 0, 5, 0},
    {15, FLAG, NEUTRAL, 1, 1, 0, 0, 0, 5, 4},
    {16, FLAG, NEUTRAL, 1, 1, 0, 0, 0, 11, -1}
};

//FUNCTIONS
//
//PLAYFILED
playfield *create_playfield(int rows, int columns);
int create_hex_map(playfield *pf);
char *get_hex(playfield *pf, int q, int r);
int set_hex(playfield *pf, int q, int r, char value);
int clear_playfield(playfield *pf);
int destroy_playfield(playfield **pf);
void print_playfield(playfield *pf);
char **parse_map(playfield *pf);

//GAME
unit *next_turn(playfield *pf);

//
//UNIT MANAGEMENT
int add_unit(playfield *pf, unit* target);
int rmv_unit(playfield *pf, unit* target);
unit *get_unit(playfield *pf, int index);
unit *get_unit_on_coords(playfield *pf, int x, int z);

//UNIT FUNCTIONS
unit *create_unit(int coord_x, int coord_z, unittype type, allegiance al, short ID);
int destroy_unit(unit **u);
int change_allegiance(unit* target, char al);
int deal_damage(unit *target, short amount);
int attack_unit(unit *source, unit *target);
int move_unit(unit *target, int coord_x, int coord_z);
int check_range(int start_x, int start_z, int dest_x, int dest_z, int range);
char *parse_unit(unit *target);

#endif /* HEX_H */

