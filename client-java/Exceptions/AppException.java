package Exceptions;
import Driver.Constants;

public class AppException extends Exception {
    public AppException (String m) {
        super(m);
    }
    public static void check_app_exception(int alert_id) throws AppException {
        if (alert_id == Constants.NO_SUCH_FILE_ID) {
            throw new BadPathException();
        }
        if (alert_id == Constants.BAD_RANGE_ID) {
            throw new IllegalRangeException();
        }
        if (alert_id == Constants.FILE_EMPTY_ID) {
            throw new EmptyFileException();
        }
        if (alert_id == Constants.FILE_ALREADY_EXISTS_ID) {
            throw new FileExistsException();
        }
        if (alert_id == Constants.NOT_A_DIRECTORY_ID) {
            throw new NotADirectoryException();
        }
        if (alert_id == Constants.NOT_A_FILE_ID) {
            throw new NotAFileException();
        }
        if (alert_id == Constants.SERVER_BUSY_ID) {
            throw new ServerBusyException();
        }
    }
}
