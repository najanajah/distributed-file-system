package Driver;

import javafx.util.Pair;

import java.util.List;
import java.util.Map;

public class Constants {

    // Size
    public static final int INT_SIZE = 4;
    public static final int MAX_PACKET_SIZE = 1024;
    public static final int PACKET_HEADER_SIZE = 5;
    public static final int MAX_PACKET_CONTENT_SIZE = MAX_PACKET_SIZE - PACKET_HEADER_SIZE;
    public static final int FILE_BLOCK_SIZE = 1024;

    // Data ID
    public static final int INT_ID = 0;
    public static final int STRING_ID = 1;
    public static final int LONG_ID = 2;

    // Settings 
    public static final int TIMEOUT = 5000;
    public static final boolean DEBUG = true;

    // Service Codes
    public static final int EXIT_ID = 0;
    public static final String SERVICE_PROMPT =
    "+---------+------------------------+\n" +
    "|   CODE  |   Service              |\n" +
    "+---------+------------------------+\n" +
    "|   0     |   exit                 |\n" +
    "|   1     |   read                 |\n" +
    "|   2     |   insert               |\n" +
    "|   3     |   monitor              |\n" +
    "|   4     |   remove               |\n" +
    "|   5     |   duplicate            |\n" +
    "|   6     |   get last edit time   |\n" +
    "+---------+------------------------+\n" +
    "Please enter a service code: \n";
    public static final String END_OF_SERVICE =
    "+---------------+---------------+\n" +
    "|   END OF SERVICE             |\n" +
    "+---------------+---------------+";
    public static final String REPLY_SEPERATOR =
    "\n+---------------+---------------+\n";
    public static final String REQUEST_SUCCESSFUL_MSG = "Request was Successful!\n";
    public static final String REQUEST_FAILED_MSG = "Request was Failed!\n";

    public static final int READ_ID = 1;
    public static final int WRITE_ID = 2;
    public static final int MONITOR_ID = 3;
    public static final int DUPLICATE_FILE_ID = 5; 
    public static final int REMOVE_FILE_ID = 4;
    public static final int EDIT_TIME_ID = 6;
    public static final int ACKNOWLEDGMENT_ID = 7;

    public static final int LIST_ID = 10;

    // Parameters for requests
    public static final List<Pair<String, Integer>> READ_REQUEST_PARAMS =
            List.of(new Pair<>("pathname", STRING_ID), new Pair<>("offset", INT_ID), new Pair<>("byte_count", INT_ID));
    public static final List<Pair<String, Integer>> WRITE_REQUEST_PARAMS =
            List.of(new Pair<>("pathname", STRING_ID), new Pair<>("offset", INT_ID), new Pair<>("content", STRING_ID));
    public static final List<Pair<String, Integer>> MONITOR_REQUEST_PARAMS =
            List.of(new Pair<>("pathname", STRING_ID), new Pair<>("monitor_time", LONG_ID));
    public static final List<Pair<String, Integer>> DUPLICATE_REQUEST_PARAMS =
            List.of(new Pair<>("pathname", STRING_ID));

    public static final List<Pair<String, Integer>> REMOVE_FILE_REQUEST_PARAMS =
            List.of(new Pair<>("pathname", STRING_ID));

    public static final List<Pair<String, Integer>> EDIT_TIME_REQUEST_PARAMS =
            List.of(new Pair<>("pathname", STRING_ID));
    public static final List<Pair<String, Integer>> ACKNOWLEDGE_PARAMS =
            List.of();


    public static final Map<Integer, List<Pair<String, Integer>>> REQUEST_PARAMS;
    static{
        REQUEST_PARAMS = Map.of(
                READ_ID, READ_REQUEST_PARAMS,
                WRITE_ID, WRITE_REQUEST_PARAMS,
                MONITOR_ID, MONITOR_REQUEST_PARAMS,
                DUPLICATE_FILE_ID, DUPLICATE_REQUEST_PARAMS,
                REMOVE_FILE_ID, REMOVE_FILE_REQUEST_PARAMS,
                EDIT_TIME_ID, EDIT_TIME_REQUEST_PARAMS,
                ACKNOWLEDGMENT_ID, ACKNOWLEDGE_PARAMS

        );
    }


    public static final int SUCCESSFUL_STATUS_ID = 1;
    public static final List<Pair<String, Integer>> READ_REPLY_PARAMS =
            List.of(new Pair<>("status", INT_ID), new Pair<>("content", STRING_ID));
    public static final List<Pair<String, Integer>> WRITE_REPLY_PARAMS =
            List.of(new Pair<>("status", INT_ID), new Pair<>("content", STRING_ID));
    public static final List<Pair<String, Integer>> MONITOR_REPLY_PARAMS =
            List.of(new Pair<>("status", INT_ID), new Pair<>("content", STRING_ID));
    public static final List<Pair<String, Integer>> DUPLICATE_REPLY_PARAMS =
            List.of(new Pair<>("status", INT_ID), new Pair<>("content", STRING_ID));        

    public static final List<Pair<String, Integer>> EDIT_TIME_REPLY_PARAMS =
            List.of(new Pair<>("status", INT_ID), new Pair<>("time", INT_ID));

    public static final List<Pair<String, Integer>> REMOVE_REPLY_PARAMS =
           List.of(new Pair<>("status", INT_ID), new Pair<>("content", STRING_ID));


    public static final Map<Integer, List<Pair<String, Integer>>> SUCCESSFUL_STATUS_PARAMS;
    static{
        SUCCESSFUL_STATUS_PARAMS = Map.of(
                READ_ID, READ_REPLY_PARAMS,
                WRITE_ID, WRITE_REPLY_PARAMS,
                MONITOR_ID, MONITOR_REPLY_PARAMS,
                DUPLICATE_FILE_ID, DUPLICATE_REPLY_PARAMS,
                EDIT_TIME_ID, EDIT_TIME_REPLY_PARAMS,
                REMOVE_FILE_ID, REMOVE_REPLY_PARAMS
               
        );
    }


    public static List<Pair<String, Integer>> get_request_params(int service_id) {
        return REQUEST_PARAMS.get(service_id);
    }
    public static List<Pair<String, Integer>> get_successful_reply_params(int service_id) {
        return SUCCESSFUL_STATUS_PARAMS.get(service_id);
    }

}
