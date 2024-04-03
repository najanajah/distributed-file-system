package com.server;

import java.io.File;
import java.net.InetAddress;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModificationTimeHandler implements RequestHandler {
    static Logger logger = LogManager.getLogger(ModificationTimeHandler.class.getName());

    @Override
    public List<Object> handleRequest(List<Object> request, InetAddress client) {
        logger.entry();
        List<String> missingFields = new LinkedList<String>();
//////////////////////////////////////////////////////////////
        List<Class<?>> expectedTypes = Arrays.asList(Character.class, Integer.class, String.class);

        try {
            ListTypeChecker.check(request, expectedTypes);
        } catch (ListTypeMismatchException e) {
            return Util.errorPacket(e.getMessage());
        }

        char requestType = (char) request.get(0);
        int requestId = (Integer) request.get(1);
        String file = (String) request.get(2);

//Retrieve and validate parameters
        //Check for code field
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
//        if(code != 0){
//            String msg = Util.inconsistentReqCodeMsg("ModificationTime", code);
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
        Long modificationTime = 0L;
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
//Get last modification time
        try{
            Path filePath = Paths.get(file);
            File reqFile = filePath.toFile();
            if(!reqFile.exists()){
                String msg = Util.nonExistFileMsg(file);
                logger.error(msg);
                return Util.errorPacket(msg);
            }
            modificationTime = reqFile.lastModified();
        }catch(InvalidPathException e){
            String msg = Util.invalidPathMsg(file);
            logger.error(msg);
            return Util.errorPacket(msg);
        }
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
//Construct the reply message
        List<Object> reply = new ArrayList<>();
        reply.add(1);
        reply.add(modificationTime);
        logger.exit();
        return reply;
    }

}