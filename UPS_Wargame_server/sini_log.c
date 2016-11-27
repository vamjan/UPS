/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
#include "sini_log.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

static char momspaghetty[2][6] = { {"INFO"}, {"ERROR"} };

void logger(const char* tag, const char* message) {
    if(!strcmp(momspaghetty[0], tag) || !strcmp(momspaghetty[1], tag)){
        time_t now;
        time(&now);
        char* timestr = ctime(&now);
        timestr[strlen(timestr) - 1] = '\0';
        printf("%s [%s]: %s\n", timestr, tag, message);
    }
}

//dont ask
