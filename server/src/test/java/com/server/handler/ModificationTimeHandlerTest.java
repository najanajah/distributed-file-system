package com.server.handler;

import com.server.Server;
import com.server.helper.TestUtil;
import com.server.helper.Util;
import com.server.model.RequestCode;
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

class ModificationTimeHandlerTest {
    private static Thread serverThread = null;
    private static File file = null;
    private static String filePath = "test/get_modification_time.txt";
    private static int port = 8888;
    static String contents = "Test Getting Modification Time";

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException{
        serverThread = new Thread(() -> new Server(port).start());
        serverThread.start();

        Thread.sleep(2000);

        // create the test file
        file = Paths.get(filePath).toFile();
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if(file.exists()) file.delete();
        file.createNewFile();

        // write the contents
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(contents);
        bw.close();
    }

    @Test
    public void test() throws Exception {

        List<Object> p = new ArrayList<>();
        char requestType = RequestCode.GETLASTMODIFICATIONTIME.getValue();
        int requestId = 256;
        p.add(filePath);

        byte[] b = Util.marshal(requestType, requestId, p);

        DatagramSocket dgs = new DatagramSocket();
        InetAddress serverAddr = InetAddress.getLocalHost();
        DatagramPacket request =
                new DatagramPacket(b, b.length, serverAddr, port);
        dgs.send(request);

        System.out.println("Send to server: " + p);

        byte[] buffer = new byte[1024];
        DatagramPacket reply = new DatagramPacket(buffer,buffer.length);
        dgs.receive(reply);
        byte[] data = Arrays.copyOf(reply.getData(), reply.getLength());

        List<Object> response = TestUtil.unmarshalReply(data);

        assertEquals(requestType, (char) response.get(0));
        assertEquals(requestId, (int) response.get(1));
        assertEquals(1, (int) response.get(2));
        assertEquals(response.get(3), String.valueOf(file.lastModified()));
    }

    @AfterAll
    public static void tearDown(){
        serverThread.interrupt();
        file.delete();
    }
}