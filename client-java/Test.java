import Services.Service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import Driver.Connection;
import Driver.Constants;
import Driver.Util;



public class Test {

    public static void main(String[] args) {

        ByteBuffer buffer = ByteBuffer.allocate(8192);

        buffer.putChar('6'); 
        buffer.putInt(0); 
        // buffer.putInt(4)
        String value = "file"; 
        int length = value.length();
        byte[] stringBytes = ((String) value).getBytes(StandardCharsets.UTF_8);
        buffer.putInt(stringBytes.length); // Add string length as int
        buffer.put(stringBytes);

        System.out.println(" added int");
        buffer.flip();

        List<Byte> raw_content = new ArrayList<>();

        while (buffer.hasRemaining()) {
            raw_content.add(buffer.get());
        }

        byte[] packet = Util.to_primitive(raw_content);
        

        ByteBuffer Ubuffer = ByteBuffer.wrap(packet);
        Ubuffer.order(ByteOrder.BIG_ENDIAN); // Assuming big-endian byte order

        System.out.println("retrieved id " + (char)Ubuffer.getChar());
        System.out.println("retrieved rq_id " + Ubuffer.getInt());
        int len = Ubuffer.getInt(); 
        System.out.println("retrieved int size "  + len);
        byte[] filebyte = new byte[len];
        Ubuffer.get(filebyte);     
        System.out.println("retrieved filepath  " +  new String(filebyte, StandardCharsets.UTF_8));



        // // Example data for testing marshalling and unmarshalling
        // // int request_id = 123;
        // // int service_id = 1;
        // // String[] values = {"example_filepath", "100", "200"};

        // // // Marshalling
        // // List<List<Byte>> packets = Util.marshall(request_id, service_id, values);
        // // System.out.println("Marshalled packet len :");
        // // System.out.println(packets.size());
        // // // Displaying marshalled packets
        // // System.out.println("Marshalled packets:");
        // // for (List<Byte> packet : packets) {
        // //     System.out.println(packet);
        // // }

        // // Unmarshalling
        // // byte[] receivedContent = {0, 0, 0, 1, 0, 0, 0, 13, 72, 101, 108, 108, 111, 44, 32, 119, 111, 114, 108, 100, 33};
        // byte[] receivedContent = new byte[] {
        //     // Message Type (8 bits)
        //     0b00000001,
        //     // Request ID (32 bits)
        //     (byte)0x49, (byte)0x96, (byte)0x02, (byte)0xD2,
        //     // Status Code (32 bits)
        //     0x00, 0x00, 0x00, 0x01,
        //     // Data Length (32 bits)
        //     0x00, 0x00, 0x00, 0x0B,
        //     // Content (data_length bytes)
        //     (byte)0x48, (byte)0x65, (byte)0x6C, (byte)0x6C, (byte)0x6F, (byte)0x20, (byte)0x57, (byte)0x6F, (byte)0x72, (byte)0x6C, (byte)0x64
        // };

        // try {
        //     // Unmarshalling the received content
        //     Map<String, Object> message = Util.un_marshall(0, byteArrayToList(receivedContent));

        //     // Printing the unmarshalled message
        //     System.out.println("Received message:");
        //     System.out.println((message));
        //     // System.out.println("Status code: " + message.get("status_code"));
        //     // System.out.println("Data length: " + message.get("data_length"));
        //     // System.out.println("Content: " + new String((byte[]) message.get("content")));
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
    }

    private static List<Byte> byteArrayToList(byte[] byteArray) {
        List<Byte> byteList = new java.util.ArrayList<>(byteArray.length);
        for (byte b : byteArray) {
            byteList.add(b);
        }
        return byteList;
    }
}