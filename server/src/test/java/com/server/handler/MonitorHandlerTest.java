package com.server.handler;

import com.server.Server;
import com.server.constant.Constants;
import com.server.helper.TestUtil;
import com.server.helper.Util;
import com.server.model.InvocationSemantics;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MonitorHandlerTest {

    private static Thread serverThread = null;
    private static File file = null;
    private static final String filePath = "test/monitor.txt";
    private static final int port = 8401;
    private static final String contents = "abcdefghi";
    private static final String contentToInsert = "xyz";
    private static final String insertedContent = "abcxyzdefghi";
    private static final int offset = 3;
    private static final Long interval = 10000L;
    private static final int callBackPort = 8897;

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        serverThread = new Thread(() -> new Server(port, InvocationSemantics.AT_MOST_ONCE.getValue()).start());
        serverThread.start();

        Thread.sleep(2000);
        //Create the test file
        file = Paths.get(filePath).toFile();
        if (file.getParentFile() != null && !file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (file.exists()) file.delete();
        file.createNewFile();

        //Write the contents
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(contents);
        bw.close();
    }

    @Test
    public void insert() throws Exception {
        List<Object> p = new ArrayList<>();
        char requestType = Constants.REQUEST_CODE_MONITOR;
        int requestId = 1;
        char insertRequestType = Constants.REQUEST_CODE_INSERT;
        int insertRequestId = 2;

        p.add(filePath);
        p.add(interval);
        p.add(callBackPort);

        byte[] b = Util.marshal(requestType, requestId, p);

        DatagramSocket dgs = new DatagramSocket();
        InetAddress serverAddr = InetAddress.getLocalHost();
        DatagramPacket request =
                new DatagramPacket(b, b.length, serverAddr, port);
        dgs.send(request);

        System.out.println("Send to server: " + p);

        byte[] buffer = new byte[Constants.MAX_PACKET_SIZE];
        DatagramPacket reply =
                new DatagramPacket(buffer, buffer.length);
        dgs.receive(reply);
        byte[] data = Arrays.copyOf(reply.getData(), reply.getLength());

        List<Object> response = TestUtil.unmarshalReply(data);

        assertEquals(1, (int) response.get(2));
        assertNotNull(response.get(3));

        // perform inserting
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                List<Object> p1 = new ArrayList<>();
                p1.add(filePath);
                p1.add(offset);
                p1.add(contentToInsert);

                byte[] b1 = Util.marshal(insertRequestType, insertRequestId, p1);

                DatagramSocket dgs1 = new DatagramSocket();
                InetAddress serverAddr1 = InetAddress.getLocalHost();
                DatagramPacket request1 =
                        new DatagramPacket(b1, b1.length, serverAddr1, port);
                dgs1.send(request1);

                System.out.println("Send to server: " + p1);

                byte[] buffer1 = new byte[Constants.MAX_PACKET_SIZE];
                DatagramPacket reply1 =
                        new DatagramPacket(buffer1, buffer1.length);
                dgs1.receive(reply1);
                byte[] data1 = Arrays.copyOf(reply1.getData(), reply1.getLength());

                List<Object> response1 = TestUtil.unmarshalReply(data1);

                assertEquals(insertRequestType, (char) response1.get(0));
                assertEquals(insertRequestId, (int) response1.get(1));
                assertEquals(1, (int) response1.get(2));
                assertNotNull(response1.get(3));

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


        DatagramSocket cbSoc = new DatagramSocket(callBackPort);

        buffer = new byte[Constants.MAX_PACKET_SIZE];
        reply =
                new DatagramPacket(buffer, buffer.length);
        cbSoc.receive(reply);
        data = Arrays.copyOf(reply.getData(), reply.getLength());

        response = TestUtil.unmarshalReply(data);

        assertEquals(insertRequestType, (char) response.get(0));
        assertEquals(insertRequestId, (int) response.get(1));
        assertEquals(1, (int) response.get(2));

        String content = (String) response.get(3);
        assertEquals(insertedContent, content);
        cbSoc.close();
    }

    @AfterAll
    public static void tearDown() {
        serverThread.interrupt();
        file.delete();
    }

}