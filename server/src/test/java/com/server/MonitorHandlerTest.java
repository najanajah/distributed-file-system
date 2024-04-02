package com.server;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class MonitorHandlerTest {

    private static Thread serverThread = null;
    private static File file = null;
    private static String filePath = "test/monitor.txt";
    private static final int port = 8401;
    private static final String contents = "abcdefghi";
    private static final String contentToInsert = "xyz";
    private static final String insertedContent = "abcxyzdefghi";
    private static final String IPAddress = "127.0";

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException{
        serverThread = new Thread(() -> new Server(port,InvocationSemantics.AT_MOST_ONCE.getValue()).start());
        serverThread.start();

        Thread.sleep(2000);
        //Create the test file
        file = Paths.get(filePath).toFile();
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if(file.exists()) file.delete();
        file.createNewFile();

        //Write the contents
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(contents);
        bw.close();
    }

    @Test
    public void insert() throws Exception {
        int cbPort = 9000;

        Map<String,Object> p = new HashMap<>();
        p.put("time",System.currentTimeMillis());
        p.put("code", 3);
        p.put("path", filePath);
        p.put("duration", 10000L);
        p.put("port", cbPort);

        byte[] b = Util.marshal(p);

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

        Map<String,Object> response = Util.unmarshal(data);

        assertEquals(1, (int) (Integer) response.get("status"));
        assertNotNull(response.get("message"));
        Long expiration = (Long)response.get("end");
        assertTrue(System.currentTimeMillis() < expiration);
        assertTrue(expiration <= System.currentTimeMillis() + 10000L);

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                Map<String,Object> p1 = new HashMap<String,Object>();
                p1.put("time",System.currentTimeMillis());
                p1.put("code", 2);
                p1.put("offset", 3);
                p1.put("path", filePath);
                p1.put("insertion", contentToInsert);
                byte[] b1 = Util.marshal(p1);

                DatagramSocket dgs1 = new DatagramSocket();
                InetAddress serverAddr1 = InetAddress.getLocalHost();
                DatagramPacket request1 =
                        new DatagramPacket(b1, b1.length, serverAddr1, port);
                dgs1.send(request1);


                System.out.println("Send to server: " + p1);

                byte[] buffer1 = new byte[1024];
                DatagramPacket reply1 =
                        new DatagramPacket(buffer1, buffer1.length);
                dgs1.receive(reply1);
                byte[] data1 = Arrays.copyOf(reply1.getData(), reply1.getLength());

                Map<String,Object> response1 = Util.unmarshal(data1);

                assertEquals(1, (int) (Integer) response1.get("status"));
                assertNotNull(response1.get("message"));

                File file = Paths.get(filePath).toFile();
                Scanner fileScanner = new Scanner(file);
                String content = fileScanner.useDelimiter("\\Z").next();
                assertEquals(insertedContent, content);
                fileScanner.close();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }).start();


        DatagramSocket cbSoc = new DatagramSocket(cbPort);

        buffer = new byte[1024];
        reply =
                new DatagramPacket(buffer,buffer.length);
        cbSoc.receive(reply);
        data = Arrays.copyOf(reply.getData(), reply.getLength());

        response = Util.unmarshal(data);

        assertEquals(1, (int) (Integer) response.get("status"));

        long modTime = (Long)response.get("time");
        assertTrue(modTime <= System.currentTimeMillis());

        String monitorPath = (String)response.get("path");
        assertEquals(monitorPath, filePath);

        String modifier = (String)response.get("modifier");
        assertTrue(modifier.startsWith(IPAddress));

        String content = (String)response.get("content");
        assertEquals(insertedContent, content);
        cbSoc.close();
    }

    @AfterAll
    public static void tearDown(){
        serverThread.interrupt();
		file.delete();
    }

}