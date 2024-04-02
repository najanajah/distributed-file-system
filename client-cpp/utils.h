#ifndef UTILS_H
#define UTILS_H

// #include "connection.h" 
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

class Connection;

std::map<std::string, std::string> sendAndReceive(int service_id, std::vector<std::string> values, Connection* Connection);
std::vector<std::vector<char>> marshall(int request_id, int service_id, std::vector<std::string> values);
void sendMessage(std::vector<std::vector<char>>& message, Connection* Connection);
std::vector<char> receiveMessage(int check_request_id, Connection* connection);
std::map<std::string, std::string> un_marshall(int service_id, std::vector<char>& raw_content);
long getCurrentTimeAsLong(); 
std::vector<char> marshall_to_content(int service_id, std::vector<std::pair<std::string, int>>& params, std::vector<std::string>& values);
std::vector<std::vector<char>> marshall_to_packets(int request_id, std::vector<char>& raw_content);
std::vector<char> add_int(int num, std::vector<char>& in);
std::vector<char> add_string(const std::string& str, std::vector<char>& in);
std::vector<char> add_byte_array(std::vector<char>& in, const std::vector<char>& add);
std::vector<char> to_primitive(const std::vector<char>& in);
int bytesToInt(const std::vector<char>& bytes);
int bytesToInt(const std::vector<char>& bytes, int start_index);
std::vector<int> getHeader(const std::vector<char>& packet);


#endif // UTILS_H
