package Exceptions;

public class ServerBusyException extends AppException {
    public ServerBusyException  ()  {
        super("server is busy; please try again later");
    }
}
