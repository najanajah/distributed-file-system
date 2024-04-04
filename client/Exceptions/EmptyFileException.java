package Exceptions;

public class EmptyFileException extends AppException{
    public EmptyFileException() {
        super("file is empty");
    }
}
