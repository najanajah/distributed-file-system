package Services;

import java.io.IOException;
import java.util.Map;

import Driver.CacheEntry;
import Driver.Connection;
import Driver.Constants;
import Driver.Util;
import Exceptions.AppException;
import Exceptions.BadPathException;
import Exceptions.IllegalRangeException;

public class Insert extends Service {
    /**
     * Initalizes the Write obj
     * @param r Connection 
     */

    public Insert(Connection r) {
        super(r);
        service_id = Constants.WRITE_ID;
    }

    /**
     * Implements Write service with Caching
     * @throws IOException
     */
    @Override
    public void act() throws IOException {
        String[] request_values = get_user_request_values();
        String pathname = request_values[0];
        // Not needed 
        int offset = Integer.parseInt(request_values[1]);
        String content = request_values[2];
         
        try {
            if (offset < 0) {
                throw new IllegalRangeException();
            }
            
            Map<String, Object> reply = send_and_receive(request_values);
        
            // if request was successful
            if ((int) reply.get("status_code")==Constants.SUCCESSFUL_STATUS_ID) {
                System.out.println(Constants.REQUEST_SUCCESSFUL_MSG);
                // System.out.println(reply.get("content"));
                String value = (String) reply.get("content");
                
                connection.cache.put(pathname, new CacheEntry(pathname, connection));
                CacheEntry cache_object = connection.cache.get(pathname);
                cache_object.set_cache(offset, value.length(), value);
                
                System.out.println("Content inserted for file " + pathname + "successfully at offset " + offset + " :");
                System.out.println(content);
                System.out.println("Updated content: ");
                System.out.println(value);
            } else {
                // if the request was unsuccessful
                System.out.println(Constants.REQUEST_FAILED_MSG);
                System.out.println(reply.get("error_message"));
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

