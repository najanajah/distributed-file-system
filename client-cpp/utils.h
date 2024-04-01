#ifndef UTILS_H
#define UTILS_H

#include "connection.h" 

#include <vector>
#include <string>
#include <stdexcept>
#include <map>
#include "constants.h"
#include "exceptions.h"
#include "connection.h"
#include <iostream>
#include <cmath>
#include <algorithm>
#include <ctime>


class Utils {
public:
    static std::map<std::string, std::string> sendAndReceive(int service_id, std::vector<std::string> values, Connection* Connection);
    static std::vector<std::vector<char>> marshall(int request_id, int service_id, std::vector<std::string> values);
    static void sendMessage(std::vector<std::vector<char>>& message, Connection* Connection);
    static std::vector<char> receive_message(int check_request_id, Connection* Connection);
    static std::map<std::string, std::string> un_marshall(int service_id, std::vector<char>& raw_content);
    static long getCurrentTimeAsLong(); 

private:
    static std::vector<char> marshall_to_content(int service_id, std::vector<std::pair<std::string, int>>& params, std::vector<std::string>& values);
    static std::vector<std::vector<char>> marshall_to_packets(int request_id, std::vector<char>& raw_content);
    static std::vector<char> add_int(int num, std::vector<char>& in);
    static std::vector<char> add_string(const std::string& str, std::vector<char>& in);
    static std::vector<char> add_byte_array(std::vector<char>& in, const std::vector<char>& add);
    static std::vector<char> to_primitive(const std::vector<char>& in);
    static int bytesToInt(const std::vector<char>& bytes);
    static int bytesToInt(const std::vector<char>& bytes, int start_index);
    static std::vector<int> getHeader(const std::vector<char>& packet);
};

#endif // UTILS_H
