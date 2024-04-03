package com.server;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.server.Util.readString;

public class TestUtil {
    public static List<Object> unmarshalReply(byte[] data)  {
        // big-endian default
        ByteBuffer buffer = ByteBuffer.wrap(data);
        List<Object> request = new ArrayList<>();

        if (buffer.remaining() < 1 + Integer.BYTES) {
            throw new IllegalArgumentException("Insufficient data for header");
        }

        // Read the message type from the header and convert to char
        char messageType = (char) buffer.get();
        request.add(messageType);

        // Read the request ID from the header
        int requestId = buffer.getInt();
        request.add(requestId);

        // Read the reply status
        int status = buffer.getInt();
        request.add(status);

        // Read the reply status
        String content = readString(buffer);
        request.add(content);



//        try {
//            switch (messageType) {
//                case '1': // read
//                    Collections.addAll(request, readString(buffer), readInt(buffer), readInt(buffer));
//                    break;
//                case '2': // insert
//                    Collections.addAll(request, readString(buffer), readInt(buffer), readString(buffer));
//                    break;
//                case '3': // monitor
//                    Collections.addAll(request, readString(buffer), readLong(buffer), readInt(buffer));
//                    break;
//                case '4': // get metadata
//                    Collections.addAll(request, readString(buffer));
//                    break;
//                case '5': // duplicate
//                    Collections.addAll(request, readString(buffer), readString(buffer));
//                    break;
//                default:
//                    throw new IllegalArgumentException("Unrecognized message type: " + messageType);
//            }
//        } catch (BufferUnderflowException e) {
//            throw new IllegalArgumentException("Incomplete data for message type " + messageType, e);
//        }

        return request;
    }
}
