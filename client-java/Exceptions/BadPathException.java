package Exceptions;

public class BadPathException extends AppException{

    public BadPathException() {
        super("pathname does not exist");
    }
}
