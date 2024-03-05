#include "properties.h"
#include <stdio.h>
#include <string.h>

#define PROPERTIES_FILE "langton.properties"

int read_properties(char **username, char **token) {
    FILE *file = fopen(PROPERTIES_FILE,"r");
    if(file == NULL) {
        return -1;
    }

    char line[200];
    while(fgets(line, sizeof(line), file) != NULL) {
        if(line[0] == '#' || line[0] == '\n' || line[0] == '\r') 
            continue;
        
        char *key = strtok(line, "=");
        char *value = strtok(NULL, "=");

        key = strtok(key, "\t\r\n ");
        value = strtok(value, "\t\r\n ");

        if(key != NULL && value != NULL) {
            if(strcmp(key, "username")==0) {
                *username = strdup(value);
            } else if(strcmp(key, "secrettoken")==0) {
                *token = strdup(value);
            }
        }
    }
    fclose(file);
    return 0;
}

int write_properties(char *username, char *token) {
    FILE *file =  fopen(PROPERTIES_FILE,"w");
    if(file == NULL) {
        return -1;
    }
    fprintf(file,"username=%s\nsecrettoken=%s\n",username,token);
    fclose(file);
    
    return 0;
}