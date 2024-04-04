package com.server.helper;

import com.server.config.ServerConfig;
import com.server.constant.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.DatatypeConverter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Util {
    static Logger logger = LogManager.getLogger(Util.class.getName());
    public static int lostReplyCount = ServerConfig.LOST_REPLY_COUNT;
    public static int replyDelaySec = ServerConfig.REPLY_DELAY_SEC;

    // construct message response for successful operation
    public static List<Object> successPacket(String msg) {
        List<Object> successPacket = new ArrayList<>();
        successPacket.add(1);
        successPacket.add(msg);
        return successPacket;
    }

    // error message for unsuccessful unmarshalling
    public static String failUnMarshalMsg(byte[] data) {
        return "Fail to Unmarshal 0x" + DatatypeConverter.printHexBinary(data);
    }

    // error message for invalid file path
    public static String invalidPathMsg(String path) {
        return "Invalid Path " + path;
    }

    // error message for file not exist
    public static String nonExistFileMsg(String file) {
        return "File on path " + file + " does not exist";
    }

    // send the reply to the ip and port address
    public static void sendPacket(InetAddress address, int port, char messageType, int requestId, List<Object> response) {
        try (DatagramSocket dgs = new DatagramSocket()) {
            byte[] data = marshal(messageType, requestId, response);
            DatagramPacket reply = new DatagramPacket(data, data.length, address, port);

            // simulate the scenario where replies are lost
            if (lostReplyCount > 0) {
                lostReplyCount--;
                logger.info("(Lost) Reply to " + address.toString() + " at port " + port + " contents: " + response);
            } else {
                Thread.sleep(replyDelaySec * 1000L);
                dgs.send(reply);
                logger.info("Reply to " + address.toString() + " at port " + port + " contents: " + response);
            }
        } catch (SocketException e) {
            logger.error(e.getMessage());
        } catch (Exception e) {
            logger.fatal(e.getMessage());
            e.printStackTrace();
        }
    }

    // construct the reply for failed operations with messages
    public static List<Object> errorPacket(String msg) {
        List<Object> errorMsg = new ArrayList<>();
        errorMsg.add(0);
        errorMsg.add(msg);
        return errorMsg;
    }

    // marshal the replies
    public static byte[] marshal(char messageType, int requestId, List<Object> parameters) {
        // initial size
        ByteBuffer buffer = ByteBuffer.allocate(Constants.MAX_PACKET_SIZE);

        // add the common header i.e. messageType (char) and requestId (int)
        buffer.put((byte) messageType);
        buffer.putInt(requestId);

        // marshal other parameters based on their type i.e. string, int, long
        for (Object arg : parameters) {
            if (arg instanceof String) {
                // write string length followed by string bytes
                byte[] stringBytes = ((String) arg).getBytes(StandardCharsets.UTF_8);
                buffer.putInt(stringBytes.length);
                buffer.put(stringBytes);
            } else if (arg instanceof Integer) {
                // indicate integer type and write the integer
                buffer.putInt((Integer) arg);
            } else if (arg instanceof Long) {
                buffer.putLong((Long) arg);
            } else {
                // handle other types or throw an exception
                throw new IllegalArgumentException("Unsupported argument type: " + arg.getClass());
            }
        }

        buffer.flip();

        byte[] data = new byte[buffer.limit()];
        buffer.get(data);
        return data;
    }

    // unmarshal the requests
    public static List<Object> unmarshal(byte[] data) {
        // big-endian default
        ByteBuffer buffer = ByteBuffer.wrap(data);
        List<Object> request = new ArrayList<>();

        // check if the common header is presented
        if (buffer.remaining() < 1 + Integer.BYTES) {
            throw new IllegalArgumentException("Insufficient data for header");
        }

        // read the messageType from the header and convert to char
        char messageType = (char) buffer.get();
        request.add(messageType);

        // read the requestID from the header
        int requestId = buffer.getInt();
        request.add(requestId);

        // unmarshal the other parameters based on the predefined formats for each service
        try {
            switch (messageType) {
                case Constants.REQUEST_CODE_READ: // read
                    Collections.addAll(request, readString(buffer), readInt(buffer), readInt(buffer));
                    break;
                case Constants.REQUEST_CODE_INSERT: // insert
                    Collections.addAll(request, readString(buffer), readInt(buffer), readString(buffer));
                    break;
                case Constants.REQUEST_CODE_MONITOR: // monitor
                    Collections.addAll(request, readString(buffer), readLong(buffer));
                    break;
                case Constants.REQUEST_CODE_DELETE: // get delete
                case Constants.REQUEST_CODE_GET_LAST_MODIFICATION_TIME: // get modificationTime
                case Constants.REQUEST_CODE_DUPLICATE: // duplicate
                    Collections.addAll(request, readString(buffer));
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized message type: " + messageType);
            }
        } catch (BufferUnderflowException e) {
            throw new IllegalArgumentException("Incomplete data for message type " + messageType, e);
        }

        return request;
    }

    // parse the string based on the structure of | stringLength (int) | content (unfixed length) |
    // the first 32 bits (int) indicate the length of the string content
    // with that length, the content can be extracted
    public static String readString(ByteBuffer buffer) {
        try {
            int filePathLen = buffer.getInt();
            // check for negative length or length longer than remaining buffer
            if (filePathLen < 0 || filePathLen > buffer.remaining()) {
                throw new IllegalArgumentException("Invalid string length: " + filePathLen);
            }
            byte[] filePathBytes = new byte[filePathLen];
            buffer.get(filePathBytes);
            return new String(filePathBytes, StandardCharsets.UTF_8);
        } catch (BufferUnderflowException e) {
            // this exception is thrown if there aren't enough bytes in the buffer
            throw new IllegalArgumentException(Constants.INSUFFICIENT_DATA_ERROR_MSG + " for string", e);
        }
    }

    // parse the int type
    private static int readInt(ByteBuffer buffer) {
        try {
            if (buffer.remaining() < Integer.BYTES) {
                throw new IllegalArgumentException(Constants.INSUFFICIENT_DATA_ERROR_MSG + " for integer");
            }
            return buffer.getInt();
        } catch (BufferUnderflowException e) {
            // this exception is thrown if there aren't enough bytes to read the data type or the number
            throw new IllegalArgumentException(Constants.INSUFFICIENT_DATA_ERROR_MSG, e);
        }
    }

    // parse the long type
    private static Long readLong(ByteBuffer buffer) {
        try {
            if (buffer.remaining() < Long.BYTES) {
                throw new IllegalArgumentException(Constants.INSUFFICIENT_DATA_ERROR_MSG + " for long");
            }
            return buffer.getLong();
        } catch (BufferUnderflowException e) {
            // this exception is thrown if there aren't enough bytes to read the data type or the number
            throw new IllegalArgumentException(Constants.INSUFFICIENT_DATA_ERROR_MSG, e);
        }
    }
}
