#include "exceptions.h"

void ApplicationException::check_app_exception(int alert_id) {
    if (alert_id == Constants::NO_SUCH_FILE_ID) {
        throw BadPathnameException();
    }
    if (alert_id == Constants::BAD_RANGE_ID) {
        throw BadRangeException();
    }
    if (alert_id == Constants::FILE_EMPTY_ID) {
        throw FileEmptyException();
    }
    if (alert_id == Constants::FILE_ALREADY_EXISTS_ID) {
        throw FileAlreadyExistsException();
    }
    if (alert_id == Constants::NOT_A_DIRECTORY_ID) {
        throw NotADirectoryException();
    }
    if (alert_id == Constants::SERVER_BUSY_ID) {
        throw ServerBusyException();
    }
}
