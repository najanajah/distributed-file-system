package com.server.handler;

import java.io.File;
import java.net.InetAddress;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.server.constant.Constant;
import com.server.helper.ListTypeChecker;
import com.server.exception.ListTypeMismatchException;
import com.server.helper.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModificationTimeHandler implements RequestHandler {
    static Logger logger = LogManager.getLogger(ModificationTimeHandler.class.getName());

    @Override
    public List<Object> handleRequest(List<Object> request, InetAddress client) {
        logger.entry();

        try {
            ListTypeChecker.check(request, Constant.GetLastModTimeServiceExpectedRequestFormat);
        } catch (ListTypeMismatchException e) {
            return Util.errorPacket(e.getMessage());
        }

        char requestType = (char) request.get(0);
        int requestId = (Integer) request.get(1);
        String file = (String) request.get(2);

        // get last modification time
        long modificationTime = 0L;

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

        // construct the reply message
        List<Object> reply = new ArrayList<>();
        reply.add(1);
        reply.add(modificationTime);
        logger.exit();
        return reply;
    }

}