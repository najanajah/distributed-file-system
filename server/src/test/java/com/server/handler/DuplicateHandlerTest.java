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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DuplicateHandlerTest {
    private static Thread serverThread = null;
    private static File sourceFile = null;

    private static final String sourcePath = "test/source.txt";

    private static final int port = 8872;
    private static final String contents = "Test File Duplication";

    @BeforeAll
    public static void setUp() throws IOException, InterruptedException {
        serverThread = new Thread(() -> new Server(port).start());
        serverThread.start();

        Thread.sleep(2000);

        // create the test file
        sourceFile = new File(sourcePath);

        if (sourceFile.getParentFile() != null && !sourceFile.getParentFile().exists()) sourceFile.getParentFile().mkdirs();

        if (sourceFile.exists()) sourceFile.delete();
        sourceFile.createNewFile();

        // write the contents
        BufferedWriter bw = new BufferedWriter(new FileWriter(sourceFile));
        bw.write(contents);
        bw.close();
    }

    @Test
    public void test() throws Exception {
        File parentDirectory = sourceFile.getParentFile();
        File[] filesInFolderBeforeDuplicate = parentDirectory.listFiles();

        // Count the number of files
        int numOfFilesBeforeDuplicate = filesInFolderBeforeDuplicate != null ? filesInFolderBeforeDuplicate.length : 0;

        List<Object> p = new ArrayList<>();
        char requestType = Constants.REQUEST_CODE_DUPLICATE;
        int requestId = 1;
        p.add(sourcePath);

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

        assertEquals(requestType, (char) response.get(0));
        assertEquals(requestId, (int) response.get(1));
        assertEquals(1, (int) response.get(2));
        assertNotNull(response.get(3));

        File sourceFile = new File(sourcePath);
        assertTrue(sourceFile.exists());


        // Check if the parent directory exists
        if (parentDirectory.exists() && parentDirectory.isDirectory()) {
            // List all files in the parent directory
            File[] filesInFolderAfterDuplicate = parentDirectory.listFiles();

            // Count the number of files
            int numOfFilesAfterDuplicate = filesInFolderAfterDuplicate != null ? filesInFolderAfterDuplicate.length : 0;
            assertEquals(numOfFilesAfterDuplicate, numOfFilesBeforeDuplicate + 1);
        }

    }

    @AfterAll
    public static void tearDown() {
        serverThread.interrupt();
        sourceFile.delete();
    }

}


