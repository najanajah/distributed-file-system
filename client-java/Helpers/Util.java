package Helpers;

import Exceptions.ApplicationException;
import Exceptions.CorruptMessageException;
import javafx.util.Pair;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Helper functions
 */
public class Util {

    /** Sending and receiving a message for a specific service
     * @param service_id the service to be performed
     * @param values parameter values
     * @param runner server connection info
     * @return the reply from the server, in a Map
     * @throws IOException from sending/receiving packet
     * @throws ApplicationException BadPathnameException, BadRangeException, FilEmptyException
     */
    public static Map<String, Object> send_and_receive(int service_id, String[] values, Runner runner) throws IOException, ApplicationException {
        if (Constants.DEBUG) System.out.println("(log) Begin send/receive for service id " + service_id + ":");
        runner.socket.setSoTimeout(Constants.TIMEOUT);
        List<Byte> reply_content;
        List<List<Byte>> request = Util.marshall(runner.get_request_id(), service_id, values);
        Util.send_message(request, runner);
        while(true) {
            try {
                reply_content = Util.receive_message(runner.get_request_id(), runner);
                break;
            }
            catch (SocketTimeoutException t) {
                if (Constants.DEBUG) System.out.println("(log) Socket timeout; Resending message");
                Util.send_message(request, runner);
            }
            catch (CorruptMessageException c) {
                if (Constants.DEBUG) System.out.println("(log) Throwing away corrupt message");
            }
        }

        try {
            Map<String, Object> reply = Util.un_marshall(service_id, reply_content);
            return reply;
        }
        finally {
            if (runner.at_most_once) {
                // upon receiving, send acknowledgment
                System.out.print("(acknowledgment) ");
                List<List<Byte>> ack = Util.marshall(runner.get_request_id(), Constants.ACKNOWLEDGMENT_ID, new String[0]);
                Util.send_message(ack, runner);
            }
            if (Constants.DEBUG) System.out.println("(log) Finished send/receive for service id " + service_id + ".");
            runner.increment_request_id();
        }

    }

    /** For marshalling requests
     * @param request_id unique per request sent
     * @param service_id service to be performed
     * @param values parameter values
     * @return packets to be sent, as a List of Lists of Bytes
     */
    public static List<List<Byte>> marshall(int request_id, int service_id, String[] values) {
        List<Pair<String, Integer>> params = Constants.get_request_params(service_id);
        List<Byte> raw_content = marshall_to_content(service_id, params, values);
        List<List<Byte>> message = marshall_to_packets(service_id , request_id, raw_content);
        return message;
    }

    /** Send an entire message (which can contain many packets) to the server
     * @param message message to be sent
     * @throws IOException from sending packet
     */
    public static void send_message(List<List<Byte>> message, Runner runner) throws IOException{
        for (List<Byte> packet : message) {
            runner.send_packet(packet);
        }
        if (Constants.DEBUG) System.out.println("(log) Message sent");
    }

    /**Receive an entire message (which could contain many packets)
     * @param check_request_id check received request ids against this one
     * @param runner server connection info
     * @return the content portion of the message
     * @throws IOException from socket receive
     */

    public static List<Byte> receive_message(int check_request_id, Runner runner) throws IOException, CorruptMessageException {

        // int total_packets = -1;
        List<Byte> all_content = new ArrayList<>();
        // int current_packet = 0;
        int overall_content_size = 0;
        // while (total_packets == -1 || current_packet != total_packets) {
            byte[] packet = runner.receive_packet();
            int[] header = get_header(packet);
            int receive_request_id = header[1];
            // overall_content_size = header[1];
            // int fragment_number = header[2];
            // if (fragment_number != current_packet || receive_request_id > check_request_id) {
            //     throw new CorruptMessageException();
            // }
            // (hacky) blindly acknowledge old replies
            if (runner.at_most_once && receive_request_id < check_request_id) {
            // if (runner.at_most_once){  
                // send acknowledgment
                if (Constants.DEBUG) System.out.println("(log) Blindly acknowledging old request id " + check_request_id);
                List<List<Byte>> ack = Util.marshall(0, Constants.ACKNOWLEDGMENT_ID, new String[0]);
                Util.send_message(ack, runner);
                // current_packet--;
                // total_packets = -1;
            }
            // if (total_packets == -1) {
            //     total_packets = (int) Math.ceil(overall_content_size*1.0/Constants.MAX_PACKET_CONTENT_SIZE);
            // }
            add_byte_array(all_content, Arrays.copyOfRange(packet, Constants.PACKET_HEADER_SIZE, packet.length));
            // current_packet++;

        // }
        // hopefully all the content is just passed as a byte array 
        // all_content = all_content.subList(0, overall_content_size);
        if (Constants.DEBUG) System.out.println("(log) Message received");
        return all_content;
    }

