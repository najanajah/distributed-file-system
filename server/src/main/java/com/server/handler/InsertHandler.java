package com.server.handler;

import com.server.constant.Constants;
import com.server.exception.ListTypeMismatchException;
import com.server.helper.ListTypeChecker;
import com.server.helper.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class InsertHandler implements RequestHandler {
    static Logger logger = LogManager.getLogger(InsertHandler.class.getName());

    @Override
    public List<Object> handleRequest(List<Object> request, InetAddress client, int clientPort) {

        // validate and retrieve parameters
        logger.trace("Entering InsertHandler");

        try {
            ListTypeChecker.check(request, Constants.InsertServiceExpectedRequestFormat);
        } catch (ListTypeMismatchException e) {
            return Util.errorPacket(e.getMessage());
        }

        char requestType = (char) request.get(0);
        int requestId = (Integer) request.get(1);
        String path = (String) request.get(2);
        Integer offset = (Integer) request.get(3);
        String insertedContent = (String) request.get(4);

        // perform the insertion
        String updatedContentAsString = null;
        try (RandomAccessFile file = new RandomAccessFile(path, "rw")) {
            // check if the offset is within the file length
            long fileLength = file.length();
            if (offset > fileLength) {
                String msg = "Offset exceeds file length.";
                logger.error(msg);
                return Util.errorPacket(msg);
            }

            // convert the string to bytes using UTF-8
            byte[] bytesToInsert = insertedContent.getBytes(StandardCharsets.UTF_8);

            byte[] buffer = new byte[(int) (fileLength - offset)];
            file.seek(offset);
            file.readFully(buffer);
            file.seek(offset);
            file.write(bytesToInsert);
            file.write(buffer);

            // get the file content after insertion
            byte[] updatedContent = new byte[(int) file.length()];
            file.seek(0);
            file.readFully(updatedContent);
            updatedContentAsString = new String(updatedContent, StandardCharsets.UTF_8);
        } catch (FileNotFoundException e) {
            String msg = Util.nonExistFileMsg(path);
            logger.error(msg);
            return Util.errorPacket(msg);
        } catch (IOException e) {
            String msg = "Internal IO Exception: " + e.getMessage();
            logger.error(msg);
            return Util.errorPacket(msg);
        }

        // construct the reply message
        List<Object> reply =
                Util.successPacket(updatedContentAsString);
        logger.trace("Exiting InsertHandler");
        return reply;
    }
}
