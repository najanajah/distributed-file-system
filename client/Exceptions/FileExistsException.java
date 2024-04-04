package Exceptions;

public class FileExistsException extends AppException {
    public FileExistsException ()  {
        super("file already exists");
    }
}
