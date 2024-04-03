package Driver;

import Exceptions.AppException;
import Exceptions.IllegalRangeException;
import Exceptions.BadPathException;
import java.lang.NullPointerException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * Entry for each file in the Cache.
 */
public class CacheEntry {

    private String pathname;
    // the last time file was checked in with servr
    private long server_checkin_time;
    // the last time the file was edited at the server according to client
    private long last_known_edit_time;

    private HashMap<Integer, String> content;
    private int final_block;

    /** Set the pathname, server_checkin_time, and last_known_edit time
     * @param pn pathname
     * @param connection connection information
     * @throws IOException sending/receiving message
     * @throws BadPathException if requesting the edit time at the server yields bad pathname
     */
    public CacheEntry(String pn, Connection connection) throws IOException, BadPathException {
        pathname = pn;
        // also sets server_checkin_time
        last_known_edit_time = get_server_edit_time(connection);
        content = new HashMap<>();
        final_block = -1;
    }

    /** Grab the requested string from the cache, assuming the necessary blocks exist
     * @param offset file offset
     * @param byte_count number of bytes to read
     * @return requested string from cache
     * @throws IllegalRangeException see check_range method (bottom)
     */
    public String get_cache(int offset, int byte_count) throws IllegalRangeException {
        check_range(offset, byte_count);
        int start_block = get_start_block(offset);
        int end_block = get_end_block(offset, byte_count);
        int start_index = offset%Constants.FILE_BLOCK_SIZE;
        int end_index = (offset+byte_count)%Constants.FILE_BLOCK_SIZE;
        if (start_block == end_block) {
            return content.get(start_block).substring(start_index, end_index);
        }
        StringBuilder answer = new StringBuilder();
        answer.append(content.get(start_block).substring(start_index));
        for (int i = start_block+1; i < end_block; i++) {
            answer.append(content.get(i));
        }
        answer.append(content.get(end_block).substring(0,end_index));
        return answer.toString();
    }

    /** Set the cache with new content read from server
     * Set entire blocks, not just the range requested
     * @param offset file offset
     * @param byte_count number of bytes read
     * @param new_content read content returned by the server (in blocks)
     * @throws IllegalRangeException see check_range method (bottom)
     */
    public void set_cache(int offset, int byte_count, String new_content) throws IllegalRangeException, BadPathException {
        if(new_content==null){ 
            throw new BadPathException();
        }
        check_range(offset, byte_count);
        int start_block = get_start_block(offset);
        int end_block = get_end_block(offset, byte_count);
        for (int i = start_block; i < end_block; i++) {
            int startIndex = i*Constants.FILE_BLOCK_SIZE;
            int endIndex = (i+1)*Constants.FILE_BLOCK_SIZE;
            content.put(i,
                    new_content.substring(startIndex, endIndex));
        }
        String last_piece = new_content.substring(end_block*Constants.FILE_BLOCK_SIZE);
        if (last_piece.length() != Constants.FILE_BLOCK_SIZE) {
            if (Constants.DEBUG) System.out.println("(log) Final block set to " + end_block);
            final_block = end_block;
        }
        content.put(end_block, last_piece);
    }

    /** Whether we must read the content from the server
     * There are two cases where we need to read from the server:
     * 1. if it is not cached locally
     * 2. if it is cached locally, expired the freshness interval, and has been edited at the server
     * @param offset file offset
     * @param byte_count number of bytes to read
     * @param connection connection info
     * @return whether or not one must read the server
     * @throws IOException send/receive message
     * @throws BadPathException if requesting edit time at server yields bad pathname
     * @throws IllegalRangeException if the given offset/byte_count combo is certain to be out of range
     */
    public boolean must_read_server(int offset, int byte_count, Connection connection) throws IOException, BadPathException, IllegalRangeException {
        boolean must = !cached(offset, byte_count) || (!local_fresh(connection.freshness_interval) && !server_fresh(connection));
        if (Constants.DEBUG) {
            if (must) {
                System.out.println ("(log) Must read from server");
            }
            else {
                System.out.println ("(log) No need to read from server");
            }
        }
        return must;
    }

