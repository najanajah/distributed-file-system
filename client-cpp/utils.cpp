#include "utils.h"


std::map<std::string, std::string> Utils::sendAndReceive(int service_id, std::vector<std::string> values, Connection* connection) {
    if (Constants::DEBUG) std::cout << "(log) Begin send/receive for service id " << service_id << ":" << std::endl;
    // connection->udpsock.set_timeout(Constants::TIMEOUT);
    // setting timeout 
    int timeout = Constants::TIMEOUT;

    if (setsockopt(connection->udpsock, SOL_SOCKET, SO_RCVTIMEO, (const char*)&timeout, sizeof(timeout)) < 0) {
        throw std::runtime_error("Failed to set timeout");
    }

    std::vector<char> reply_content;
    std::vector<std::vector<char>> request = marshall(connection->get_request_id(), service_id, values);
    sendMessage(request, connection);
    while (true) {
        // try {
            reply_content = receive_message(connection->get_request_id(), connection);
            // if (reply_content == SOCKET_ERROR){ 
                if (WSAGetLastError()==WSAETIMEDOUT){ 
                    std::cerr<<"Socket operation timed out ";
                }
            // }
            // break;
        // } catch (SocketTimeoutException& t) {
        //     if (Constants::DEBUG) std::cout << "(log) Socket timeout; Resending message" << std::endl;
        //     sendMessage(request, connection);
        // } catch (CorruptMessageException& c) {
        //     if (Constants::DEBUG) std::cout << "(log) Throwing away corrupt message" << std::endl;
        // }
            
    }

    std::map<std::string, std::string> reply;
    // try {
        reply = un_marshall(service_id, reply_content);
    // } catch (...) {
    //     // Handle exception
    // }
    
    if (connection->at_most_once) {
        std::cout << "(acknowledgment) ";
        std::vector<std::vector<char>> ack = marshall(connection->get_request_id(), Constants::ACKNOWLEDGMENT_ID, std::vector<std::string>());
        sendMessage(ack, connection);
    }
    if (Constants::DEBUG) std::cout << "(log) Finished send/receive for service id " << service_id << "." << std::endl;
    connection->increment_request_id();

    return reply;
}

std::vector<std::vector<char>> Utils::marshall(int request_id, int service_id, std::vector<std::string> values) {
    std::vector<std::pair<std::string, int>> params = Constants::get_request_params(service_id);
    std::vector<char> raw_content = marshall_to_content(service_id, params, values);
    return marshall_to_packets(request_id, raw_content);
}

void Utils::sendMessage(std::vector<std::vector<char>>& message, Connection* connection) {
    for (auto& packet : message) {
        connection->sendPacket(packet);
    }
    if (Constants::DEBUG) std::cout << "(log) Message sent" << std::endl;
}

std::vector<char> Utils::receive_message(int check_request_id, Connection* connection) {
    int total_packets = -1;
    std::vector<char> all_content;
    int current_packet = 0;
    int overall_content_size = -1;
    while (total_packets == -1 || current_packet != total_packets) {
        std::vector<char> packet = connection->receive_packet();
        std::vector<int> header = getHeader(packet);
        int receive_request_id = header[0];
        overall_content_size = header[1];
        int fragment_number = header[2];
        if (fragment_number != current_packet || receive_request_id > check_request_id) {
            throw CorruptMessageException();
        } else if (connection->at_most_once && receive_request_id < check_request_id) {
            if (Constants::DEBUG) std::cout << "(log) Blindly acknowledging old request id " << receive_request_id << std::endl;
            std::vector<std::vector<char>> ack = marshall(receive_request_id, Constants::ACKNOWLEDGMENT_ID, std::vector<std::string>());
            sendMessage(ack, connection);
            current_packet--;
            total_packets = -1;
        }
        if (total_packets == -1) {
            total_packets = static_cast<int>(std::ceil(overall_content_size * 1.0 / Constants::MAX_PACKET_CONTENT_SIZE));
        }
        add_byte_array(all_content, std::vector<char>(packet.begin() + Constants::PACKET_HEADER_SIZE, packet.end()));
        current_packet++;
    }
    all_content.resize(overall_content_size);
    if (Constants::DEBUG) std::cout << "(log) Message received" << std::endl;
    return all_content;
}

