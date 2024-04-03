package Services;

import java.io.IOException;
import java.util.Map;

import Exceptions.ApplicationException;
import Exceptions.BadPathnameException;
import Exceptions.BadRangeException;
import Helpers.CacheEntry;
import Helpers.Constants;
import Helpers.Connection;

public class Duplicate extends Service{

    public Duplicate(Connection r) {
        super(r);
        service_id = Constants.DUPLICATE_FILE_ID;
    }

    @Override
    public void act() throws IOException {
        String[] request_values = get_user_request_values();
        String pathname = request_values[0];
        // Not needed 
        String destination = request_values[1];
        try {
            // place a new CacheObject if it didn't exist before
            if (!connection.cache.containsKey(pathname)) {
                connection.cache.put(pathname, new CacheEntry(pathname, connection));
            }
            // CacheObject cache_object = connection.cache.get(pathname);
            // // only read from the server if we must
            // int offset=0; 
            // int byte_count=0; 

            //  Must send to server to duplicate the file 
            Map<String, Object> reply = send_and_receive(request_values);

            // Create duplicate file in the cache 
            connection.cache.put(destination, new CacheEntry(destination, connection));
            System.out.println("Content:");
            System.out.println(reply.get("content"));
            System.out.println("Done.");

        } 
        catch(BadPathnameException bpe) {
            System.out.println("Error: " + bpe.getMessage() + ".");
        }
        catch(BadRangeException bre){ 
            System.out.println("Error: " + bre.getMessage() + ".");
        }
        catch(ApplicationException a){ 
            System.out.println("Error: " + a.getMessage() + ".");
        }
        

    } 


}