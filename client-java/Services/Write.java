package Services;

import java.io.IOException;
import java.util.Map;

import Exceptions.ApplicationException;
import Exceptions.BadPathnameException;
import Exceptions.BadRangeException;
import Helpers.CacheEntry;
import Helpers.Constants;
import Helpers.Connection;

public class Write extends Service {

    public Write(Connection r) {
        super(r);
        service_id = Constants.WRITE_ID;
    }

    @Override
    public void act() throws IOException {
        String[] request_values = get_user_request_values();
        String pathname = request_values[0];
        // Not needed 
        int offset = Integer.parseInt(request_values[1]);
        String content = request_values[2];
         
        try {
            // place a new CacheObject if it didn't exist before
            if (!connection.cache.containsKey(pathname)) {
                connection.cache.put(pathname, new CacheEntry(pathname, connection));
            }
            
            
            CacheEntry cache_object = connection.cache.get(pathname);
            // Get freshness 
            // Gets from server if cache is not fresh 
            if (cache_object.must_read_server(offset, Integer.MAX_VALUE, connection)) {
                String byte_count = Integer.toString(Integer.MAX_VALUE);
                String[] read_request = {pathname, "0" , byte_count};
                Map<String, Object> reply = send_and_receive(read_request);
                // updates the cache with new file 
                cache_object.set_cache(offset, Integer.MAX_VALUE, (String) reply.get("content"));
            }

            // gets updated file 
            String currentContent = cache_object.get_cache(0, Integer.MAX_VALUE);

            StringBuilder updatedContent = new StringBuilder(currentContent);
            updatedContent.insert(offset, content);

            // update cache 
            cache_object.set_cache(0, updatedContent.length(), updatedContent.toString());
            
            System.out.println("Content inserted for file " + pathname + "successfully at offset " + offset);
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

