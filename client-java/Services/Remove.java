package Services;

import java.io.IOException;
import java.util.Map;

import Driver.Connection;
import Driver.Constants;
import Exceptions.AppException;
import Exceptions.BadPathException;
import Exceptions.IllegalRangeException;

public class Remove extends Service {

    public Remove(Connection r) {
        super(r);
        service_id = Constants.REMOVE_FILE_ID;
    }
    @Override
    public void act() throws IOException {
        String[] request_values = get_user_request_values();
        String pathname = request_values[0];

        try{ 
            // Send directly to server since file must be deleted 
            Map<String, Object> reply = send_and_receive(request_values);
            //  remove key if in cache 
            if (connection.cache.containsKey(pathname)) {
                connection.cache.remove(pathname);
            }

            System.out.println(pathname + " successfully removed!")
            System.out.println(Constants.END_OF_SERVICE);
        }
        catch(BadPathException bpe) {
            System.out.println("Error: " + bpe.getMessage() + ".");
        }
        catch(IllegalRangeException bre){ 
            System.out.println("Error: " + bre.getMessage() + ".");
        }
        catch(AppException a){ 
            System.out.println("Error: " + a.getMessage() + ".");
        }

    } 
}
