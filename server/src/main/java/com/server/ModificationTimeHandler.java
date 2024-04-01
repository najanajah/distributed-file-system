package com.server;

import java.io.File;
import java.net.InetAddress;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModificationTimeHandler implements RequestHandler {
    static Logger logger = LogManager.getLogger(ModificationTimeHandler.class.getName());

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> request, InetAddress client) {
        logger.entry();
        List<String> missingFields = new LinkedList<String>();
//////////////////////////////////////////////////////////////
//Retrieve and validate parameters
        //Check for code field
        if(request.get("code") == null){
            missingFields.add("code");
        }
        if(request.get("path") == null){
            missingFields.add("path");
        }
        if(missingFields.size() > 0){
            return Util.errorPacket(Util.missingFieldMsg(missingFields));
        }


        if(!(request.get("code") instanceof Integer)){
            return Util.errorPacket(Util.inconsistentFieldTypeMsg("code", "integer"));
        }
        int code = (Integer)request.get("code");

        if(code != 0){
            String msg = Util.inconsistentReqCodeMsg("ModificationTime", code);
            logger.fatal(msg);
            return Util.errorPacket(msg);
        }

        //Check for path field

        if(!(request.get("path") instanceof String)){
            return Util.errorPacket(Util.inconsistentFieldTypeMsg("path", "String"));
        }

        String file = (String)request.get("path");
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
        Map<String,Object> reply = new HashMap<String,Object>();
        reply.put("status", Integer.valueOf(1));
        reply.put("path", file);
        reply.put("modification", modificationTime);
        logger.exit();
        return reply;
    }

}