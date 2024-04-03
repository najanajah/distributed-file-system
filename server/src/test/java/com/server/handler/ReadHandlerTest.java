package com.server.handler;

import com.server.Server;
import com.server.constant.Constants;
import com.server.helper.TestUtil;
import com.server.helper.Util;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReadHandlerTest {
    private static Thread serverThread = null;
    private static File file = null;
    private static final String filePath = "read.txt";
    private final Integer offset = 0;
    private final Integer numBytes = 12;
    private static final int port = 8899;
    static String contents = "Test Reading";

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        serverThread = new Thread(() -> new Server(port).start());
        serverThread.start();

        Thread.sleep(2000);
        // create the test file
        file = Paths.get(filePath).toFile();
        if (file.getParentFile() != null && !file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (file.exists()) file.delete();
        file.createNewFile();

        // write the contents
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(contents);
        bw.close();
    }

    @Test
    public void test() throws Exception {
        List<Object> p = new ArrayList<>();
        char requestType = Constants.REQUEST_CODE_READ;
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

        byte[] buffer = new byte[Constants.MAX_PACKET_SIZE];
        DatagramPacket reply =
                new DatagramPacket(buffer, buffer.length);
        dgs.receive(reply);
        byte[] data = Arrays.copyOf(reply.getData(), reply.getLength());

        List<Object> response = TestUtil.unmarshalReply(data);
        System.out.println(response);

        assertEquals(requestType, (char) response.get(0));
        assertEquals(requestId, (int) response.get(1));
        assertEquals(1, (int) response.get(2));
        assertEquals(response.get(3), contents);

    }

    @AfterAll
    public static void tearDown() {
        serverThread.interrupt();
        file.delete();
    }
}
