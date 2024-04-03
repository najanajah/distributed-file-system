package com.server.helper;

import com.server.constant.Constant;
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
    public static int lostReplyCount = 0;
    public static int replyDelaySec = 0;

    // construct message response for successful operation
    public static List<Object> successPacket(String msg) {
        List<Object> successPacket = new ArrayList<>();
        successPacket.add(1);
        successPacket.add(msg);
        return successPacket;
    }

    public static String failUnMarshalMsg(byte[] data) {
        return "Fail to Unmarshal 0x" + DatatypeConverter.printHexBinary(data);
    }

    public static String invalidPathMsg(String path) {
        return "Invalid Path " + path;
    }

    public static String nonExistFileMsg(String file) {
        return "File on path " + file + " does not exist";
    }

    // send the reply to the ip and port address
    public static void sendPacket(InetAddress address, int port, char messageType, int requestId, List<Object> response) {
        try (DatagramSocket dgs = new DatagramSocket()) {
            byte[] data = marshal(messageType, requestId, response);
            DatagramPacket reply = new DatagramPacket(data, data.length, address, port);

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

    public static byte[] marshal(char messageType, int requestId, List<Object> parameters) {
        // initial size
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        buffer.put((byte) messageType);
        buffer.putInt(requestId);

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

    public static List<Object> unmarshal(byte[] data) {
        // big-endian default
        ByteBuffer buffer = ByteBuffer.wrap(data);
        List<Object> request = new ArrayList<>();

        if (buffer.remaining() < 1 + Integer.BYTES) {
            throw new IllegalArgumentException("Insufficient data for header");
        }

        // read the message type from the header and convert to char
        char messageType = (char) buffer.get();
        request.add(messageType);

        // read the request ID from the header
        int requestId = buffer.getInt();
        request.add(requestId);

        try {
            switch (messageType) {
                case '1': // read
                    Collections.addAll(request, readString(buffer), readInt(buffer), readInt(buffer));
                    break;
                case '2': // insert
                    Collections.addAll(request, readString(buffer), readInt(buffer), readString(buffer));
                    break;
                case '3': // monitor
                    Collections.addAll(request, readString(buffer), readLong(buffer), readInt(buffer));
                    break;
                case '4': // get delete
                case '6': // get modificationTime
                    Collections.addAll(request, readString(buffer));
                    break;
                case '5': // duplicate
                    Collections.addAll(request, readString(buffer), readString(buffer));
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized message type: " + messageType);
            }
        } catch (BufferUnderflowException e) {
            throw new IllegalArgumentException("Incomplete data for message type " + messageType, e);
        }

        return request;
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
            throw new IllegalArgumentException(Constant.INSUFFICIENT_DATA_ERROR_MSG + " for string", e);
        }
    }

    private static int readInt(ByteBuffer buffer) {
        try {
            if (buffer.remaining() < Integer.BYTES) {
                throw new IllegalArgumentException(Constant.INSUFFICIENT_DATA_ERROR_MSG + " for integer");
            }
            return buffer.getInt();
        } catch (BufferUnderflowException e) {
            // This exception is thrown if there aren't enough bytes to read the data type or the number
            throw new IllegalArgumentException(Constant.INSUFFICIENT_DATA_ERROR_MSG, e);
        }
    }

    private static Long readLong(ByteBuffer buffer) {
        try {
            if (buffer.remaining() < Long.BYTES) {
                throw new IllegalArgumentException(Constant.INSUFFICIENT_DATA_ERROR_MSG + " for long");
            }
            return buffer.getLong();
        } catch (BufferUnderflowException e) {
            // This exception is thrown if there aren't enough bytes to read the data type or the number
            throw new IllegalArgumentException(Constant.INSUFFICIENT_DATA_ERROR_MSG, e);
        }
    }
}
