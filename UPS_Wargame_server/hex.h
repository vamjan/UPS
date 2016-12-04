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
#define DEFAULT_ROWS 25
#define DEFAULT_COLS 30

typedef enum {
    INFANTRY, ARMORED, ARTILERY, STRUCTURE
} unittype;

typedef enum {
    TEAM1, TEAM2, NEUTRAL
} allegiance;

typedef struct {
    short ID;
    unittype type;
    allegiance al;
    short health;
    short damage;
    short move_range;
    short attack_range;
    short view;
    int dead;
    int coord_x;
    int coord_z;
} unit;

static unit empty = {0};

//#pragma pack(4)
typedef struct {
    char** terain;
    unit** units;
    int rows;
    int columns;
} playfield;
//#pragma pack()

static char default_map[DEFAULT_ROWS][DEFAULT_COLS] = { 
    {'G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G'},
    {'G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G'},
    {'G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G'},
    {'G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G'},
    {'G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G'},
    {'G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G'},
    {'G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G'},
    {'G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G'},
    {'G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','W','W','G','G','G','G','G','G','G','G','G'},
    {'G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','W','W','W','W','G','G','G','G','G','G','G','G'},
    {'G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','W','W','W','W','G','G','G','G','G','G','G','G'},
    {'G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','W','W','W','W','G','G','G','G','G','G','G','G','G'},
    {'G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','W','W','G','G','G','G','G','G','G','G','G','G'},
    {'G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','W','G','G','G','G','G','G','G','G','G','G'},
    {'G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G'},
    {'G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G'},
    {'G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G'},
    {'G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G'},
    {'G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G'},
    {'G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G'},
    {'G','G','G','W','W','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G'},
    {'G','W','W','W','W','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G'},
    {'G','W','W','W','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G'},
    {'W','W','W','W','W','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G'},
    {'W','W','W','W','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G','G'} 
};

static int default_units[23][2] = { {1, 23}, {1, 27}, {2, 26}, {2, 27}, {3, 26}, {3, 24}, {4, 22}, {5, 24}, {6, 23}, {7, 24},  
                                    {17, -8}, {18, -6}, {20, -9}, {20, -5}, {21, -8}, {22, -10}, {23, -10}, {23, -8}, {23, -6}, {24, -11},
                                    {15, 0}, {5, 0}, {25, 0} };

//FUNCTIONS
//
//PLAYFILED
playfield *create_playfield(int columns, int rows);
int create_hex_map(playfield *pf);
char *get_hex(playfield *pf, int q, int r);
int set_hex(playfield *pf, int q, int r, char value);
int clear_playfield(playfield *pf);
int destroy_playfield(playfield **pf);
void print_playfield(playfield *pf);
//
//UNIT MANAGEMENT
int add_unit(playfield *pf, unit* target);
int rmv_unit(playfield *pf, unit* target);

//UNIT FUNCTIONS
unit *create_unit(int coord_x, int coord_z, unittype type, short ID);
int destroy_unit(unit **u);
int change_allegiance(unit* target, allegiance al);
int deal_damage(unit *target, short amount);
int move_unit(unit *target, int coord_x, int coord_z);
int check_range(int start_x, int start_z, int dest_x, int dest_z, int range);

#endif /* HEX_H */

