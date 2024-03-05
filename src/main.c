#include <stdio.h>
#include <stdlib.h>

#include "auth.h"
#include "properties.h"
#include "socket_connection.h"


int login(int fd) {
    char *username, *token;
    if(read_properties(&username, &token)!=0) {
        perror("Error while reading properties");
        return -1;
    }

    char *accesstoken;
    int err;
    if((err = getaccesstoken(&accesstoken, username, token) < 0)) {
        printf("Error getting the access token: %d\n",err);
        return -1;
    }

    // login
    struct version ver = { .major = 1, .minor = 1, .patch = 0 };
    packet_sendversion(fd, ver);
    packet_sendlogin(fd,username,accesstoken);

    free(accesstoken);
    free(username);
    free(token);

    return 0;
}

int main(int argc, char *argv[]) {
    int sockfd = init_connection();

    login(sockfd);

    int res = process_packets(sockfd);    

    return 0;
}