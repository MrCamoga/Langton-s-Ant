#include <curl/curl.h>
#include <string.h>
#include <stdlib.h>

#include "auth.h"

size_t write_callback(void *ptr, size_t size, size_t nmemb, char **data) {
    size_t total_size = size * nmemb;

    *data = malloc(total_size);
    if(*data != NULL) strcpy(*data, (char *)ptr);

    return total_size;
}

int getaccesstoken(char **accesstoken, char *username, char *token) {
    char* url = "https://langtonsant.es/getaccesstoken.php";
    char msg[256];
    sprintf(msg,"username=%s&pass=%s",username,token);

    int err = 0;

    CURL *curl = curl_easy_init();
    if(curl == NULL) {
        err = auth_curl_error;
    } else {
        curl_easy_setopt(curl, CURLOPT_URL, url);
        curl_easy_setopt(curl, CURLOPT_POSTFIELDS, msg);
        
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, accesstoken);
        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, write_callback);

        CURLcode res = curl_easy_perform(curl);

        if(res != CURLE_OK) {
            err = auth_curl_error;
        } else if(*accesstoken == NULL) {
            err = auth_callback_error;
        }
        curl_easy_cleanup(curl);
    }

    return err;
}