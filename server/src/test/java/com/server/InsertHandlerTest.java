package com.server;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InsertHandlerTest {
    private Thread serverThread = null;
    private File file = null;
    private String filePath = "test/insert.txt";
    private int port = 8603;
    String contents = "Test Inserting";
    private Integer offset = 0;
    private String insertedContent = "Stop ";


    @Before
    public void setUp() throws IOException, InterruptedException{
        serverThread = new Thread(() -> new Server(port).start());
        serverThread.start();
        Thread.sleep(2000);
        //Create the test file

        this.file = Paths.get(filePath).toFile();
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if(file.exists()) file.delete();
        file.createNewFile();


        //Write the contents
        BufferedWriter bw = new BufferedWriter(new FileWriter(this.file));
        bw.write(contents);
        bw.close();
    }


    @Test
    public void test() throws Exception {

        List<Object> p = new ArrayList<>();
        char requestType = '2';
        int requestId = 233;
        p.add(filePath);
        p.add(offset);
        p.add(insertedContent);
        byte[] b = Util.marshal(requestType, requestId, p);

        DatagramSocket dgs = new DatagramSocket();
        InetAddress serverAddr = InetAddress.getLocalHost();
        DatagramPacket request =
                new DatagramPacket(b, b.length, serverAddr, port);
        dgs.send(request);


        System.out.println("Send to server: " + p);

        byte[] buffer = new byte[1024];
        DatagramPacket reply =
                new DatagramPacket(buffer,buffer.length);
        dgs.receive(reply);
        byte[] data = Arrays.copyOf(reply.getData(), reply.getLength());

        List<Object> response = TestUtil.unmarshalReply(data);
        System.out.println(response);

        assertEquals(requestType, (char) response.get(0));
        assertEquals(requestId, (int) response.get(1));
        assertEquals(1, (int) response.get(2));
        assertNotNull(response.get(3));

        this.file = Paths.get(filePath).toFile();
        Scanner fileScanner = new Scanner(file);
        String content = fileScanner.useDelimiter("\\Z").next();
        fileScanner.close();
        assertEquals(content, "Stop Test Inserting");

//
//        dgs.send(request);
//
//
//        System.out.println("Send to server: " + p);
//
//        buffer = new byte[1024];
//        reply =
//                new DatagramPacket(buffer,buffer.length);
//        dgs.receive(reply);
//        data = Arrays.copyOf(reply.getData(), reply.getLength());
//
//        response = Util.unmarshal(data);
//
//        assertTrue((Integer) response.get("status") == 1);
//        assertTrue(response.get("message") != null);
//
//        this.file = Paths.get(filePath).toFile();
//        fileScanner = new Scanner(file);
//        content = fileScanner.useDelimiter("\\Z").next();
//        assertTrue(content.equals("abczyxdefghi"));
//        fileScanner.close();
    }

    @AfterAll
    public void tearDown(){
        this.serverThread.interrupt();
        this.file.delete();
    }
}