    /** Unmarshall the message received from server
     * The message may indicate:
     *    1. successful service performed
     *    2. an error (ex. file doesn't exit)
     *    3. a notification (ex. file updated)
     * @param service_id the service we expected to be performed
     * @param raw_content message from server
     * @return the message, as a map
     * @throws ApplicationException BadPathnameException, BadRangeException, FilEmptyException
     */
    public static Map<String, Object> un_marshall(int service_id, List<Byte> raw_content) throws ApplicationException {
        
        int counter = 0;
        Map<String, Object> message = new HashMap<>();

        // Header 
        int message_type = raw_content.get(counter++); //CHAR_SIZE = 1 
        int request_id = bytes_to_int(raw_content.subList(counter, counter + Constants.INT_SIZE));
        counter += Constants.INT_SIZE;

        message.put("message_type", message_type);
        message.put("request_id", request_id);

        // Not needed - payload length 
        // int payload_len = bytes_to_int(raw_content.subList(counter, counter + Constants.INT_SIZE));
        // counter += Constants.INT_SIZE;

        int status_code = bytes_to_int(raw_content.subList(counter, counter + Constants.INT_SIZE));
        counter += Constants.INT_SIZE;
        message.put("status_code", status_code);

        // Extracting data_length
        int data_length = bytes_to_int(raw_content.subList(counter, counter + Constants.INT_SIZE));
        counter += Constants.INT_SIZE;
        message.put("data_length", data_length);

        // Extracting content
        List<Byte> contentBytes = raw_content.subList(counter, counter + data_length);
        counter+= data_length;
        String contentString = new String(to_primitive(contentBytes), StandardCharsets.UTF_8);


        if (status_code==Constants.SUCCESSFUL_STATUS_ID) 
            message.put("content", contentString);
        else 
            message.put("error_message", contentString);

        return message;

        // int status_id = bytes_to_int(raw_content.subList(counter, counter + Constants.INT_SIZE));
        // message.put("status_id", status_id);
        // counter += Constants.INT_SIZE;

        // List<Pair<String, Integer>> params;
        // // if the status is "successful", then the parameters of the message depend on the SERVICE ID given
        // // i.e. a successful read vs. successful write vs. successful monitor have different parameters
        // if (status_id == Constants.SUCCESSFUL_STATUS_ID) {
        //     params = Constants.get_successful_reply_params(service_id);
        // }
        // // if the status is not "successful", then the parameters of the message depend on the STATUS ID given
        // // i.e. one of the error messages or an update notification
        // // a status that is not "successful" is called an "alert"
        // else {
        //     int alert_id = status_id;
        //     ApplicationException.check_app_exception(alert_id);
        //     params = Constants.get_alert_reply_params(alert_id);
        // }

        // // hacky way of dealing with variable number of repetitions of parameters
        // int repeat = 1;
        // if (params.size() > 0 && params.get(0).getKey().equals("repeat")) {
        //     repeat = bytes_to_int(raw_content.subList(counter, counter + Constants.INT_SIZE));
        //     counter += Constants.INT_SIZE;
        //     // remove the "repeat" parameter
        //     params = params.subList(1, params.size());
        // }
        // message.put("repeat", repeat);

        // // match raw_content with params
        // for(int r = 0; r < repeat; r++) {
        //     for (Pair<String, Integer> param : params) {
        //         String param_name = param.getKey();
        //         if (repeat > 1) {
        //             param_name += " ";
        //             param_name += r;
        //         }
        //         int param_type = param.getValue();
        //         int i = bytes_to_int(raw_content.subList(counter, counter + Constants.INT_SIZE));
        //         counter += Constants.INT_SIZE;
        //         message.put(param_name, i);
        //         if (param_type == Constants.STRING_ID) {
        //             String s = new String(to_primitive(raw_content.subList(counter, counter + i)));
        //             counter += i;
        //             message.put(param_name, s);
        //         }
        //     }
        // }

        // return message;
    }

    /** Convert List of Bytes to Array of Bytes
     * @param in List of Bytes
     * @return Array of Bytes
     */
    public static byte[] to_primitive(List<Byte> in) {
        byte[] ret = new byte[in.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = in.get(i);
        }
        return ret;
    }


    private static int[] get_header(byte[] packet) {
        int[] header = new int[3];
        // message / service Id
        header[0] = bytes_to_int(Arrays.copyOfRange(packet, 0, 1));
        // request_id 
        header[1] = bytes_to_int(Arrays.copyOfRange(packet, 1, 5));
        // header[2] = bytes_to_int(Arrays.copyOfRange(packet, 8, 12));
        return header;
    }

    private static int bytes_to_int(byte[] bytes) {
        return ((bytes[3] & 0xFF) << 0) |
                ((bytes[2] & 0xFF) << 8) |
                ((bytes[1] & 0xFF) << 16 ) |
                ((bytes[0] & 0xFF) << 24 );
    }

