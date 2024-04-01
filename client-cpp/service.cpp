#include "service.h"

Service* Service::generate_service(int service_id, Connection conn) {
    switch(service_id) {
        case Constants::READ_ID:
            return new Read(conn);
        case Constants::WRITE_ID:
            return new Write(conn);
        case Constants::MONITOR_ID:
            return new Monitor(conn);
        // case Constants::CLEAR_ID:
        //     return new Clear(conn);
        case Constants::TRIM_ID:
            return new Trim(conn);
        case Constants::EDIT_TIME_ID:
            return new EditTime(conn);
        case Constants::CREATE_FILE_ID:
            return new Create(conn);
        case Constants::REMOVE_FILE_ID:
            return new Remove(conn);
        case Constants::LIST_ID:
            return new ListDir(conn);
        default:
            return nullptr;
    }
}

void EditTime::act() {
    std::vector<std::string> request_values = get_user_request_values();
    try {
        std::map<std::string, std::string> reply = sendAndReceive(request_values);
        std::cout << "Edit time:" << std::endl;
        std::cout << std::stoi(reply["time"]) << std::endl;
        std::cout << "Done." << std::endl;
    } catch (const ApplicationException& ae) {
        std::cout << "Error: " << ae.what() << "." << std::endl;
    } catch (...) {
        std::cout << "Unexpected error." << std::endl;
    }
}

void ListDir::act() {
    std::vector<std::string> request_values = get_user_request_values();
    try {
        std::map<std::string, std::string> reply = sendAndReceive(request_values);
        int repeat = std::stoi(reply["repeat"]);
        for (int i = 0; i < repeat; i++) {
            std::string type_key = "type";
            std::string name_key = "name";
            if (repeat > 1) {
                type_key += " " + std::to_string(i);
                name_key += " " + std::to_string(i);
            }

            std::string type = "";
            if (std::stoi(reply[type_key]) == 1) {
                type = "dir: ";
            } else {
                type = "file: ";
            }
            std::string name = reply[name_key];
            std::cout << type << name << std::endl;
        }
        std::cout << "Done." << std::endl;
    } catch (const ApplicationException& ae) {
        std::cout << "Error: " << ae.what() << "." << std::endl;
    } catch (...) {
        std::cout << "Unexpected error." << std::endl;
    }
}

void Monitor::act() {
    std::vector<std::string> request_values = get_user_request_values();
    int monitor_period = std::stoi(request_values[1]);
    long monitor_start = Utils::getCurrentTimeAsLong();
    int monitor_request_id = connection.get_request_id();

    if (monitor_period < 0) {
        std::cout << "Error: bad monitor period" << std::endl;
        return;
    }

    try {
        sendAndReceive(request_values);
        std::vector<char> update_bytes;
        setsockopt(connection.udpsock, SOL_SOCKET, SO_RCVTIMEO, (const char*)&monitor_period, sizeof(int));
        std::cout << "Start receiving updates: " << std::endl;
        try {
            while (true) {
                long current_time = Utils::getCurrentTimeAsLong();
                if (current_time - monitor_start >= monitor_period) {
                    throw TimeoutException();
                }
                int remaining_time = monitor_period - static_cast<int>(current_time - monitor_start);
                setsockopt(connection.udpsock, SOL_SOCKET, SO_RCVTIMEO, (const char*)&remaining_time, sizeof(int));
                try {
                    update_bytes = Utils::receive_message(monitor_request_id, &connection);
                    std::map<std::string, std::string> update = Utils::un_marshall(-1, update_bytes);
                    std::cout << "Update: " << update["content"] << std::endl;
                } catch (const CorruptMessageException& c) {
                    std::cout << "(log) Received corrupt message; Throwing away" << std::endl;
                }
            }
        } catch (const TimeoutException& t) {
            std::cout << "Done receiving updates." << std::endl;
        }
    } catch (const ApplicationException& ae) {
        std::cout << "Error: " << ae.what() << "." << std::endl;
    }
}

void Read::act() {
    std::vector<std::string> request_values = get_user_request_values();
    std::string pathname = request_values[0];
    int offset = std::stoi(request_values[1]);
    int byte_count = std::stoi(request_values[2]);

    try {
        if (!connection.cache.count(pathname)) {
            connection.cache.emplace(pathname, new Cache(pathname, &connection));
        }

        Cache* cache_object = &(connection.cache.at(pathname));
        if (cache_object->mustReadServer(offset, byte_count, &connection)) {
            std::map<std::string, std::string> reply = sendAndReceive(request_values);
            cache_object->setCache(offset, byte_count, reply["content"]);
        }

        std::string content = cache_object->getCache(offset, byte_count);
        std::cout << "Content:" << std::endl;
        std::cout << content << std::endl;
        std::cout << "Done." << std::endl;
    } catch (const BadPathnameException& bpe) {
        if (connection.cache.count(pathname)) {
            connection.cache.erase(pathname);
        }
        std::cout << "Error: " << bpe.what() << "." << std::endl;
    } catch (const ApplicationException& ae) {
        std::cout << "Error: " << ae.what() << "." << std::endl;
    }
}
