package Services;

import Exceptions.ApplicationException;
import Exceptions.BadPathnameException;
import Helpers.CacheEntry;
import Helpers.Constants;
import Helpers.Connection;

import java.io.IOException;
import java.util.Map;

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



        try {
            // place a new CacheObject if it didn't exist before
            if (!connection.cache.containsKey(pathname)) {
                connection.cache.put(pathname, new CacheEntry(pathname, connection));
            }

            CacheEntry cache_object = connection.cache.get(pathname);
            // only read from the server if we must

            if (cache_object.must_read_server(offset, byte_count, connection)) {
                Map<String, Object> reply = send_and_receive(request_values);
                cache_object.set_cache(offset, byte_count, (String) reply.get("content"));
            }

            // either way, get the content from the cache
            String content = cache_object.get_cache(offset, byte_count);
            System.out.println("Content:");
            System.out.println(content);
            System.out.println("Done.");
        }
        catch(BadPathnameException bpe) {
            if (connection.cache.containsKey(pathname)) {
                connection.cache.remove(pathname);
            }
            System.out.println("Error: " + bpe.getMessage() + ".");
        }
        catch(ApplicationException ae) {
            System.out.println("Error: " + ae.getMessage() + ".");
        }

    }

}
