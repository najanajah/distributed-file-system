#include "constants.h"

// Services
const std::string Constants::SERVICE_PROMPT = "Please enter: 0 for exit, 1 for read, 2 for write, 3 for monitor, 4 for clear, 5 for trim, 6 for edit time, 8 for create file, 9 for remove file, 10 for list";

// Parameters for requests
const std::vector<std::pair<std::string, int>> Constants::READ_REQUEST_PARAMS = {
    {"pathname", STRING_ID},
    {"offset", INT_ID},
    {"byte_count", INT_ID}
};
const std::vector<std::pair<std::string, int>> Constants::WRITE_REQUEST_PARAMS = {
    {"pathname", STRING_ID},
    {"offset", INT_ID},
    {"content", STRING_ID}
};
const std::vector<std::pair<std::string, int>> Constants::MONITOR_REQUEST_PARAMS = {
    {"pathname", STRING_ID},
    {"monitor_time", INT_ID}
};
const std::vector<std::pair<std::string, int>> Constants::CLEAR_REQUEST_PARAMS = {
    {"pathname", STRING_ID}
};
const std::vector<std::pair<std::string, int>> Constants::TRIM_REQUEST_PARAMS = {
    {"pathname", STRING_ID}
};
const std::vector<std::pair<std::string, int>> Constants::EDIT_TIME_REQUEST_PARAMS = {
    {"pathname", STRING_ID}
};
const std::vector<std::pair<std::string, int>> Constants::ACKNOWLEDGE_PARAMS = {};
const std::vector<std::pair<std::string, int>> Constants::CREATE_FILE_REQUEST_PARAMS = {
    {"pathname", STRING_ID}
};
const std::vector<std::pair<std::string, int>> Constants::REMOVE_FILE_REQUEST_PARAMS = {
    {"pathname", STRING_ID}
};
const std::vector<std::pair<std::string, int>> Constants::LIST_REQUEST_PARAMS = {
    {"pathname", STRING_ID}
};

// Parameters for successful services
const std::vector<std::pair<std::string, int>> Constants::READ_REPLY_PARAMS = {
    {"content", STRING_ID}
};
const std::vector<std::pair<std::string, int>> Constants::WRITE_REPLY_PARAMS = {};
const std::vector<std::pair<std::string, int>> Constants::MONITOR_REPLY_PARAMS = {};
const std::vector<std::pair<std::string, int>> Constants::CLEAR_REPLY_PARAMS = {};
const std::vector<std::pair<std::string, int>> Constants::TRIM_REPLY_PARAMS = {};
const std::vector<std::pair<std::string, int>> Constants::EDIT_TIME_REPLY_PARAMS = {
    {"time", INT_ID}
};
const std::vector<std::pair<std::string, int>> Constants::CREATE_REPLY_PARAMS = {};
const std::vector<std::pair<std::string, int>> Constants::REMOVE_REPLY_PARAMS = {};
const std::vector<std::pair<std::string, int>> Constants::LIST_REPLY_PARAMS = {
    {"repeat", INT_ID},
    {"type", INT_ID},
    {"name", STRING_ID}
};

// Parameters for alerts
const std::vector<std::pair<std::string, int>> Constants::NO_SUCH_FILE_PARAMS = {
    {"message", STRING_ID}
};
const std::vector<std::pair<std::string, int>> Constants::BAD_RANGE_PARAMS = {
    {"message", STRING_ID}
};
const std::vector<std::pair<std::string, int>> Constants::FILE_EMPTY_PARAMS = {
    {"message", STRING_ID}
};
const std::vector<std::pair<std::string, int>> Constants::UPDATE_PARAMS = {
    {"pathname", STRING_ID},
    {"content", STRING_ID}
};
const std::vector<std::pair<std::string, int>> Constants::FILE_ALREADY_EXISTS_PARAMS = {
    {"message", STRING_ID}
};
const std::vector<std::pair<std::string, int>> Constants::NOT_A_DIRECTORY_EXAMS = {
    {"message", STRING_ID}
};
const std::vector<std::pair<std::string, int>> Constants::SERVER_BUSY_PARAMS = {
    {"message", STRING_ID}
};


std::vector<std::pair<std::string, int>> Constants::get_request_params(int service_id) {
    switch (service_id) {
        case READ_ID:
            return READ_REQUEST_PARAMS;
        case WRITE_ID:
            return WRITE_REQUEST_PARAMS;
        case MONITOR_ID:
            return MONITOR_REQUEST_PARAMS;
        case CLEAR_ID:
            return CLEAR_REQUEST_PARAMS;
        case TRIM_ID:
            return TRIM_REQUEST_PARAMS;
        case EDIT_TIME_ID:
            return EDIT_TIME_REQUEST_PARAMS;
        case ACKNOWLEDGMENT_ID:
            return ACKNOWLEDGE_PARAMS;
        case CREATE_FILE_ID:
            return CREATE_FILE_REQUEST_PARAMS;
        case REMOVE_FILE_ID:
            return REMOVE_FILE_REQUEST_PARAMS;
        case LIST_ID:
            return LIST_REQUEST_PARAMS;
        default:
            return {};
    }
}

std::vector<std::pair<std::string, int>> Constants::get_successful_reply_params(int service_id) {
    switch (service_id) {
        case READ_ID:
            return READ_REPLY_PARAMS;
        case WRITE_ID:
            return WRITE_REPLY_PARAMS;
        case MONITOR_ID:
            return MONITOR_REPLY_PARAMS;
        case CLEAR_ID:
            return CLEAR_REPLY_PARAMS;
        case TRIM_ID:
            return TRIM_REPLY_PARAMS;
        case EDIT_TIME_ID:
            return EDIT_TIME_REPLY_PARAMS;
        case CREATE_FILE_ID:
            return CREATE_REPLY_PARAMS;
        case REMOVE_FILE_ID:
            return REMOVE_REPLY_PARAMS;
        case LIST_ID:
            return LIST_REPLY_PARAMS;
        default:
            return {};
    }
}

std::vector<std::pair<std::string, int>> Constants::get_alert_reply_params(int alert_id) {
    switch (alert_id) {
        case NO_SUCH_FILE_ID:
            return NO_SUCH_FILE_PARAMS;
        case BAD_RANGE_ID:
            return BAD_RANGE_PARAMS;
        case FILE_EMPTY_ID:
            return FILE_EMPTY_PARAMS;
        case UPDATE_ID:
            return UPDATE_PARAMS;
        case FILE_ALREADY_EXISTS_ID:
            return FILE_ALREADY_EXISTS_PARAMS;
        case NOT_A_DIRECTORY_ID:
            return NOT_A_DIRECTORY_EXAMS;
        case SERVER_BUSY_ID:
            return SERVER_BUSY_PARAMS;
        default:
            return {};
    }
}
