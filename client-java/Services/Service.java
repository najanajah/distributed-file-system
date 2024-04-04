package Services;

import Exceptions.AppException;
import javafx.util.Pair;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import Driver.Connection;
import Driver.Constants;
import Driver.Util;

/**
 * Abstract class for services that this application can perform for the client
 * Sub-classes: Read, Write, Monitor, Clear, Trim
 */
public abstract class Service {

    public Connection connection;
    public int service_id;

    public Service(Connection r) {
        connection = r;
    }

    /** Perform the service
     * This basic method (shared by Write, Create, Remove, Trim, and Clear)
     * can be overridden for more involved services (Monitor, Read, Edit Time, List)
     * @throws IOException send/receive message
     */
    public void act() throws IOException {
        //ask for user input
        String[] request_values = get_user_request_values();
        try{
            send_and_receive(request_values);
            System.out.println("Done.");
        }
        catch(AppException ae) {
            System.out.println("Error: " + ae.getMessage() + ".");
        }
    }

    /** Generating for services
     * @param service_id the service requested
     * @param r connection info
     * @return the requested Service
     */
    public static Service generate_service(int service_id, Connection r) {
        if (service_id == Constants.READ_ID) {
            return new Read(r);
        }
        else if (service_id == Constants.WRITE_ID) {
            return new Write(r);
        }
        else if (service_id == Constants.MONITOR_ID) {
            return new Monitor(r);
        }
        else if (service_id == Constants.DUPLICATE_FILE_ID) {
            return new Duplicate(r);
        }
        else if (service_id == Constants.REMOVE_FILE_ID) {
            return new Remove(r);
        }
        else if (service_id == Constants.EDIT_TIME_ID) {
            return new EditTime(r);
        }
        else {
            return null;
        }
    }

    /** Wrapper for Util.send_and_receive
     * @param values parameter values for the service
     * @return the reply from the server, as a Map
     * @throws IOException send/receive message
     * @throws AppException BadPathnameException, BadRangeException, FileEmptyException
     */
    public Map<String, Object> send_and_receive(String[] values) throws IOException, AppException {
        // System.out.println(service_id);
        return Util.send_and_receive(service_id, values, connection);
    }

    /** Gets values required to perform Service from user
     * @return the values
     */
    public String[] get_user_request_values() {
        List<Pair<String, Integer>> params = Constants.get_request_params(service_id);
        String[] ret = new String[params.size()];
        for(int i = 0; i < params.size(); i++) {
            Pair<String, Integer> p = params.get(i);
            String prompt =  "Please enter the " + p.getKey() + ": ";
            System.out.println(prompt);
            ret[i] = connection.scanner.nextLine();
        }
        return ret;
    }




}
