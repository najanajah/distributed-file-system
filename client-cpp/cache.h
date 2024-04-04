#ifndef CACHE_H
#define CACHE_H

#include <iostream>
#include <unordered_map>
#include <cmath>
#include "constants.h"
#include "exceptions.h"
// #include "connection.h"
#include "utils.h"
class Connection;
class Cache {
public:
    Cache(const std::string& pn, Connection* conn);
    std::string getCache(int offset, int byte_count);
    void setCache(int offset, int byte_count, const std::string& new_content);
    bool mustReadServer(int offset, int byte_count, Connection* conn);
    
private:
    std::string pathname;
    long server_checkin_time;
    int last_known_edit_time;
    std::unordered_map<int, std::string> content;
    int final_block;

    int getStartBlock(int offset);
    int getEndBlock(int offset, int byte_count);
    void checkRange(int offset, int byte_count);
    bool cached(int offset, int byte_count);
    bool localFresh(int freshness_interval);
    bool serverFresh(Connection* conn);
    int getServerEditTime(Connection* conn);
};

#endif // CACHE_H
