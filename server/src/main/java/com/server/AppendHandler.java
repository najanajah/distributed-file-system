//package com.server;
//
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.net.InetAddress;
//import java.nio.file.InvalidPathException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Scanner;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//public class AppendHandler implements RequestHandler {
//    static Logger logger = LogManager.getLogger(AppendHandler.class.getName());
//
//    @Override
//    public Map<String, Object> handleRequest(Map<String, Object> request, InetAddress client) {
///////////////////////////////////////////////////////
//        //Validate and retrieve parameters
//        logger.entry();
//        List<String> missingFields = new LinkedList<String>();
//        if(request.get("code") == null){
//            missingFields.add("code");
//        }
//        if(request.get("path") == null){
//            missingFields.add("path");
//        }
//        if(request.get("append") == null){
//            missingFields.add("append");
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
//        if(code != 5){
//            String msg = Util.inconsistentReqCodeMsg("Read", 5);
//            logger.fatal(msg);
//            return Util.errorPacket(msg);
//        }
//
//        if(!(request.get("path") instanceof String)){
//            return Util.errorPacket(Util.inconsistentFieldTypeMsg("path", "String"));
//        }
//
//        String file = (String)request.get("path");
//
//        if(!(request.get("append") instanceof String)){
//            return Util.errorPacket(Util.inconsistentFieldTypeMsg("append", "String"));
//        }
//        String appendedContents = (String)request.get("append");
//
//        String content = null;
//
////////////////////////////////////////////////////////////////
//
////////////////////////////////////////////////////////////////
//        //Perform the appending
//
//        try{
//            Path filePath = Paths.get(file);
//            File reqFile = filePath.toFile();
//            Scanner fileScanner = new Scanner(reqFile);
//            content = fileScanner.useDelimiter("\\Z").next();
//            fileScanner.close();
//
//            StringBuffer buffer = new StringBuffer(content);
//            buffer.append(appendedContents);
//
//            BufferedWriter bw = new BufferedWriter(new FileWriter(reqFile));
//            bw.write(buffer.toString());
//            bw.close();
//
//        }catch(InvalidPathException e){
//            String msg = Util.invalidPathMsg(file);
//            logger.error(msg);
//            return Util.errorPacket(msg);
//        }catch (FileNotFoundException e) {
//            String msg = Util.nonExistFileMsg(file);
//            logger.error(msg);
//            return Util.errorPacket(msg);
//        } catch (IOException e) {
//            String msg = "Internal IO Exception: " + e.getMessage();
//            logger.error(msg);
//            return Util.errorPacket(msg);
//        }
//
///////////////////////////////////////////
//        //Construct the reply
//
//        Map<String,Object> reply =
//                Util.successPacket("File " + file + " Appending Succeeded.");
//
//
//        logger.exit();
//        return reply;
//
//    }
//
//}