std::map<std::string, std::string> Utils::un_marshall(int service_id, std::vector<char>& raw_content) {
    int counter = 0;
    std::map<std::string, std::string> message;

    int status_id = bytesToInt(raw_content, counter);
    message["status_id"] = std::to_string(status_id);
    counter += Constants::INT_SIZE;

    std::vector<std::pair<std::string, int>> params;
    if (status_id == Constants::SUCCESSFUL_STATUS_ID) {
        params = Constants::get_successful_reply_params(service_id);
    } else {
        int alert_id = status_id;
        // Handle ApplicationException
        params = Constants::get_alert_reply_params(alert_id);
    }

    int repeat = 1;
    if (!params.empty() && params[0].first == "repeat") {
        repeat = bytesToInt(raw_content, counter);
        counter += Constants::INT_SIZE;
        params.erase(params.begin()); // Remove the "repeat" parameter
    }
    message["repeat"] = std::to_string(repeat);

    for (int r = 0; r < repeat; ++r) {
        for (const auto& param : params) {
            std::string param_name = param.first;
            if (repeat > 1) {
                param_name += " " + std::to_string(r);
            }
            int param_type = param.second;
            int param_value = bytesToInt(raw_content, counter);
            counter += Constants::INT_SIZE;
            message[param_name] = std::to_string(param_value);
            if (param_type == Constants::STRING_ID) {
                std::string s(raw_content.begin() + counter, raw_content.begin() + counter + param_value);
                counter += param_value;
                message[param_name] = s;
            }
        }
    }

    return message;
}

std::vector<char> Utils::marshall_to_content(int service_id, std::vector<std::pair<std::string, int>>& params, std::vector<std::string>& values) {
    std::vector<char> raw_content;
    raw_content = add_int(service_id, raw_content);
    for (size_t i = 0; i < params.size(); ++i) {
        int param_type = params[i].second;
        if (param_type == Constants::STRING_ID) {
            raw_content = add_int(values[i].size(), raw_content);
            raw_content = add_string(values[i], raw_content);
        } else if (param_type == Constants::INT_ID) {
            raw_content = add_int(std::stoi(values[i]), raw_content);
        }
    }
    return raw_content;
}

std::vector<std::vector<char>> Utils::marshall_to_packets(int request_id, std::vector<char>& raw_content) {
    int raw_content_size = raw_content.size();
    int total_packets = static_cast<int>(std::ceil(raw_content_size * 1.0 / Constants::MAX_PACKET_CONTENT_SIZE));
    std::vector<std::vector<char>> message;
    for (int fragment = 0; fragment < total_packets; ++fragment) {
        std::vector<char> packet;
        packet = add_int(request_id, packet);
        packet = add_int(raw_content_size, packet);
        packet = add_int(fragment, packet);
        int begin_index = fragment * Constants::MAX_PACKET_CONTENT_SIZE;
        int end_index = (fragment == total_packets - 1) ? raw_content_size : (fragment + 1) * Constants::MAX_PACKET_CONTENT_SIZE;
        packet.insert(packet.end(), raw_content.begin() + begin_index, raw_content.begin() + end_index);
        message.push_back(packet);
    }
    return message;
}

std::vector<char> Utils::add_int(int num, std::vector<char>& in) {
    for (int i = 0; i < Constants::INT_SIZE; ++i) {
        in.push_back((num >> (8 * (Constants::INT_SIZE - i - 1))) & 0xFF);
    }
    return in;
}

std::vector<char> Utils::add_string(const std::string& str, std::vector<char>& in) {
    in = add_int(str.size(), in);
    in.insert(in.end(), str.begin(), str.end());
    return in;
}

std::vector<char> Utils::add_byte_array(std::vector<char>& in, const std::vector<char>& add) {
    in.insert(in.end(), add.begin(), add.end());
    return in;
}

std::vector<char> Utils::to_primitive(const std::vector<char>& in) {
    return in;
}

int Utils::bytesToInt(const std::vector<char>& bytes, int start_index) {
    // int result = 0;
    // for (int i = 0; i < Constants::INT_SIZE; ++i) {
    //     result |= (bytes[start_index + i] & 0xFF) << (8 * (Constants::INT_SIZE - i - 1));
    // }
    // return result;
    return ((bytes[0] & 0xFF) << 24) |
           ((bytes[1] & 0xFF) << 16) |
           ((bytes[2] & 0xFF) << 8) |
           (bytes[3] & 0xFF);
}

int Utils::bytesToInt(const std::vector<char>& bytes, int start_index) {
    int result = 0;
    for (int i = 0; i < Constants::INT_SIZE; ++i) {
        result |= (bytes[start_index + i] & 0xFF) << (8 * (Constants::INT_SIZE - i - 1));
    }
    return result;
}

std::vector<int> Utils::getHeader(const std::vector<char>& packet) {
    std::vector<int> header(3);
    header[0] = bytesToInt(std::vector<char>(packet.begin(), packet.begin() + 4));
    header[1] = bytesToInt(std::vector<char>(packet.begin() + 4, packet.begin() + 8));
    header[2] = bytesToInt(std::vector<char>(packet.begin() + 8, packet.begin() + 12));
    return header;
}

long Utils::getCurrentTimeAsLong() {
    time_t currentTime = std::time(nullptr);
    return static_cast<long>(currentTime);
}
