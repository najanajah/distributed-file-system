package com.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class DuplicateHandler implements RequestHandler {
    static Logger logger = LogManager.getLogger(DuplicateHandler.class.getName());

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> request, InetAddress client) {
        //Validate and retrieve parameters
        logger.entry();
        List<String> missingFields = new LinkedList<String>();
        if(request.get("code") == null){
            missingFields.add("code");
        }
        if(request.get("sourcePath") == null){
            missingFields.add("sourcePath");
        }
        if(request.get("destinationPath") == null){
            missingFields.add("destinationPath");
        }
        if(missingFields.size() > 0){
            return Util.errorPacket(Util.missingFieldMsg(missingFields));
        }

        if(!(request.get("code") instanceof Integer)){
            return Util.errorPacket(Util.inconsistentFieldTypeMsg("code", "integer"));
        }
        int code = (Integer)request.get("code");

//        if(code != 2){
//            String msg = Util.inconsistentReqCodeMsg("Insert", 2);
//            logger.fatal(msg);
//            return Util.errorPacket(msg);
//        }

        //Check for path field

        if(!(request.get("sourcePath") instanceof String)){
            return Util.errorPacket(Util.inconsistentFieldTypeMsg("sourcePath", "String"));
        }

        String sourcePath = (String)request.get("sourcePath");

        if(!(request.get("destinationPath") instanceof String)){
            return Util.errorPacket(Util.inconsistentFieldTypeMsg("destinationPath", "String"));
        }

        String destinationPath = (String)request.get("destinationPath");

        String content = null;

        //Perform the duplication
        try{
            File sourceFile = Paths.get(sourcePath).toFile();

            if(!sourceFile.exists()){
                String msg = Util.nonExistFileMsg(sourcePath);
                logger.error(msg);
                return Util.errorPacket(msg);
            }

            File destinationFile = Paths.get(destinationPath).toFile();
            if(destinationFile.exists()){
                String msg = "Renamed file " + destinationFile + " already exists";
                logger.error(msg);
                return Util.errorPacket(msg);
            }

            Scanner fileScanner = new Scanner(sourceFile);
            content = fileScanner.useDelimiter("\\Z").next();
            fileScanner.close();


            StringBuffer buffer = new StringBuffer(content);

            BufferedWriter bw = new BufferedWriter(new FileWriter(destinationFile));
            bw.write(buffer.toString());
            bw.close();

//        }catch(InvalidPathException e){
//            String msg = Util.invalidPathMsg(file);
//            logger.error(msg);
//            return Util.errorPacket(msg);
        }catch (FileNotFoundException e) {
            String msg = Util.nonExistFileMsg(sourcePath);
            logger.error(msg);
            return Util.errorPacket(msg);
        } catch (IOException e) {
            String msg = "Internal IO Exception: " + e.getMessage();
            logger.error(msg);
            return Util.errorPacket(msg);
        }

        //Construct the reply message
        Map<String,Object> reply =
                Util.successPacket("File " + sourcePath + " Duplication Succeeded.");

        logger.exit();
        return reply;
    }
}
