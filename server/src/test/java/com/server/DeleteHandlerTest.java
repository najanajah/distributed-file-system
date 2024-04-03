package com.server;

import org.junit.Before;
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

import static org.junit.jupiter.api.Assertions.*;

class DeleteHandlerTest {
    private static Thread serverThread = null;
    private static File file = null;
    private static final String filePath = "test/delete.txt";
    private static final int port = 3456;
    private static final String contents = "Test Deleting";

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException{
        serverThread = new Thread(() -> new Server(port).start());
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
    public void test() throws Exception {

        List<Object> p = new ArrayList<>();
        char requestType = '4';
        int requestId = 9999;
        p.add(filePath);

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

        assertEquals(requestType, (char) response.get(0));
        assertEquals(requestId, (int) response.get(1));
        assertEquals(1, (int) response.get(2));
        assertNotNull(response.get(3));

        File sourceFile = Paths.get(filePath).toFile();
        assertFalse(sourceFile.exists());
    }

    @AfterAll
    public static void tearDown(){
        serverThread.interrupt();
        file.delete();
    }

}
