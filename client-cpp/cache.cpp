#include "cache.h"

Cache::Cache(const std::string& pn, Connection* conn) : pathname(pn), final_block(-1) {
    last_known_edit_time = getServerEditTime(conn);
}

std::string Cache::getCache(int offset, int byte_count) {
    checkRange(offset, byte_count);
    int start_block = getStartBlock(offset);
    int end_block = getEndBlock(offset, byte_count);
    std::string cache_content;

    for (int i = start_block; i <= end_block; i++) {
        cache_content += content[i];
    }

    return cache_content.substr(offset % Constants::FILE_BLOCK_SIZE, byte_count);
}

void Cache::setCache(int offset, int byte_count, const std::string& new_content) {
    checkRange(offset, byte_count);
    int start_block = getStartBlock(offset);
    int end_block = getEndBlock(offset, byte_count);

    for (int i = start_block; i < end_block; i++) {
        content[i] = new_content.substr(i * Constants::FILE_BLOCK_SIZE, Constants::FILE_BLOCK_SIZE);
    }

    content[end_block] = new_content.substr(end_block * Constants::FILE_BLOCK_SIZE);
    if (content[end_block].length() != Constants::FILE_BLOCK_SIZE) {
        final_block = end_block;
    }
}

bool Cache::mustReadServer(int offset, int byte_count, Connection* conn) {
    return !cached(offset, byte_count) || (!localFresh(conn->freshness_interval) && !serverFresh(conn));
}

int Cache::getStartBlock(int offset) {
    return static_cast<int>(std::floor(offset * 1.0 / Constants::FILE_BLOCK_SIZE));
}

int Cache::getEndBlock(int offset, int byte_count) {
    return static_cast<int>(std::floor((offset + byte_count) * 1.0 / Constants::FILE_BLOCK_SIZE));
}

void Cache::checkRange(int offset, int byte_count) {
    int end_block = getEndBlock(offset, byte_count);
    if (offset < 0 || byte_count < 0 ||
        (final_block != -1 && final_block == end_block &&
         (offset + byte_count) % Constants::FILE_BLOCK_SIZE > content[end_block].length()) ||
        (final_block != -1 && end_block > final_block)) {
        throw BadRangeException();
    }
}

bool Cache::cached(int offset, int byte_count) {
    checkRange(offset, byte_count);
    int start_block = getStartBlock(offset);
    int end_block = getEndBlock(offset, byte_count);

    for (int i = start_block; i <= end_block; i++) {
        if (content.find(i) == content.end()) {
            return false;
        }
    }

    return true;
}

bool Cache::localFresh(int freshness_interval) {
    long current_time = getCurrentTimeAsLong();
    bool fresh = current_time - server_checkin_time < freshness_interval;
    if (Constants::DEBUG) {
        std::cout << "(log) Checking freshness locally: it is currently " << current_time
                  << " and we last checked the server at time " << server_checkin_time << std::endl;
        if (fresh) {
            std::cout << "(log) -> fresh locally" << std::endl;
        } else {
            std::cout << "(log) -> not fresh locally" << std::endl;
        }
    }
    return fresh;
}

bool Cache::serverFresh(Connection* conn) {
    int last_edit_time = getServerEditTime(conn);
    if (last_known_edit_time == last_edit_time) {
        return true;
    } else {
        last_known_edit_time = last_edit_time;
        content.clear();
        final_block = -1;
        return false;
    }
}

int Cache::getServerEditTime(Connection* conn) {
    server_checkin_time = getCurrentTimeAsLong();
    try {
        std::vector<std::string> request_values = {pathname}; // Assuming pathname is a member variable of Cache class
        std::map<std::string, std::string> reply = sendAndReceive(Constants::EDIT_TIME_ID, request_values, conn);
        return std::stoi(reply["time"]); // Assuming the time value is returned as a string in the reply map
    } catch (const BadPathnameException& nsfe) {
        throw; // Rethrow the exception
    } catch (const ApplicationException& ae) {
        std::cerr << "unexpected error: " << ae.what() << std::endl;
        return -1;
    }
}

