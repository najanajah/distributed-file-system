package com.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class DeleteHandler implements RequestHandler {
    static Logger logger = LogManager.getLogger(DuplicateHandler.class.getName());

    @Override
    public List<Object> handleRequest(List<Object> request, InetAddress client) {
        //Validate and retrieve parameters
        logger.trace("Entering DeleteHandler");

        List<Class<?>> expectedTypes = Arrays.asList(Character.class, Integer.class, String.class);


        try {
            ListTypeChecker.check(request, expectedTypes);
        } catch (ListTypeMismatchException e) {
            return Util.errorPacket(e.getMessage());
        }

        char requestType = (char) request.get(0);
        int requestId = (Integer) request.get(1);
        String path = (String) request.get(2);


        //Perform the deletion
        Path filePath = Paths.get(path);

        try {
            boolean deleted = Files.deleteIfExists(filePath);
            List<Object> reply;
            if (deleted) {
                reply = Util.successPacket("File was deleted");
            } else {
                reply = Util.successPacket("File does not exist, no need to delete.");
            }
            logger.trace("Exiting DeleteHandler");
            return reply;
        } catch (InvalidPathException e) {
            String msg = Util.invalidPathMsg(path);
            logger.error(msg);
            return Util.errorPacket(msg);
        } catch (IOException e) {
            String msg = "Internal IO Exception: " + e.getMessage();
            logger.error(msg);
            return Util.errorPacket(msg);
        }
    }
}
