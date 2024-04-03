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
import java.nio.file.InvalidPathException;
import java.util.List;


public class ReadHandler implements RequestHandler {
    static Logger logger = LogManager.getLogger(ReadHandler.class.getName());

    @Override
    public List<Object> handleRequest(List<Object> request, InetAddress client) {

        logger.trace("Entering ReadHandler");

        try {
            ListTypeChecker.check(request, Constants.ReadServiceExpectedRequestFormat);
        } catch (ListTypeMismatchException e) {
            return Util.errorPacket(e.getMessage());
        }

        char requestType = (char) request.get(0);
        int requestId = (Integer) request.get(1);
        String path = (String) request.get(2);
        Integer offset = (Integer) request.get(3);
        Integer numBytes = (Integer) request.get(4);

        // perform the reading
        String readContent;
        try (RandomAccessFile file = new RandomAccessFile(path, "r")) {
            // check if the offset exceeds the file length
            long fileLength = file.length();

            if (offset >= fileLength) {
                String msg = "Offset " + offset + " exceeds file "
                        + path + " length (" + fileLength + ").";
                logger.error(msg);
                return Util.errorPacket(msg);
            }

            // set the file pointer to the specified offset
            file.seek(offset);

            // read the specified number of bytes
            byte[] bytes = new byte[numBytes];
            int bytesRead = file.read(bytes, 0, numBytes);

            // convert bytes to string, assuming UTF-8 encoding
            readContent = new String(bytes, 0, bytesRead);
        } catch (InvalidPathException e) {
            String msg = Util.invalidPathMsg(path);
            logger.error(msg);
            return Util.errorPacket(msg);
        } catch (FileNotFoundException e) {
            String msg = Util.nonExistFileMsg(path);
            logger.error(msg);
            return Util.errorPacket(msg);
        } catch (IOException e) {
            String msg = "Internal IO Exception: " + e.getMessage();
            logger.error(msg);
            return Util.errorPacket(msg);
        }

        // construct the reply
        List<Object> reply =
                Util.successPacket(readContent);
        logger.trace("Exiting ReadHandler");
        return reply;
    }

}