/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "net_interface.h"
#include "sini_log.h"
/**
 * Allocate memory and create buffer
 * @return 
 */
char *create_buffer() {
    char *retval = malloc(sizeof(char) * BUFFER_LENGTH);
    
    memset(retval, 0, sizeof(char) * BUFFER_LENGTH);
    
    return retval;
}

/**
 * Free and destroy buffer
 * @param buffer
 * @return 
 */
int destroy_buffer(char **buffer) {
    logger("INFO", "Destroying buffer");
    
    free(*buffer);
    *buffer = NULL;
    
    return 1;
}

/**
 * Adds a message into buffer. If the buffer is full, it will drop first DROP 
 * characters and move the rest to the start of the buffer.
 * @param buffer
 * @param msg
 * @param read
 * @return 
 */
int add_to_buffer(char *buffer, const char *msg, int *read) {
    logger("INFO", "Adding message to buffer...");
    int buffer_fill, msg_length;
    
    buffer_fill = strlen(buffer);
    msg_length = strlen(msg);
    
    if((buffer_fill + msg_length) > BUFFER_LENGTH - 1) {
        logger("INFO", "Buffer full! Dropping...");
        memmove(buffer, buffer + DROP, buffer_fill);
        buffer_fill -= DROP;
        memset(buffer + buffer_fill, 0, BUFFER_LENGTH - buffer_fill);
    }
    
    logger("INFO", "Filling buffer...");
    memcpy(buffer + buffer_fill, msg, msg_length);
    *read = buffer_fill;
}

/**
 * Flush characters which were already read and used.
 * @param buffer
 * @param read
 * @return 
 */
int flush_buffer(char *buffer, const int read) {
    logger("INFO", "Flushing buffer...");
    memset(buffer, 0, sizeof(char) * read);
    memcpy(buffer, buffer + read, sizeof(char) * (BUFFER_LENGTH - read));
    return 1;
}