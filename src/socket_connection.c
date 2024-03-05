#include <netinet/in.h>
#include <stdlib.h>
#include <netdb.h>
#include <string.h>
#include <stdio.h>

#include "socket_connection.h"

int init_connection() {
    int sockfd;
    struct sockaddr_in server_addr;
    struct hostent *server;
    
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if(sockfd == -1) {
        perror("Socket error");
        return -1;
    }
    server = gethostbyname("langtonsant.es");
    if(server == NULL) {
        perror("Error host not found");
        return -1;
    }

    memset(&server_addr, 0, sizeof(server_addr));
    server_addr.sin_family = AF_INET;
    server_addr.sin_port = htons(7357);
    memcpy(&server_addr.sin_addr.s_addr, server->h_addr, server->h_length);

    if(connect(sockfd, (struct sockaddr*)&server_addr, sizeof(server_addr)) < 0) {
        perror("Error connecting");
        return -1;
    }

    printf("Connected\n");
    return sockfd;
}

int process_packets(int fd) {
    char pkid;
    while(1) {
        recv(fd,&pkid,1,0);
        if(pkid==packet_version) {
            struct version server_ver;
            packet_getversion(fd,&server_ver);
            printf("Version: %d.%d.%d\n",server_ver.major,server_ver.minor,server_ver.patch);
        } else if(pkid == packet_login) {
            char *username;
            packet_readmessage(fd,&username);
            printf("Logged in as %s\n",username);
            packet_requestassignment(fd, 0, ASSIGNMENT_SIZE);
        } else if(pkid == packet_tasks) {
            char type;
            int size;
            long long *rules;
            packet_readassignment(fd,&type,&size,&rules);
            printf("New assignment of %d rules\n",size/2);
        } else if(pkid == packet_results) {
            // TODO
        } else if(pkid == packet_message) {
            char *msg;
            packet_readmessage(fd,&msg);
            printf("%s\n",msg);
        } else if(pkid == packet_status) {
            char *msg;
            char statusid;
            packet_readstatus(fd,&statusid,&msg);
            printf("Status %d: %s\n",statusid,msg);
        } else {
            return pkid;
        }
    }

    return 0;
}

int packet_sendversion(int fd, struct version ver) {
    char pkid = packet_version;
    int b;
    send(fd,&pkid,1,0);
    b = htonl(ver.major);
    send(fd,&b,4,0);
    b = htonl(ver.minor);
    send(fd,&b,4,0);
    b = htonl(ver.patch);
    send(fd,&b,4,0);
    return 0;
}

int packet_getversion(int fd, struct version *ver) {
    int received;
    recv(fd, &received, sizeof(received), 0);
    ver->major = ntohl(received);
    recv(fd, &received, sizeof(received), 0);
    ver->minor = ntohl(received);
    recv(fd, &received, sizeof(received), 0);
    ver->patch = ntohl(received);
    return 0;
}

int packet_sendlogin(int fd, char *username, char *accesstoken) {
    char pkid = packet_login;
    send(fd,&pkid,1,0);
    int userlength = htonl(strlen(username));
    int tokenlength = htonl(strlen(accesstoken));
    send(fd,&userlength,4,0);
    send(fd,username,strlen(username),0);
    send(fd,&tokenlength,4,0);
    send(fd,accesstoken,strlen(accesstoken),0);
    return 0;
}

int packet_readmessage(int fd, char **msg) {
    int msglength;
    recv(fd, &msglength, sizeof(msglength), 0);
    msglength = ntohl(msglength);
    if(msglength > 4096) return -1;
    *msg = (char*) malloc(msglength*sizeof(char));
    if(*msg == NULL) return -1;
    recv(fd,*msg,msglength,0);
    return 0;
}

int packet_readstatus(int fd, char *id, char **msg) {
    recv(fd, id, sizeof(*id), 0);
    return packet_readmessage(fd, msg);
}

int packet_readassignment(int fd, char *type, int *size, long long **rules) {
    recv(fd, type, sizeof(*type), 0);
    recv(fd, size, sizeof(*size), 0);
    *size = ntohl(*size);
    *rules = (long long *)malloc((*size)*sizeof(**rules));
    for(int i = 0; i < *size; i++) {
        recv(fd, *rules+i, sizeof(**rules), 0);
        (*rules)[i] = htobe64((*rules)[i]);
    }
    return 0;
}

int packet_requestassignment(int fd, char type, int size) {
    char pkid = packet_tasks;
    send(fd, &pkid, sizeof(pkid), 0);
    send(fd, &type, sizeof(type), 0);
    size = htonl(size);
    send(fd, &size, sizeof(size), 0);
    return 0;
}

int packet_sendassignment(int fd, long long *results, int size) {
    char pkid = packet_results;
    send(fd, &pkid, sizeof(pkid), 0);
    long long c;
    for(int i = 0; i < size; i++) {
        c = htobe64(results[i]);
        send(fd,&c,sizeof(c),0);
    }
    return 0;
}