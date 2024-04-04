#ifndef CONNECTION_H
#define CONNECTION_H

#include "constants.h"
#include "utils.h"
#include "exceptions.h"
#include <string>
#include <winsock2.h>
#include <unordered_map>
#include <vector>
#include <iostream>
#include <stdexcept>
// #include <netinet/in.h>
// #include <sys/socket.h>
#include <unistd.h>
#include <ws2tcpip.h>
#include "cache.h"

// class Cache;

class Connection {
private:
    int server_port;
    int request_id = 0;
    sockaddr_in server_address;
    int socket_fd;

public:
    SOCKET udpsock;
    int freshness_interval;
    bool at_most_once;
    double network_failure_rate;
    std::unordered_map<std::string, Cache> cache;

    Connection(const std::string& s_name, int s_port, int amo, double nfr, int f_interval);
    ~Connection();

    void sendPacket(const std::vector<char>& packet);
    std::vector<char> receive_packet();
    void close_connection();
    void increment_request_id();
    int get_request_id() const;
};

#endif /* CONNECTION_H */
