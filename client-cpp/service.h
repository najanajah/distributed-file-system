#ifndef SERVICE_H
#define SERVICE_H

#include <iostream>
#include <string>
#include <vector>
#include <map>
#include <stdexcept>
#include "connection.h"
#include "utils.h"
#include "exceptions.h"
#include "cache.h"

class Service {
public:
    Connection connection;
    int service_id;

    Service(Connection conn) : connection(conn) {}

    virtual void act() {
        std::cout << "Base Service: This method should be overridden by subclasses." << std::endl;
    }

    static Service* generate_service(int service_id, Connection conn);

    std::map<std::string, std::string> sendAndReceiveValues(std::vector<std::string> values) {
        return sendAndReceive(service_id, values, &connection);
    };

    std::vector<std::string> get_user_request_values() {
        std::vector<std::pair<std::string, int>> params = Constants::get_request_params(service_id);
        std::vector<std::string> ret(params.size());
        for(size_t i = 0; i < params.size(); i++) {
            std::string prompt =  "Please enter the " + params[i].first + ": ";
            std::cout << prompt << std::endl;
            std::cin >> ret[i];
        }
        return ret;
    }
};

class Create : public Service {
public:
    Create(Connection conn) : Service(conn) {
        service_id = Constants::CREATE_FILE_ID;
    }
};

class EditTime : public Service {
public:
    EditTime(Connection conn) : Service(conn) {
        service_id = Constants::EDIT_TIME_ID;
    }

    void act() override;
};

class ListDir : public Service {
public:
    ListDir(Connection conn) : Service(conn) {
        service_id = Constants::LIST_ID;
    }

    void act() override;
};

class Monitor : public Service {
public:
    Monitor(Connection conn) : Service(conn) {
        service_id = Constants::MONITOR_ID;
    }

    void act() override;
};

class Read : public Service {
public:
    Read(Connection conn) : Service(conn) {
        service_id = Constants::READ_ID;
    }

    void act() override;
};

class Remove : public Service {
public:
    Remove(Connection conn) : Service(conn) {
        service_id = Constants::REMOVE_FILE_ID;
    }
};

class Trim : public Service {
public:
    Trim(Connection conn) : Service(conn) {
        service_id = Constants::TRIM_ID;
    }
};

class Write : public Service {
public:
    Write(Connection conn) : Service(conn) {
        service_id = Constants::WRITE_ID;
    }
};

#endif // SERVICE_H
