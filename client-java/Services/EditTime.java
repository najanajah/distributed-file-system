package Services;

import Exceptions.AppException;

import java.io.IOException;
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
            System.out.println("Edit time:");
            System.out.println((int) reply.get("content"));
            System.out.println(Constants.END_OF_SERVICE);
        }
        catch(AppException ae) {
            System.out.println("Error: " + ae.getMessage() + ".");
        }
    }
}
