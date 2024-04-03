package Services;

import Exceptions.AppException;
import Exceptions.CorruptMessageException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;

import Driver.Connection;
import Driver.Constants;
import Driver.Util;

public class Monitor extends Service {

    public Monitor(Connection r) {
        super(r);
        service_id = Constants.MONITOR_ID;
    }

    @Override
    public void act() throws IOException {

        // ask for user input
        String[] request_values = get_user_request_values();
        // in milliseconds
        int monitor_period = Integer.parseInt(request_values[1]);
        long monitor_start = System.currentTimeMillis();
        int monitor_request_id = connection.get_request_id();
        // long endTime = monitor_start + (Long)monitor_period;

        if (monitor_period < 0) {
            System.out.println("Error: bad monitor period");
            return;
        }

        try {
            send_and_receive(request_values);
            byte[] update_bytes;
            connection.socket.setSoTimeout(monitor_period);
            // hacky way of waiting for a certain period for updates from server
            System.out.println("Start receiving updates: ");
            try {
                while(true) {
                    long current_time = System.currentTimeMillis();
                    if (current_time - monitor_start >= monitor_period) {
                        throw new SocketTimeoutException();
                    }
                    connection.socket.setSoTimeout((int) (monitor_period - (current_time - monitor_start)));
                    try {
                        // updates must have same request id as the original monitor request
                        update_bytes = Util.receive_message(monitor_request_id, connection);
                        // we know that service id is not needed here
                        Map<String, Object> update = Util.un_marshall(-1, update_bytes);
                        System.out.println("Update to file : " + request_values[0]);
                        System.out.println(update.get("content"));
                        System.out.println(Constants.END_OF_SERVICE);
                    }
                    catch (CorruptMessageException c) {
                        if (Constants.DEBUG) System.out.println("(log) Received corrupt message; Throwing away");
                    }
                }
                
            }
            catch (SocketTimeoutException t) {
                System.out.println("Done receiving updates.");
            }
        }
        catch (AppException ae) {
            System.out.println("Error: " + ae.getMessage() + ".");
        }

    }

}
