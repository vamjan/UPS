#include "sini_log.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

static char momspaghetty[2][6] = { {"WARN"}, {"ERROR"} };

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