    /** checks whether the requested offset/byte_count combo already exist in the cache
     * @param offset file offset
     * @param byte_count number of bytes to read
     * @return whether it exists in the cache
     * @throws IllegalRangeException if the given offset/byte_count combo is certain to be out of range
     */
    private boolean cached(int offset, int byte_count) throws IllegalRangeException {
        check_range(offset, byte_count);
        int start_block = get_start_block(offset);
        int end_block = get_end_block(offset, byte_count);
        for (int i = start_block; i <= end_block; i++) {
            if (!content.containsKey(i)) {
                if (Constants.DEBUG) System.out.println("(log) Checking cache: NOT cached");
                return false;
            }
        }
        if (Constants.DEBUG) System.out.println("(log) Checking cache: cached");
        return true;
    }

    /** Whether the freshness interval has expired or not
     * @return expired?
     */
    private boolean local_fresh(int freshness_interval) {
        long current_time = System.currentTimeMillis();
        boolean fresh =  current_time - server_checkin_time < freshness_interval;
        if (Constants.DEBUG) System.out.println("(log) Checking freshness locally: it is currently " + current_time +
                " and we last checked the server at time " + server_checkin_time);
        if (Constants.DEBUG) {
            if (fresh) {
                System.out.println("(log) -> cache is fresh locally");
            }
            else {
                System.out.println("(log) -> cache is not fresh locally");
            }
        }
        return fresh;
    }

    /** Whether the last update time at the server matches our last known edit time
     * If it doesn't match, then we must clear the cache (as it is now out of date)
     * @param connection connection info
     * @return match?
     * @throws IOException send/receive messages
     * @throws BadPathException if requesting the edit time at the server yields bad pathname
     */
    private boolean server_fresh(Connection connection) throws IOException, BadPathException {
        long last_edit_time = get_server_edit_time(connection);
        if (Constants.DEBUG) System.out.println("(log) Checking server: our last known edit time is " + last_known_edit_time +
                " and the server's last edit time is " + last_edit_time);
        if (last_known_edit_time == last_edit_time) {
            if (Constants.DEBUG) System.out.println("(log) -> fresh at server");
            return true;
        }
        else{
            if (Constants.DEBUG) System.out.println("(log) -> not fresh at server");
            last_known_edit_time = last_edit_time;
            content = new HashMap<>();
            final_block = -1;
            return false;
        }
    }

    /** 
     * Retrieves files last updated time from server and updates.  
     * @param connection connection information
     * @return time in ms (Long)
     * @throws IOException send/receive messages
     * @throws BadPathException if requesting the edit time at the server yields bad pathname
     */
    private long get_server_edit_time(Connection connection) throws IOException, BadPathException, NullPointerException {
        server_checkin_time = System.currentTimeMillis();
        // Request values for read function
        String[] request_values = {pathname};
        try {
            Map<String, Object> reply = Util.send_and_receive(Constants.EDIT_TIME_ID, request_values, connection);
            System.out.println(reply.keySet());
            System.out.println(reply.values());
            String contentString = (String) reply.get("content");
            long time = Long.parseLong(contentString);
            // changed from time to -> content generalise across replies 
            return time ;
        }
        catch (BadPathException nsfe) {
            throw new BadPathException();
        }
        catch (AppException ae) {
            System.out.println("unexpected error: " + ae.getMessage());
            return -1;
        }
        catch (NullPointerException n){
            // System.out.println("unexpected error: " + n.getMessage());
            // return ;
            throw new NullPointerException();
        }
    }

    private int get_start_block(int offset) {
        return (int) Math.floor(offset * 1.0 / Constants.FILE_BLOCK_SIZE);
    }

    private int get_end_block(int offset, int byte_count) {
        if (byte_count == Integer.MAX_VALUE) {
            return Integer.MAX_VALUE / Constants.FILE_BLOCK_SIZE;
        }
        return (int) Math.floor((offset + byte_count) * 1.0 / Constants.FILE_BLOCK_SIZE);
    }

    /** 
     * Checks if the offset and byte_count are out of range
     * @param offset file offset
     * @param byte_count number of bytes to read
     * @throws IllegalRangeException if the combo is indeed out of range
     */
    private void check_range(int offset, int byte_count) throws IllegalRangeException {
        int end_block = get_end_block(offset, byte_count);
        if (offset <= 0 ||
            byte_count < 0 ||
            (final_block != -1 && final_block == end_block && (offset+byte_count) % Constants.FILE_BLOCK_SIZE > content.get(end_block).length()) ||
            (final_block != -1 && end_block > final_block))
        {
            throw new IllegalRangeException();
        }
    }
}


