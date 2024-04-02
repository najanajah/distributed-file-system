package com.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.nio.file.InvalidPathException;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ReadHandler implements RequestHandler {
    static Logger logger = LogManager.getLogger(ReadHandler.class.getName());

    @Override
    public List<Object> handleRequest(List<Object> request, InetAddress client) {

        logger.trace("Entering ReadHandler");

        List<Class<?>> expectedTypes = Arrays.asList(Character.class, Integer.class, String.class, Integer.class, Integer.class);

        try {
            ListTypeChecker.check(request, expectedTypes);
        } catch (ListTypeMismatchException e) {
            return Util.errorPacket(e.getMessage());
        }

        char requestType = (char) request.get(0);
        int requestId = (Integer) request.get(1);
        String path = (String) request.get(2);
        Integer offset = (Integer) request.get(3);
        Integer numBytes = (Integer) request.get(4);

//        //Retrieve and validate parameters
//        List<String> missingFields = new LinkedList<String>();
//        if(request.get("code") == null){
//            missingFields.add("code");
//        }
//        if(request.get("path") == null){
//            missingFields.add("path");
//        }
//        if(missingFields.size() > 0){
//            return Util.errorPacket(Util.missingFieldMsg(missingFields));
//        }
//
//
//        if(!(request.get("code") instanceof Integer)){
//            return Util.errorPacket(Util.inconsistentFieldTypeMsg("code", "integer"));
//        }
//        int code = (Integer)request.get("code");
//
//        if(code != 1){
//            String msg = Util.inconsistentReqCodeMsg("Read", 1);
//            logger.fatal(msg);
//            return Util.errorPacket(msg);
//        }
//
//        //Check for path field
//
//        if(!(request.get("path") instanceof String)){
//            return Util.errorPacket(Util.inconsistentFieldTypeMsg("path", "String"));
//        }
//
//        String file = (String)request.get("path");


//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
        //Perform the reading
        String readContent;
        try (RandomAccessFile file = new RandomAccessFile(path, "r")) {
            // Check if the offset exceeds the file length
            long fileLength = file.length();

            if (offset >= fileLength) {
                String msg = "Offset " + offset + " exceeds file "
                        + path + " length (" + fileLength + ").";
                logger.error(msg);
                return Util.errorPacket(msg);
            }

            // Set the file pointer to the specified offset
            file.seek(offset);

            // Read the specified number of bytes
            byte[] bytes = new byte[numBytes];
            int bytesRead = file.read(bytes, 0, numBytes);

            // Convert bytes to string, assuming UTF-8 encoding
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
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
//Construct the reply
        List<Object> reply =
                Util.successPacket(readContent);
        logger.trace("Exiting ReadHandler");
        return reply;
    }

}