    private static int bytes_to_int(List<Byte> bytes) {
        return ((bytes.get(3) & 0xFF) << 0) |
                ((bytes.get(2) & 0xFF) << 8) |
                ((bytes.get(1) & 0xFF) << 16 ) |
                ((bytes.get(0) & 0xFF) << 24 );
    }

    private static List<Byte> marshall_to_content(int service_id, List<Pair<String, Integer>> params, String[] values) {
        
        List<Byte> raw_content = new ArrayList<>();
        // raw_content = add_int(service_id, raw_content);
        for (int i = 0; i < params.size(); i++) {
            int param_type = params.get(i).getValue();
            // strings are preceded by their length
            if (param_type == Constants.STRING_ID) {
                // String value = values[i];
                // raw_content.addAll(add_int(value.length(), new ArrayList<>()));
                // raw_content.addAll(add_string(value, new ArrayList<>()));
                // need to add s?
                raw_content = add_int(values[i].length(), raw_content);
                raw_content = add_string(values[i], raw_content);
            }
            // ints NOT preceded by length
            else if (param_type == Constants.INT_ID) {
                // raw_content.addAll(add_int(Integer.parseInt(values[i]), new ArrayList<>()));
                raw_content = add_int(Integer.parseInt(values[i]), raw_content);
            }
        }
        System.out.println(raw_content);
        return raw_content;
    }
    private static char sidToChar(int sid){ 
        switch (sid) {
            case 1:
                return '1'; // read
            case 2:
                return '2'; // write 
            case 3:
                return '3'; // monitor
            case 4:
                return '4'; // duplicate 
            case 5:
                return '5'; // remove 
            case 6:
                return '6'; // get last edit time 
            default:
                throw new IllegalArgumentException("Invalid service id: " + sid);
        } 
    }

    private static List<List<Byte>> marshall_to_packets(int service_id, int request_id, List<Byte> raw_content) {
        int raw_content_size = raw_content.size();
        List<List<Byte>> message = new ArrayList<>();
        ByteBuffer  buffer = ByteBuffer.allocate(12 +raw_content_size);
        
        System.out.println(service_id);
        String serviceIdString = String.valueOf(service_id);
        char[] chars = serviceIdString.toCharArray();
        for (char c : chars) {
            buffer.put((byte) c);
        }
        char sid = sidToChar(service_id);
        buffer.putChar(sid); 
        buffer.putInt(request_id); 

        byte[] rawContentArray = new byte[raw_content_size];
        for (int i = 0; i < raw_content.size(); i++) {
            rawContentArray[i] = raw_content.get(i);
        }
        buffer.put(rawContentArray); 
        // buffer is flipped to prepare for reading 
        buffer.flip();
        int total_packets = (int) Math.ceil(raw_content_size * 1.0 / Constants.MAX_PACKET_CONTENT_SIZE);
        // List<List<Byte>> message = new ArrayList<>();
        // for (int fragment = 0; fragment < total_packets; fragment++) {
        //     List<Byte> packet = new ArrayList<>();
        //     // packet = add_int(request_id, packet);
        //     packet = add_int(raw_content_size, packet);
        //     packet = add_int(fragment, packet);
        //     int begin_index = fragment * Constants.MAX_PACKET_CONTENT_SIZE;
        //     int end_index;
        //     if (fragment == total_packets - 1) {
        //         end_index = raw_content_size;
        //     }
        //     else {
        //         end_index = (fragment+1) * Constants.MAX_PACKET_CONTENT_SIZE;
        //     }
        //     packet.addAll(raw_content.subList(begin_index, end_index));
        //     message.add(packet);
        // }
        if (total_packets==1){ 
            byte[] bytes = buffer.array(); 
            List<Byte> packet = new ArrayList<>(); 
            for(byte b :bytes){ 
                packet.add(b); 
            }
            message.add(packet); 
        } else { 
            System.out.println("More than 1 Packet required increase max packet size");
        }
        
        return message;
    }

    // big-endian
    private static List<Byte> add_int(int num, List<Byte> in) {
        byte[] bytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            bytes[4-i-1] = (byte) (num >>> (i*8));
        }
        return add_byte_array(in, bytes);
    }

    private static List<Byte> add_string(String str, List<Byte> in) {
        byte[] bytes = str.getBytes();
        return add_byte_array(in, bytes);
    }

    private static List<Byte> add_byte_array(List<Byte> in, byte[] add) {
        for (byte b : add) {
            in.add(b);
        }
        return in;
    }

    // FOR DEBUGGING PURPOSES ONLY
    // private static List<List<Byte>> marshall_reply(int request_id, int service_id, String[] values) {
    //     List<Pair<String, Integer>> params = Constants.get_successful_reply_params(service_id);
    //     List<Byte> raw_content = marshall_to_content(service_id, params, values);
    //     List<List<Byte>> message = marshall_to_packets(request_id, raw_content);
    //     return message;
    // }
    


}
