package com.server;

import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UpdateHandler implements RequestHandler {

    static Logger logger = LogManager.getLogger(UpdateHandler.class.getName());

    private Map<Path, Set<RegisteredClient>> monitoringInfo =
            null;


    private RequestHandler nextRqHdler = null;



    public UpdateHandler(Map<Path, Set<RegisteredClient>> monitoringInfo, RequestHandler nextRqHdler) {
        super();
        this.monitoringInfo = monitoringInfo;
        this.nextRqHdler = nextRqHdler;
    }

    @Override
    public List<Object> handleRequest(List<Object> request, InetAddress client) {
        logger.entry();
//////////////////////////////////////////////////////////////
//Pass the request to next request handlers in the chain
        List<Object> nextReply = this.nextRqHdler.handleRequest(request, client);

        int code = (int) nextReply.get(0);
        if(code == 0){
            //If operation fails (Code=0), just return response. No callback message.
            return nextReply;
        }

//////////////////////////////////////////////////////////////
        List<Class<?>> expectedTypes = Arrays.asList(Character.class, Integer.class, String.class);

        try {
            ListTypeChecker.check(request, expectedTypes);
        } catch (ListTypeMismatchException e) {
            return Util.errorPacket(e.getMessage());
        }

        char requestType = (char) request.get(0);
        int requestId = (Integer) request.get(1);
        String filePath = (String) request.get(2);

        System.out.println("filePath to be callback: " + filePath);

        //Retrieve and validate parameters
//        List<String> missingFields = new LinkedList<String>();
//        if(request.get("path") == null){
//            missingFields.add("path");
//        }
//        if(missingFields.size() > 0){
//            return Util.errorPacket(Util.missingFieldMsg(missingFields));
//        }
//        if(!(request.get("path") instanceof String)){
//            return Util.errorPacket(Util.inconsistentFieldTypeMsg("path", "String"));
//        }
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
//Retrieve the file's last modification time
        String content = null;
//        long modificationTime;
//        try{
//            File reqFile = Paths.get(filePath).toFile();
//            Scanner fileScanner = new Scanner(reqFile);
//            content = fileScanner.useDelimiter("\\Z").next();
//            modificationTime = reqFile.lastModified();
//            fileScanner.close();
//        }catch(InvalidPathException e){
//            String msg = Util.invalidPathMsg(filePath);
//            logger.error(msg);
//            return Util.errorPacket(msg);
//        }catch (FileNotFoundException e) {
//            String msg = Util.nonExistFileMsg(filePath);
//            logger.error(msg);
//            return Util.errorPacket(msg);
//        }
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
//Retrieve the records of main.java.com.server.MonitoringClientInfo for that file
//For each record, if the current time has not exceeded its monitoring expiration time
        //Construct the callback message with newly updated file contents, modifier and other info
        Set<RegisteredClient> registeredClients = this.monitoringInfo.get(Paths.get(filePath));
        if(registeredClients != null){
            for(RegisteredClient clientInfo: registeredClients){
                InetAddress clientAddr = clientInfo.getClientAddr();
                int clientPort = clientInfo.getClientPort();
                long expiration = clientInfo.getExpiration();

                System.out.println(System.currentTimeMillis() < expiration);
                System.out.println(System.currentTimeMillis());

                if(System.currentTimeMillis() < expiration){
                    List<Object> callbackMsg = nextReply;
//                    callbackMsg.put("status"	, Integer.valueOf(1));
//                    callbackMsg.put("time", modificationTime);
//                    callbackMsg.put("path", filePath);
//                    callbackMsg.put("modifier", client.getHostAddress());
//                    callbackMsg.put("content", content);
                    Util.sendPacket(clientAddr, clientPort, requestType, requestId, nextReply);
                }
            }
        }
//////////////////////////////////////////////////////////////

        logger.exit();
        return nextReply;
    }


}