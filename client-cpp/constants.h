#ifndef CONSTANTS_H
#define CONSTANTS_H

#include <vector>
#include <utility>
#include <map>
#include <string>

class Constants {
public:
    // Basic sizes
    static const int INT_SIZE = 4;
    static const int MAX_PACKET_SIZE = 256;
    static const int PACKET_HEADER_SIZE = 12;
    static const int MAX_PACKET_CONTENT_SIZE = MAX_PACKET_SIZE - PACKET_HEADER_SIZE;
    static const int FILE_BLOCK_SIZE = 100;

    // Basic ID's
    static const int INT_ID = 0;
    static const int STRING_ID = 1;
    static const int EXIT_ID = 0;

    // Basic settings
    static const int TIMEOUT = 5000;
    static const bool DEBUG = true;

    // Services
    static const std::string SERVICE_PROMPT;
    static const int READ_ID = 1;
    static const int WRITE_ID = 2;
    static const int MONITOR_ID = 3;
    static const int CLEAR_ID = 4;
    static const int TRIM_ID = 5;
    static const int EDIT_TIME_ID = 6;
    static const int ACKNOWLEDGMENT_ID = 7;
    static const int CREATE_FILE_ID = 8;
    static const int REMOVE_FILE_ID = 9;
    static const int LIST_ID = 10;

    // Parameters for requests
    static const std::vector<std::pair<std::string, int>> READ_REQUEST_PARAMS;
    static const std::vector<std::pair<std::string, int>> WRITE_REQUEST_PARAMS;
    static const std::vector<std::pair<std::string, int>> MONITOR_REQUEST_PARAMS;
    static const std::vector<std::pair<std::string, int>> CLEAR_REQUEST_PARAMS;
    static const std::vector<std::pair<std::string, int>> TRIM_REQUEST_PARAMS;
    static const std::vector<std::pair<std::string, int>> EDIT_TIME_REQUEST_PARAMS;
    static const std::vector<std::pair<std::string, int>> ACKNOWLEDGE_PARAMS;
    static const std::vector<std::pair<std::string, int>> CREATE_FILE_REQUEST_PARAMS;
    static const std::vector<std::pair<std::string, int>> REMOVE_FILE_REQUEST_PARAMS;
    static const std::vector<std::pair<std::string, int>> LIST_REQUEST_PARAMS;

    // Parameters for successful services
    static const int SUCCESSFUL_STATUS_ID = 0;
    static const std::vector<std::pair<std::string, int>> READ_REPLY_PARAMS;
    static const std::vector<std::pair<std::string, int>> WRITE_REPLY_PARAMS;
    static const std::vector<std::pair<std::string, int>> MONITOR_REPLY_PARAMS;
    static const std::vector<std::pair<std::string, int>> CLEAR_REPLY_PARAMS;
    static const std::vector<std::pair<std::string, int>> TRIM_REPLY_PARAMS;
    static const std::vector<std::pair<std::string, int>> EDIT_TIME_REPLY_PARAMS;
    static const std::vector<std::pair<std::string, int>> CREATE_REPLY_PARAMS;
    static const std::vector<std::pair<std::string, int>> REMOVE_REPLY_PARAMS;
    static const std::vector<std::pair<std::string, int>> LIST_REPLY_PARAMS;

    // Parameters for alerts (i.e. messages that are not "successful")
    static const int NO_SUCH_FILE_ID = 1;
    static const int BAD_RANGE_ID = 2;
    static const int FILE_EMPTY_ID = 3;
    static const int UPDATE_ID = 4;
    static const int FILE_ALREADY_EXISTS_ID = 5;
    static const int NOT_A_DIRECTORY_ID = 6;
    static const int SERVER_BUSY_ID = 11;
    static const std::vector<std::pair<std::string, int>> NO_SUCH_FILE_PARAMS;
    static const std::vector<std::pair<std::string, int>> BAD_RANGE_PARAMS;
    static const std::vector<std::pair<std::string, int>> FILE_EMPTY_PARAMS;
    static const std::vector<std::pair<std::string, int>> UPDATE_PARAMS;
    static const std::vector<std::pair<std::string, int>> FILE_ALREADY_EXISTS_PARAMS;
    static const std::vector<std::pair<std::string, int>> NOT_A_DIRECTORY_EXAMS;
    static const std::vector<std::pair<std::string, int>> SERVER_BUSY_PARAMS;
    static const std::vector<std::pair<std::string, int>> NOT_A_FILE_ID;


    static std::vector<std::pair<std::string, int>> get_request_params(int service_id);
    static std::vector<std::pair<std::string, int>> get_successful_reply_params(int service_id);
    static std::vector<std::pair<std::string, int>> get_alert_reply_params(int alert_id);
};

#endif // CONSTANTS_H
