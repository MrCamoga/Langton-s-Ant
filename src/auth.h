#ifndef AUTH_H
#define AUTH_H

#include <stddef.h>

typedef enum {
    auth_curl_error = -1,
    auth_callback_error = -2
} auth_error_t;

size_t write_callback(void *ptr, size_t size, size_t nmemb, char **data);
int getaccesstoken(char **accesstoken, char *username, char *token);

#endif