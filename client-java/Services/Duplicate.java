package Services;

import java.io.IOException;
import java.util.Map;

import Driver.CacheEntry;
import Driver.Connection;
import Driver.Constants;
import Exceptions.AppException;
import Exceptions.BadPathException;
import Exceptions.IllegalRangeException;

public class Duplicate extends Service{

    public Duplicate(Connection r) {
        super(r);
        service_id = Constants.DUPLICATE_FILE_ID;
    }

    @Override
    public void act() throws IOException {
        String[] request_values = get_user_request_values();
        String source = request_values[0];
        // Not needed 
        // String destination = request_values[1];
        try {
            //  Must send to server to duplicate the file 
            Map<String, Object> reply = send_and_receive(request_values);
            System.out.println(Constants.REPLY_SEPERATOR);

            // if request was successful
            if ((int) reply.get("status_code")==Constants.SUCCESSFUL_STATUS_ID) {
                System.out.println(Constants.REQUEST_SUCCESSFUL_MSG);
                String content = (String) reply.get("content");
                // Create duplicate file in the cache 
                // place a new CacheObject if it didn't exist before
                if (!connection.cache.containsKey(source)) {
                    connection.cache.put(source, new CacheEntry(source, connection));
                } else {
                    CacheEntry cache_object = connection.cache.get(source);
                    cache_object.set_cache(0,content.length(), content);
                }
                // CacheEntry destination_cache_object = new CacheEntry(destination, connection);
                // destination_cache_object.set_cache(0, content.length(), content)
                // connection.cache.put(destination, destination_cache_object);
                System.out.println("File: " + source + " has been successfully duplicated");
            } else {
                System.out.println(Constants.REQUEST_FAILED_MSG);
                System.out.println("Error from server: " + reply.get("error_message"));
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