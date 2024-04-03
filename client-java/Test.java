import Helpers.Constants;
import Helpers.Connection;
import Services.Service;
import Helpers.Util;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;



public class Test {

    public static void main(String[] args) {
        // Example data for testing marshalling and unmarshalling
        // int request_id = 123;
        // int service_id = 1;
        // String[] values = {"example_filepath", "100", "200"};

        // // Marshalling
        // List<List<Byte>> packets = Util.marshall(request_id, service_id, values);
        // System.out.println("Marshalled packet len :");
        // System.out.println(packets.size());
        // // Displaying marshalled packets
        // System.out.println("Marshalled packets:");
        // for (List<Byte> packet : packets) {
        //     System.out.println(packet);
        // }

        // Unmarshalling
        // byte[] receivedContent = {0, 0, 0, 1, 0, 0, 0, 13, 72, 101, 108, 108, 111, 44, 32, 119, 111, 114, 108, 100, 33};
        byte[] receivedContent = new byte[] {
            // Message Type (8 bits)
            0b00000001,
            // Request ID (32 bits)
            (byte)0x49, (byte)0x96, (byte)0x02, (byte)0xD2,
            // Status Code (32 bits)
            0x00, 0x00, 0x00, 0x01,
            // Data Length (32 bits)
            0x00, 0x00, 0x00, 0x0B,
            // Content (data_length bytes)
            (byte)0x48, (byte)0x65, (byte)0x6C, (byte)0x6C, (byte)0x6F, (byte)0x20, (byte)0x57, (byte)0x6F, (byte)0x72, (byte)0x6C, (byte)0x64
        };

        try {
            // Unmarshalling the received content
            Map<String, Object> message = Util.un_marshall(0, byteArrayToList(receivedContent));

            // Printing the unmarshalled message
            System.out.println("Received message:");
            System.out.println((message));
            // System.out.println("Status code: " + message.get("status_code"));
            // System.out.println("Data length: " + message.get("data_length"));
            // System.out.println("Content: " + new String((byte[]) message.get("content")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Byte> byteArrayToList(byte[] byteArray) {
        List<Byte> byteList = new java.util.ArrayList<>(byteArray.length);
        for (byte b : byteArray) {
            byteList.add(b);
        }
        return byteList;
    }
}