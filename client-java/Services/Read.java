package Services;

import Exceptions.AppException;
import Exceptions.BadPathException;
import Exceptions.IllegalRangeException;

import java.io.IOException;
import java.util.Map;

import Driver.CacheEntry;
import Driver.Connection;
import Driver.Constants;

public class Read extends  Service {

    public Read(Connection r) {
        super(r);
        service_id = Constants.READ_ID;
    }

    @Override
    public void act() throws IOException {

        //ask for user input
        String[] request_values = get_user_request_values();
        String pathname = request_values[0];

        int offset = Integer.parseInt(request_values[1]);
        int byte_count = Integer.parseInt(request_values[2]);

        boolean is_safe_to_read_from_cache = true;
        boolean isCached = false;

        try {
            System.out.println(Constants.REPLY_SEPERATOR);

            if (offset < 0 || byte_count < 0) {
                throw new IllegalRangeException();
            }

            CacheEntry cache_object = null;

            // place a new CacheObject if it didn't exist before
            if (connection.cache.containsKey(pathname)) {
                isCached = true;
                cache_object = connection.cache.get(pathname);
            }
             
            // only read from the server if we must
            if (!isCached || cache_object.must_read_server(offset, byte_count, connection)) {
                Map<String, Object> reply = send_and_receive(request_values);
                System.out.println(reply.keySet());
                System.out.println(reply.values());

                // if request was successful
                if ((int) reply.get("status_code")==Constants.SUCCESSFUL_STATUS_ID) {
                    System.out.println(Constants.REQUEST_SUCCESSFUL_MSG);
                    if (!connection.cache.containsKey(pathname)) {
                        cache_object = new CacheEntry(pathname, connection);
                        connection.cache.put(pathname, cache_object);
                    }
                    cache_object.set_cache(offset, byte_count, (String) reply.get("content"));
                } else {
                    // if the request was unsuccessful
                    System.out.println(Constants.REQUEST_FAILED_MSG);
                    System.out.println(reply.get("error_message"));
                    is_safe_to_read_from_cache = false;
                }
            }

            if (is_safe_to_read_from_cache) {
                // either way, get the content from the cache
                String content = cache_object.get_cache(offset, byte_count);
                System.out.println("Content read from " + pathname + " :");
                System.out.println(content);
            }

            System.out.println(Constants.REPLY_SEPERATOR);
            System.out.println(Constants.END_OF_SERVICE);
        }
        catch(BadPathException bpe) {
            if (connection.cache.containsKey(pathname)) {
                connection.cache.remove(pathname);
            }
            System.out.println("Error: " + bpe.getMessage() + ".");
        }
        catch(AppException ae) {
            System.out.println("Error: " + ae.getMessage() + ".");
        }

    }

}
