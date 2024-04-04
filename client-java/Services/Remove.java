package Services;

import java.io.IOException;
import java.util.Map;

import javax.sound.midi.Soundbank;

import Driver.Connection;
import Driver.Constants;
import Exceptions.AppException;
import Exceptions.BadPathException;
import Exceptions.IllegalRangeException;

public class Remove extends Service {
     /**
     * Initalizes the Remove obj
     * @param r Connection 
     */

    public Remove(Connection r) {
        super(r);
        service_id = Constants.REMOVE_FILE_ID;
    }
    /* 
     * Implements Remove service with Caching 
     * @throws IOException
     */
    @Override
    public void act() throws IOException {
        String[] request_values = get_user_request_values();
        String pathname = request_values[0];

        try{ 
            // Send directly to server since file must be deleted 
            Map<String, Object> reply = send_and_receive(request_values);

            System.out.println(Constants.REPLY_SEPERATOR);

            // if request was successful
            if ((int) reply.get("status_code")==Constants.SUCCESSFUL_STATUS_ID) {
                System.out.println(Constants.REQUEST_SUCCESSFUL_MSG);

                //  remove key if in cache 
                if (connection.cache.containsKey(pathname)) {
                    connection.cache.remove(pathname);
                }

                System.out.println(pathname + " successfully removed!");
            } else {
                System.out.println(Constants.REQUEST_FAILED_MSG);
                System.out.println("Error: " + reply.get("error_message"));
            }

            System.out.println(Constants.REPLY_SEPERATOR);
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
