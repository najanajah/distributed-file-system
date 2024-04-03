package Driver;

import Exceptions.AppException;
import Exceptions.CorruptMessageException;
import javafx.util.Pair;
import Driver.Constants;

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
     * @param connection server connection info
     * @return the reply from the server, in a Map
     * @throws IOException from sending/receiving packet
     * @throws AppException BadPathnameException, BadRangeException, FilEmptyException
     */
    public static Map<String, Object> send_and_receive(int service_id, String[] values, Connection connection) throws IOException, AppException {
        if (Constants.DEBUG) System.out.println("(log) Begin send/receive for service id " + service_id + "and req id " + connection.get_request_id());
        connection.socket.setSoTimeout(Constants.TIMEOUT);
        byte[] reply_content;
        List<List<Byte>> request = Util.marshall(connection.get_request_id(), service_id, values);
        Util.send_message(request, connection);
        while(true) {
            try {
                reply_content = Util.receive_message(connection.get_request_id(), connection);
                break;
            }
            catch (SocketTimeoutException t) {
                if (Constants.DEBUG) System.out.println("(log) Socket timeout; Resending message");
                Util.send_message(request, connection);
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
            // if (connection.at_most_once) {
            //     // upon receiving, send acknowledgment
            //     System.out.print("(acknowledgment) ");
            //     List<List<Byte>> ack = Util.marshall(connection.get_request_id(), Constants.ACKNOWLEDGMENT_ID, new String[0]);
            //     Util.send_message(ack, connection);
            // }
            if (Constants.DEBUG) System.out.println("(log) Finished send/receive for service id " + service_id + ".");
            connection.increment_request_id();
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
        System.out.println("params" + params);
        List<List<Byte>>  message = marshall_to_content(service_id, request_id, params, values);
        // List<List<Byte>> message = marshall_to_packets(service_id , request_id, raw_content);
        return message;
    }

    /** Send an entire message (which can contain many packets) to the server
     * @param message message to be sent
     * @throws IOException from sending packet
     */
    public static void send_message(List<List<Byte>> message, Connection connection) throws IOException{
        for (List<Byte> packet : message) {
            connection.send_packet(packet);
        }
        if (Constants.DEBUG) System.out.println("(log) Message sent");
    }

    /**Receive an entire message (which could contain many packets)
     * @param check_request_id check received request ids against this one
     * @param connection server connection info
     * @return the content portion of the message
     * @throws IOException from socket receive
     */
    // public static List<Byte> receiveMessageMonitor(int request_id, Connection connection) throws IOException, CorruptMessageException {
    //     List<Byte> all_content = new ArrayList<>();

    //     System.out.println("Trying to receive packet"); 
    //     byte[] packet = connection.receive_packet();
    //     ByteBuffer buffer = ByteBuffer.wrap(packet);
    //     // get char message_id
    //     buffer.get();
    //     buffer.getInt();

    //     while (buffer.hasRemaining()) {
    //             all_content.add(buffer.get());
    //     }
    //     // if (Constants.DEBUG) System.out.println("(log) Message received");
    //     return packet;
    // }
    public static byte[] receive_message(int request_id, Connection connection) throws IOException, CorruptMessageException {

        // List<Byte> all_content = new ArrayList<>();
    
        System.out.println("Trying to receive packet"); 
        byte[] packet = connection.receive_packet();
        // ByteBuffer buffer = ByteBuffer.wrap(packet);
        // get char message_id
        // buffer.get();
        // buffer.getInt();

        // while (buffer.hasRemaining()) {
        //     all_content.add(buffer.get());
        // }
        // System.out.println("getting header"); 
        // Object[] header = get_header(packet);
        // int receive_request_id = (int) header[1];
        // if (connection.at_most_once && receive_request_id < request_id) {
        //     // if (connection.at_most_once){  
        //         // send acknowledgment
        //         if (Constants.DEBUG) System.out.println("(log) Blindly acknowledging old request id " + request_id);
                // List<List<Byte>> ack = Util.marshall(0, Constants.ACKNOWLEDGMENT_ID, new String[0]);
                // Util.send_message(ack, connection);
        //         // current_packet--;
        //         // total_packets = -1;
        // }
        //     // if (total_packets == -1) {
        //     //     total_packets = (int) Math.ceil(overall_content_size*1.0/Constants.MAX_PACKET_CONTENT_SIZE);
        //     // }
        //     add_byte_array(all_content, Arrays.copyOfRange(packet, Constants.PACKET_HEADER_SIZE, packet.length));
        //     // current_packet++;

        // }
        // hopefully all the content is just passed as a byte array 
        // all_content = all_content.subList(0, overall_content_size);
        // if (Constants.DEBUG) System.out.println("(log) Message received");
        return packet;
    }

    /** Unmarshall the message received from server
     * The message may indicate:
     *    1. successful service performed
     *    2. an error (ex. file doesn't exit)
     *    3. a notification (ex. file updated)
     * @param service_id the service we expected to be performed
     * @param raw_content message from server
     * @return the message, as a map
     * @throws AppException BadPathnameException, BadRangeException, FilEmptyException
     */
    public static Map<String, Object> un_marshall(int service_id, byte[] raw_content) throws AppException {
    
        //  byte[] packet = Util.to_primitive(raw_content);
         ByteBuffer buffer = ByteBuffer.wrap(raw_content);
         Map<String, Object> message = new HashMap<>();

         if (buffer.remaining() < 1 + Integer.BYTES) {
             throw new IllegalArgumentException("Insufficient data for header");
         }
 
         // read the message type from the header and convert to char
         char messageType = (char) buffer.get();
         message.put("message_type", messageType);
 
         // read the request ID from the header
        int requestId = buffer.getInt();
        message.put("request_id", requestId);
 
         // read the reply status
        int status = buffer.getInt();
        message.put("status_code", status);
 
         // read the reply status
        try {
            int filePathLen = buffer.getInt();
            message.put("data_length" , filePathLen); 
            // Check for negative length or length longer than remaining buffer
            if (filePathLen < 0 || filePathLen > buffer.remaining()) {
                throw new IllegalArgumentException("Invalid string length: " + filePathLen);
            }
            byte[] filePathBytes = new byte[filePathLen];
            buffer.get(filePathBytes);
            String content = new String(filePathBytes, StandardCharsets.UTF_8);
            String key = (status==Constants.SUCCESSFUL_STATUS_ID)?"content" :"error_message"; 
            message.put(key, content);
         
        }catch (BufferUnderflowException e) {
            // This exception is thrown if there aren't enough bytes in the buffer
            throw new IllegalArgumentException("Not enough space in buffer for string", e);
        }
        // message.put("data_length", content);
        // // int counter = 0;
        // Map<String, Object> message = new HashMap<>();

        // // Header 
        // int message_type = raw_content.get(counter++); //CHAR_SIZE = 1 
        // int request_id = bytes_to_int(raw_content.subList(counter, counter + Constants.INT_SIZE));
        // counter += Constants.INT_SIZE;

        // message.put("message_type", message_type);
        // message.put("request_id", request_id);

        // Not needed - payload length 
        // int payload_len = bytes_to_int(raw_content.subList(counter, counter + Constants.INT_SIZE));
        // counter += Constants.INT_SIZE;

        // int status_code = bytes_to_int(raw_content.subList(counter, counter + Constants.INT_SIZE));
        // counter += Constants.INT_SIZE;
        // message.put("status_code", status_code);

        // // Extracting data_length
        // int data_length = bytes_to_int(raw_content.subList(counter, counter + Constants.INT_SIZE));
        // counter += Constants.INT_SIZE;
        // message.put("data_length", data_length);

        // // Extracting content
        // List<Byte> contentBytes = raw_content.subList(counter, counter + data_length);
        // counter+= data_length;
        // String contentString = new String(to_primitive(contentBytes), StandardCharsets.UTF_8);


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

    public static String readString(ByteBuffer buffer) {
        try {
            int filePathLen = buffer.getInt();
            // Check for negative length or length longer than remaining buffer
            if (filePathLen < 0 || filePathLen > buffer.remaining()) {
                throw new IllegalArgumentException("Invalid string length: " + filePathLen);
            }
            byte[] filePathBytes = new byte[filePathLen];
            buffer.get(filePathBytes);
            return new String(filePathBytes, StandardCharsets.UTF_8);
        } catch (BufferUnderflowException e) {
            // This exception is thrown if there aren't enough bytes in the buffer
            throw new IllegalArgumentException("Not enough buffer space" + " for string", e);
        }
    }


    private static Object[] get_header(byte[] packet) {
        Object[] header = new Object[Constants.PACKET_HEADER_SIZE];
        ByteBuffer buffer = ByteBuffer.wrap(packet);
        char service_id = (char) buffer.get();
        header[0] = service_id;
        int requestId = buffer.getInt();
        header[1] = requestId; 
        // message / service Id
        // header[0] = bytes_to_int(Arrays.copyOfRange(packet, 0, 1));
        // request_id 
        // header[1] = bytes_to_int(Arrays.copyOfRange(packet, 1, 5));
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

    private static List<List<Byte>>  marshall_to_content(int service_id, int request_id,  List<Pair<String, Integer>> params, String[] values) {
        
        List<Byte> raw_content = new ArrayList<>();
        
        System.out.println(service_id);
        char sid = sidToChar(service_id); 
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        buffer.put((byte) sid);
        buffer.putInt(request_id);

        for (int i = 0; i < params.size(); i++) {
            int param_type = params.get(i).getValue();
            if (param_type == Constants.STRING_ID) {
                // Write string length followed by string bytes
                byte[] stringBytes = ((String) values[i]).getBytes(StandardCharsets.UTF_8);
                buffer.putInt(stringBytes.length);
                buffer.put(stringBytes);
            } else if (param_type == Constants.INT_ID) {
                // Indicate integer type and write the integer
                // buffer.putInt((Integer) values[i]);
                int parsed = Integer.parseInt(values[i]); 
                System.out.println("Parsed integer " + parsed);
                buffer.putInt(parsed);
            } else if (param_type == Constants.LONG_ID) { 
                long parsed = Long.parseLong(values[i]); 
                System.out.println("Parsed Long " + parsed);
                buffer.putLong(parsed);
            }
            else {
                // Handle other types or throw an exception
                throw new IllegalArgumentException("Unsupported argument type: " );
            }
        }

        buffer.flip();
        

        while (buffer.hasRemaining()) {
            raw_content.add(buffer.get());
        }

        System.out.println("raw content" + raw_content);
        List<List<Byte>> listOfLists = new ArrayList<>();
        listOfLists.add(raw_content); // Add raw_content as the only element in the list
        return listOfLists;
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
            case 7:
                return '7'; // ack
            default:
                throw new IllegalArgumentException("Invalid service id: " + sid);
        } 
    }

    // private static List<List<Byte>> marshall_to_packets(int service_id, int request_id, List<Byte> raw_content) {
    //     int raw_content_size = raw_content.size();
    //     List<List<Byte>> message = new ArrayList<>();
    //     ByteBuffer  buffer = ByteBuffer.allocate(12 +raw_content_size);
        
    //     System.out.println(service_id);
    //     String serviceIdString = String.valueOf(service_id);
    //     char[] chars = serviceIdString.toCharArray();
    //     for (char c : chars) {
    //         buffer.put((byte) c);
    //     }
    //     char sid = sidToChar(service_id);
    //     buffer.putChar(sid); 
    //     buffer.putInt(request_id); 
        

    //     byte[] rawContentArray = new byte[raw_content_size];
    //     for (int i = 0; i < raw_content.size(); i++) {
    //         rawContentArray[i] = raw_content.get(i);
    //     }
    //     buffer.put(rawContentArray); 
    //     // buffer is flipped to prepare for reading 
    //     buffer.flip();
    //     int total_packets = (int) Math.ceil(raw_content_size * 1.0 / Constants.MAX_PACKET_CONTENT_SIZE);
    //     // List<List<Byte>> message = new ArrayList<>();
    //     // for (int fragment = 0; fragment < total_packets; fragment++) {
    //     //     List<Byte> packet = new ArrayList<>();
    //     //     // packet = add_int(request_id, packet);
    //     //     packet = add_int(raw_content_size, packet);
    //     //     packet = add_int(fragment, packet);
    //     //     int begin_index = fragment * Constants.MAX_PACKET_CONTENT_SIZE;
    //     //     int end_index;
    //     //     if (fragment == total_packets - 1) {
    //     //         end_index = raw_content_size;
    //     //     }
    //     //     else {
    //     //         end_index = (fragment+1) * Constants.MAX_PACKET_CONTENT_SIZE;
    //     //     }
    //     //     packet.addAll(raw_content.subList(begin_index, end_index));
    //     //     message.add(packet);
    //     // }
    //     if (total_packets==1){ 
    //         byte[] bytes = buffer.array(); 
    //         List<Byte> packet = new ArrayList<>(); 
    //         for(byte b :bytes){ 
    //             packet.add(b); 
    //         }
    //         message.add(packet); 
    //     } else { 
    //         System.out.println("More than 1 Packet required increase max packet size");
    //     }
        
    //     return message;
    // }

    // // big-endian
    // private static List<Byte> add_int(int num, List<Byte> in) {
    //     byte[] bytes = new byte[4];
    //     for (int i = 0; i < 4; i++) {
    //         bytes[4-i-1] = (byte) (num >>> (i*8));
    //     }
    //     return add_byte_array(in, bytes);
    // }

    // private static List<Byte> add_string(String str, List<Byte> in) {
    //     byte[] bytes = str.getBytes();
    //     return add_byte_array(in, bytes);
    // }

    // private static List<Byte> add_byte_array(List<Byte> in, byte[] add) {
    //     for (byte b : add) {
    //         in.add(b);
    //     }
    //     return in;
    // }

    // FOR DEBUGGING PURPOSES ONLY
    // private static List<List<Byte>> marshall_reply(int request_id, int service_id, String[] values) {
    //     List<Pair<String, Integer>> params = Constants.get_successful_reply_params(service_id);
    //     List<Byte> raw_content = marshall_to_content(service_id, params, values);
    //     List<List<Byte>> message = marshall_to_packets(request_id, raw_content);
    //     return message;
    // }
    


}