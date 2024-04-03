package com.server;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.*;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ReadHandlerTest {
    private Thread serverThread = null;
    private File file = null;
    private String filePath = "test/read.txt";
    private Integer offset = 3;
    private Integer numBytes = 5;
    private int port = 8888;
    String contents = "Test Reading";


    @Before
    public void setUp() throws IOException, InterruptedException{
        serverThread = new Thread(() -> new Server(port).start());
        serverThread.start();

        Thread.sleep(2000);
        //Create the test file
        file = Paths.get(filePath).toFile();
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
        char requestType = '1';
        int requestId = 256;
        p.add(filePath);
        p.add(offset);
        p.add(numBytes);

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

        List<Object> response = Util.unmarshal(data);

        assertEquals(1, (int) (Integer) response.get(3));
        assertNotNull(response.get(1));

        String content = (String)response.get(-1);
        assertEquals(content, contents);

    }

    @AfterAll
    public void tearDown(){
        serverThread.interrupt();
        file.delete();
    }
}
