#include "connection.h"



Connection::Connection(const std::string& s_name, int s_port, int amo, double nfr, int f_interval) {
    server_port = s_port;
    freshness_interval = f_interval;
    at_most_once = amo == 1;
    network_failure_rate = nfr;

    // Initialize server address struct
    server_address.sin_family = AF_INET;
    server_address.sin_port = htons(server_port);
    // server_address.sin_addr.s_addr = inet_addr(s_name,c_str());

    // Using winsock2 instead 

    WSADATA wsaData;
    if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
        throw std::runtime_error("WSAStartup failed");
    }

    inet_pton(AF_INET, s_name.c_str(), &server_address.sin_addr);

    // Create UDP socket 
    udpsock = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);

    if(udpsock == INVALID_SOCKET){
        WSACleanup();
        throw std::runtime_error("Socket creation failed");
    }

    // if (inet_pton(AF_INET, s_name.c_str(), &server_address.sin_addr) <= 0) {
    //     throw std::invalid_argument("Invalid server address");
    // }

    // // Create UDP socket
    // if ((socket_fd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
    //     throw std::runtime_error("Socket creation failed");
    // }
}

Connection::~Connection() {
    // close(socket_fd);
    // Using WinSock2 
    closesocket(udpsock); 
    WSACleanup();
}

void Connection::sendPacket(const std::vector<char>& packet) {
    // change to array with byte characters 
    // Since the input is alr in packet DO i need to convert it here? 
    // std::vector<char> ret(packet.size());

    // for (size_t i = 0; i < packet.size(); ++i) {
    // // Assign the value from the input vector to the corresponding index in the new vector
    // ret[i] = in[i];
    // }

    double random = static_cast<double>(rand()) / RAND_MAX;
    if (random >= network_failure_rate) {
        sendto(udpsock, packet.data(), packet.size(), 0,
                reinterpret_cast<const sockaddr*>(&server_address), sizeof(server_address));
    } 
    // else if (Constants::DEBUG) 
    else
    {
        std::cout << ">> Network failure encountered: Simulating a failure in sending Packet" << std::endl;
    }
}

std::vector<char> Connection::receive_packet() {
    std::vector<char> buffer(Constants::MAX_PACKET_SIZE);
    socklen_t len = sizeof(server_address);
    recvfrom(udpsock, buffer.data(), buffer.size(), 0,
                reinterpret_cast<sockaddr*>(&server_address), &len);
    return buffer;
}

void Connection::close_connection() {
    if (at_most_once) {
        // struct timeval timeout;
        // timeout.tv_sec = Constants::TIMEOUT / 1000;  // convert timeout from milliseconds to seconds
        // timeout.tv_usec = (Constants::TIMEOUT % 1000) * 1000;  // remaining microseconds
        int timeout = Constants::TIMEOUT;
        setsockopt(udpsock, SOL_SOCKET, SO_RCVTIMEO, (const char*)&timeout, sizeof(timeout));
        // what is this doing honestly 
        if (Constants::DEBUG) std::cout << "(log) Begin acknowledging old replies" << std::endl;
        while (true) {
            try {
                receiveMessage(request_id, this);
            } catch (const std::runtime_error& e) {
                if (Constants::DEBUG) std::cout << "(log) Socket timeout; Done with cleanup" << std::endl;
                break;
            } catch (const CorruptMessageException& e) {
                if (Constants::DEBUG) std::cout << "(log) Throwing away corrupt message" << std::endl;
            }
        }
    }
    close(udpsock);
}

void Connection::increment_request_id() {
    request_id++;
}

int Connection::get_request_id() const {
    return request_id;
}


