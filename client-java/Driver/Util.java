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
 * Utility functions
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
  
    public static byte[] receive_message(int request_id, Connection connection) throws IOException, CorruptMessageException {


        System.out.println("Trying to receive packet"); 
        byte[] packet = connection.receive_packet();

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


        return message;

    }

    /** Convert List<Byte> to byte[]
     * @param in List<Byte>
     * @return byte[]
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

}
