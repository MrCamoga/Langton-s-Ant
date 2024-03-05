#ifndef CONNECTION_H
#define CONNECTION_H

#include <netinet/in.h>
#include <stdlib.h>
#include <netdb.h>
#include <string.h>
#include <stdio.h>

#define ASSIGNMENT_SIZE 50
#define SERVER_PORT 7357

typedef enum {
    packet_version = 0,
    packet_login = 1,
    packet_tasks = 2,
    packet_results = 3,
    packet_message = 4,
    packet_status = 5
} packet_t;

typedef enum {
    packet_status_invalid = -1,
	packet_status_outdated = 0, // client version is outdated
	packet_status_expiredtoken = 1, // user access token has expired
	packet_status_badauth =	2, // user credentials are wrong
	packet_status_unsettoken = 3, // user secret token is not set
	packet_status_logged = 4, // user has successfully logged in
	packet_status_newversion = 5, // there is a new version available
	packet_status_badrequest = 6, // bad packet
	packet_status_unauthorized = 7, // 
	packet_status_ratelimit = 8, // TODO rate limit exceeded
	packet_status_internalerror = 64, // general server error
	packet_status_authdisabled = 65, //	login disabled for maintenance
	packet_status_antdisabled =	66, // some type of ant (2d, hex, 3d, 4d,...) is disabled
} packet_status_t;

struct version {
    int major;
    int minor;
    int patch;
};

int init_connection();
int process_packets(int fd);

int packet_sendversion(int fd, struct version ver);
int packet_getversion(int fd, struct version *ver);

int packet_sendlogin(int fd, char *username, char *accesstoken);

int packet_readmessage(int fd, char **msg);
int packet_readstatus(int fd, char *id, char **msg);

int packet_readassignment(int fd, char *type, int *size, long long **rules);
int packet_requestassignment(int fd, char type, int size);
int packet_sendassignment(int fd, long long *results, int size);

#endif