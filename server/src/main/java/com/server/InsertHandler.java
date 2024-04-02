package com.server;

import java.io.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InsertHandler implements RequestHandler {
    static Logger logger = LogManager.getLogger(InsertHandler.class.getName());

    @Override
    public List<Object> handleRequest(List<Object> request, InetAddress client) {

        //Validate and retrieve parameters
        logger.trace("Entering InsertHandler");

        List<Class<?>> expectedTypes = Arrays.asList(Character.class, Integer.class, String.class, Integer.class, String.class);

        try {
            ListTypeChecker.check(request, expectedTypes);
        } catch (ListTypeMismatchException e) {
            return Util.errorPacket(e.getMessage());
        }

        char requestType = (char) request.get(0);
        int requestId = (Integer) request.get(1);
        String path = (String) request.get(2);
        Integer offset = (Integer) request.get(3);
        String insertedContent = (String) request.get(4);


//        List<String> missingFields = new LinkedList<String>();
//        if(request.get("code") == null){
//            missingFields.add("code");
//        }
//        if(request.get("path") == null){
//            missingFields.add("path");
//        }
//        if(request.get("offset") == null){
//            missingFields.add("offset");
//        }
//        if(request.get("insertion") == null){
//            missingFields.add("insertion");
//        }
//        if(missingFields.size() > 0){
//            return Util.errorPacket(Util.missingFieldMsg(missingFields));
//        }
//
//        if(!(request.get("code") instanceof Integer)){
//            return Util.errorPacket(Util.inconsistentFieldTypeMsg("code", "integer"));
//        }
//        int code = (Integer)request.get("code");
//
//        if(code != 2){
//            String msg = Util.inconsistentReqCodeMsg("Insert", 2);
//            logger.fatal(msg);
//            return Util.errorPacket(msg);
//        }

        //Check for path field
//
//        if(!(request.get("path") instanceof String)){
//            return Util.errorPacket(Util.inconsistentFieldTypeMsg("path", "String"));
//        }
//
//        String file = (String)request.get("path");
//
//
//        if(!(request.get("offset") instanceof Integer)){
//            return Util.errorPacket(Util.inconsistentFieldTypeMsg("offset", "Integer"));
//        }
//        int offset = (Integer)request.get("offset");
//
//        if(!(request.get("insertion") instanceof String)){
//            return Util.errorPacket(Util.inconsistentFieldTypeMsg("insertion", "String"));
//        }
//
//        String insertedContents = (String)request.get("insertion");


//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
        //Perform the insertion
        Path filepath = Paths.get(path);
        try (RandomAccessFile file = new RandomAccessFile(path, "rw")) {
            // Check if the offset is within the file length
            long fileLength = file.length();
            if (offset > fileLength) {
                String msg = "Offset exceeds file length.";
                logger.error(msg);
                return Util.errorPacket(msg);
            }

            // Convert the string to bytes using UTF-8 or any appropriate charset
            byte[] bytesToInsert = insertedContent.getBytes(StandardCharsets.UTF_8);

            byte[] buffer = new byte[(int) (fileLength - offset)];
            file.seek(offset);
            file.readFully(buffer); // Read the content after the offset

            file.seek(offset);
            file.write(bytesToInsert); // Insert new bytes (converted from string)
            file.write(buffer); // Write back the original content pushed forward

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
//Construct the reply message
        List<Object> reply =
                Util.successPacket("File " + path + " Insertion Succeeded.");
        logger.trace("Exiting InsertHandler");
        return reply;
    }
}
