package Exceptions;

public class CorruptMessageException extends AppException {

    public CorruptMessageException() {
        super("Corrupt message received");
    }
}
