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
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class DuplicateHandlerTest {
    private static Thread serverThread = null;
    private static File destinationFile = null;
    private static File sourceFile = null;

    private static final String sourcePath = "test/source.txt";
    private static final String destinationPath = "test/destination.txt";

    private static final int port = 8872;
    private static final String contents = "Test File Duplication";

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        serverThread = new Thread(() -> new Server(port).start());
        serverThread.start();

        Thread.sleep(2000);

        // create the test file
        sourceFile = Paths.get(sourcePath).toFile();

        if (!sourceFile.getParentFile().exists()) sourceFile.getParentFile().mkdirs();

        if (sourceFile.exists()) sourceFile.delete();
        sourceFile.createNewFile();

        // write the contents
        BufferedWriter bw = new BufferedWriter(new FileWriter(sourceFile));
        bw.write(contents);
        bw.close();

        destinationFile = Paths.get(destinationPath).toFile();
        destinationFile.delete();
    }

    @Test
    public void test() throws Exception {

        List<Object> p = new ArrayList<>();
        char requestType = RequestCode.DUPLICATE.getValue();
        int requestId = 1;
        p.add(sourcePath);
        p.add(destinationPath);

        byte[] b = Util.marshal(requestType, requestId, p);

        DatagramSocket dgs = new DatagramSocket();
        InetAddress serverAddr = InetAddress.getLocalHost();
        DatagramPacket request =
                new DatagramPacket(b, b.length, serverAddr, port);
        dgs.send(request);

        System.out.println("Send to server: " + p);

        byte[] buffer = new byte[1024];
        DatagramPacket reply =
                new DatagramPacket(buffer, buffer.length);
        dgs.receive(reply);
        byte[] data = Arrays.copyOf(reply.getData(), reply.getLength());

        List<Object> response = TestUtil.unmarshalReply(data);

        assertEquals(requestType, (char) response.get(0));
        assertEquals(requestId, (int) response.get(1));
        assertEquals(1, (int) response.get(2));
        assertNotNull(response.get(3));

        File sourceFile = Paths.get(sourcePath).toFile();
        File destinationFile = Paths.get(destinationPath).toFile();
        assertTrue(sourceFile.exists());
        assertTrue(destinationFile.exists());

        Scanner fileScanner = new Scanner(destinationFile);
        String content = fileScanner.useDelimiter("\\Z").next();
        fileScanner.close();

        assertEquals(content, contents);
    }

    @AfterAll
    public static void tearDown() {
        serverThread.interrupt();
        destinationFile.delete();
        sourceFile.delete();
    }

}


