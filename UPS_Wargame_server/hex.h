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

#define UNIT_ARRAY 100
#define DEFAULT_ROWS 10
#define DEFAULT_COLS 15

typedef enum {
    INFANTRY = 'I', TANK = 'T', SPG = 'S', FLAG = 'F'
} unittype;

typedef enum {
    BLU = 'B', RED = 'R', NEUTRAL = 'N'
} allegiance;

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

//#pragma pack(4)

typedef struct {
    char** terain;
    unit** units;
    int rows;
    int columns;
    unit *on_turn;
    int score_one;
    int score_two;
} playfield;
//#pragma pack()

static char default_map[DEFAULT_ROWS][DEFAULT_COLS] = {
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

//static int default_units[17][2] = { {1, 5}, {2, 5}, {3, 5}, {3, 6}, {1, 6}, {2, 7}, {2, 6}, 
//                                    {13, -3}, {12, -3}, {11, -3}, {11, -4}, {13, -4}, {12, -5}, {12, -4},
//                                    {4, -1}, {7, 0}, {11, 1} };

static const unit default_units[] = {
    {0, INFANTRY, BLU, 10, 5, 2, 1, 0, 1, 5},
    {1, INFANTRY, BLU, 10, 5, 2, 1, 0, 2, 5},
    {2, INFANTRY, BLU, 10, 5, 2, 1, 0, 3, 5},
    {3, INFANTRY, BLU, 10, 5, 2, 1, 0, 3, 6},
    {4, TANK, BLU, 30, 10, 4, 3, 0, 1, 6},
    {5, TANK, BLU, 30, 10, 4, 3, 0, 2, 7},
    {6, SPG, BLU, 15, 12, 2, 5, 0, 2, 6},
    
    {7, INFANTRY, RED, 10, 5, 2, 1, 0, 13, -3},
    {8, INFANTRY, RED, 10, 5, 2, 1, 0, 12, -3},
    {9, INFANTRY, RED, 10, 5, 2, 1, 0, 11, -3},
    {10, INFANTRY, RED, 10, 5, 2, 1, 0, 11, -4},
    {11, TANK, RED, 30, 10, 4, 3, 0, 13, -4},
    {12, TANK, RED, 30, 10, 4, 3, 0, 12, -5},
    {13, SPG, RED, 15, 12, 2, 5, 0, 12, -4},
    
    {14, FLAG, NEUTRAL, 1, 1, 0, 0, 0, 4, -1},
    {15, FLAG, NEUTRAL, 1, 1, 0, 0, 0, 7, 0},
    {16, FLAG, NEUTRAL, 1, 1, 0, 0, 0, 11, 1}
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

//
//UNIT MANAGEMENT
int add_unit(playfield *pf, unit* target);
int rmv_unit(playfield *pf, unit* target);

//UNIT FUNCTIONS
unit *create_unit(int coord_x, int coord_z, unittype type, allegiance al, short ID);
int destroy_unit(unit **u);
int change_allegiance(unit* target, allegiance al);
int deal_damage(unit *target, short amount);
int move_unit(unit *target, int coord_x, int coord_z);
int check_range(int start_x, int start_z, int dest_x, int dest_z, int range);
char *parse_unit(unit *target);

#endif /* HEX_H */

