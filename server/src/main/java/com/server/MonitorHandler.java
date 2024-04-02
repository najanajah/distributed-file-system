package com.server;

import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MonitorHandler implements RequestHandler {
    static Logger logger = LogManager.getLogger(MonitorHandler.class.getName());
    /**
     * Key: The file path
     * Value: a set of main.java.com.server.MonitoringClientInfo that monitors this file
     */
    private Map<Path, Set<RegisteredClient>> monitoringInfo;


    public MonitorHandler(Map<Path, Set<RegisteredClient>> monitoringInfo) {
        super();
        this.monitoringInfo = monitoringInfo;
    }

    @Override
    public List<Object> handleRequest(List<Object> request, InetAddress client) {
        logger.entry();
//////////////////////////////////////////////////////////////
//Retrieve and validate parameters
        List<Class<?>> expectedTypes = Arrays.asList(Character.class, Integer.class, String.class, Long.class, Integer.class);

        try {
            ListTypeChecker.check(request, expectedTypes);
        } catch (ListTypeMismatchException e) {
            return Util.errorPacket(e.getMessage());
        }

        char requestType = (char) request.get(0);
        int requestId = (Integer) request.get(1);
        String filePath = (String) request.get(2);
        Long duration = (Long) request.get(3);
        int port = (Integer) request.get(4);

        //Check for code field
//        List<String> missingFields = new LinkedList<String>();
//
//        if(request.get("code") == null){
//            missingFields.add("code");
//        }
//        if(request.get("path") == null){
//            missingFields.add("path");
//        }
//        if(request.get("port") == null){
//            missingFields.add("port");
//        }
//        if(request.get("duration") == null){
//            missingFields.add("duration");
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

//        if(code != 3){
//            String msg = Util.inconsistentReqCodeMsg("Monitor", 3);
//            logger.fatal(msg);
//            return Util.errorPacket(msg);
//        }

        //Check for path field

//        if(!(request.get("path") instanceof String)){
//            return Util.errorPacket(Util.inconsistentFieldTypeMsg("path", "String"));
//        }
//
//        String file = (String)request.get("path");
        Path monitoredPath = Paths.get(filePath);

        if(!monitoredPath.toFile().exists()){
            String msg = Util.nonExistFileMsg(filePath);
            logger.error(msg);
            return Util.errorPacket(msg);
        }

        //Get port attribute

//        if(!(request.get("port") instanceof Integer)){
//            return Util.errorPacket(Util.inconsistentFieldTypeMsg("port", "integer"));
//        }
//        int port = (Integer)request.get("port");

        //Get duration attribute

//        if(!(request.get("duration") instanceof Long)){
//            return Util.errorPacket(Util.inconsistentFieldTypeMsg("duration", "long"));
//        }
//        long duration = (Long)request.get("duration");
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
//Add a 	main.java.com.server.MonitoringClientInfo record to the monitored file
        long expiration = System.currentTimeMillis() + duration;
        RegisteredClient clientInfo = new RegisteredClient(client, port, expiration);

        Set<RegisteredClient> monitoringClients = this.monitoringInfo.get(monitoredPath);
        if(monitoringClients == null){
            monitoringClients = new HashSet<RegisteredClient>();
        }
        monitoringClients.add(clientInfo);

        logger.info("Add " + monitoredPath + " to File " + filePath + " monitoringList");
        this.monitoringInfo.put(monitoredPath, monitoringClients);
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
//Construct the reply
//        Map<String,Object> reply = new HashMap<>();
//        reply.put("status"	, 1);
//        reply.put("end", expiration);
//        reply.put("message", "Monitoring File " + filePath + " Started.");
        List<Object> reply = new ArrayList<>();
        reply.add("Monitoring File " + filePath + " Started.");
        logger.exit();
        return reply;
    }
}