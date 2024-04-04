package Services;

import Exceptions.AppException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import Driver.Connection;
import Driver.Constants;

public class EditTime extends Service {

    public EditTime(Connection r) {
        super(r);
        service_id = Constants.EDIT_TIME_ID;
    }

    /** Perform the service
     * @throws IOException send/receive message
     */
    @Override
    public void act() throws IOException {
        //ask for user input
        String[] request_values = get_user_request_values();
        try{
            Map<String, Object> reply = send_and_receive(request_values);
            System.out.println(Constants.REPLY_SEPERATOR);

            // if request was successful
            if ((int) reply.get("status_code")==Constants.SUCCESSFUL_STATUS_ID) {
                System.out.println(Constants.REQUEST_SUCCESSFUL_MSG);
                System.out.println("Last modification time: ");

                // convert time of long type in millis to a human-friendly format
                long last_modified_time = Long.parseLong(reply.get("content").toString());
                LocalDateTime last_modified_date_time = LocalDateTime.ofEpochSecond(last_modified_time / 1000, 0, ZoneOffset.ofHours(8));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                System.out.println(last_modified_date_time.format(formatter));
            } else {
                // if the request was unsuccessfil
                System.out.println(Constants.REQUEST_FAILED_MSG);
                System.out.println(reply.get("error_message"));
            }
            System.out.println(Constants.REPLY_SEPERATOR);

            System.out.println(Constants.END_OF_SERVICE);
        }
        catch(AppException ae) {
            System.out.println("Error: " + ae.getMessage() + ".");
        }
    }
}
