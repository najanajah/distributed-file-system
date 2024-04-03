package com.server.helper;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.server.helper.Util.readString;

public class TestUtil {
    public static List<Object> unmarshalReply(byte[] data) {
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

        // read the reply status
        int status = buffer.getInt();
        request.add(status);

        // read the reply status
        String content = readString(buffer);
        request.add(content);

        return request;
    }
}
