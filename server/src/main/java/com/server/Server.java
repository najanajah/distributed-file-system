package com.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Path;
import java.util.*;

import static com.server.InvocationSemantics.AT_LEAST_ONCE;
import static com.server.InvocationSemantics.AT_MOST_ONCE;

public class Server {
    static Logger logger = LogManager.getLogger(Server.class.getName());
    private final int port;
    private final int semantics;

    public Server(int port, int semantics){
        this.port = port;
        this.semantics = semantics;
        // default invocation semantics is at-most-once
        String semantic = (this.semantics == AT_MOST_ONCE.getValue()?"at-most-once":"at-least-once");
        logger.info("main.java.com.server.Server Semantics: " + semantic);
        configureRequestHandler();
    }

    public Server(int port){
        this(port, AT_MOST_ONCE.getValue());
    }

    public Server(){
        this(ServerConfig.PORT, AT_MOST_ONCE.getValue());
    }
    private  RequestHandler modTimeHandler = null;
    private  RequestHandler readHandler = null;
    private  RequestHandler insertHandler = null;
    private  RequestHandler monitorHandler = null;
//    private  RequestHandler renameHandler = null;
//    private  RequestHandler appendHandler = null;
    private RequestHandler duplicateHandler = null;
    private RequestHandler deleteHandler = null;

    public void start(){
        logger.entry();
        try(DatagramSocket dgs = new DatagramSocket(this.port)){

            while(true){
//////////////////////////////////////////////////////////////
                //Keep waiting on the designated port
                byte[] buffer = new byte[1024];
                DatagramPacket requestPacket =
                        new DatagramPacket(buffer,buffer.length);
                logger.info("Waiting for request at port " + this.port);
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
                //Retrieve the request message
                dgs.receive(requestPacket);
                byte[] data = Arrays.copyOf(requestPacket.getData(), requestPacket.getLength());
                InetAddress clientAddr = requestPacket.getAddress();
                int  clientPort = requestPacket.getPort();
                List<Object> request;
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
                //Try to unmarshal the received packet
                try{
                    request = Util.unmarshal(data);
                }catch(Exception e){
                    String msg = Util.failUnMarshalMsg(data);
                    msg += " Marshalling Failed: " + e.getMessage();
                    logger.error(msg);

//                    Util.sendPacket(clientAddr, clientPort, Util.errorPacket(msg));
                    continue;
                }

                logger.info("Received Request " + request + "From " + clientAddr.getHostAddress() + " At Port " + clientPort);
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
                //Retrieve and validate requests
//                List<String> missingFields = new LinkedList<>();
//                if(request.get("code") == null){
//                    missingFields.add("code");
//                }
//                if(missingFields.size() > 0){
//                    String msg = Util.missingFieldMsg(missingFields);
//                    logger.error(msg);
//                    Util.sendPacket(clientAddr, clientPort, Util.errorPacket(msg));
//                    continue;
//                }
//
//                if(!(request.get("code") instanceof Integer)){
//                    String msg = Util.inconsistentFieldTypeMsg("code", "integer");
//                    logger.error(msg);
//                    Util.sendPacket(clientAddr, clientPort, Util.errorPacket(msg));
//                    continue;
//                }
//                int code = (Integer)request.get("code");
                char requestType = (char) request.get(0);
                int requestId = (int) request.get(1);
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
                //Route the request to specific request handlers based on the request code
                List<Object> reply;
                if(requestType == '1'){ // read
                    reply = this.readHandler.handleRequest((ArrayList<Object>) request, clientAddr);
                }else if(requestType == '2'){ // write
                    reply = this.insertHandler.handleRequest((ArrayList<Object>) request, clientAddr);
                }else if(requestType == '3'){ // monitor
                    reply = this.monitorHandler.handleRequest((ArrayList<Object>) request, clientAddr);
                }else if(requestType == '4'){ // delete
                    reply = this.deleteHandler.handleRequest((ArrayList<Object>) request, clientAddr);
                }else if(requestType == '5') { // duplicate
                    reply = this.duplicateHandler.handleRequest((ArrayList<Object>) request, clientAddr);
                } else if (requestType == '6') {
                    reply = this.modTimeHandler.handleRequest((ArrayList<Object>) request, clientAddr);
                }else{
                    String msg = "Unrecognized code " + requestType;
                    logger.error(msg);
                    reply = Util.errorPacket(msg);
                }

                Util.sendPacket(clientAddr, clientPort, requestType, requestId, reply);
//////////////////////////////////////////////////////////////

            }//End of while(true)
        } catch (IOException e1) {
            logger.fatal(e1.getMessage());
            e1.printStackTrace();
        }
        logger.exit();
    }


    /**
     * Construct the request handlers and chain them into a list
     * based on the configured semantics.
     */
    private  void configureRequestHandler(){
        logger.entry();
        Map<String,List<Object>> cachedReply = new HashMap<>();
        Map<Path,Set<RegisteredClient>> monitoringInfo = new HashMap<>();

        RequestHandler modTimeHandler = new ModificationTimeHandler();
        RequestHandler readHandler = new ReadHandler();
        RequestHandler insertHandler = new UpdateHandler(monitoringInfo,new InsertHandler());
//        RequestHandler appendHandler = new UpdateHandler(monitoringInfo,new AppendHandler());
//        RequestHandler renameHandler = new RenameHandler();
        RequestHandler monitorHandler = new MonitorHandler(monitoringInfo);
        RequestHandler duplicateHandler = new DuplicateHandler();
        RequestHandler deleteHandler = new DeleteHandler();

        if(this.semantics == AT_MOST_ONCE.getValue()){
            this.modTimeHandler = new AtMostOnceHandler(cachedReply, modTimeHandler);
            this.readHandler = new AtMostOnceHandler(cachedReply, readHandler);
            this.insertHandler = new AtMostOnceHandler(cachedReply, new UpdateHandler(monitoringInfo,new InsertHandler()));
            this.monitorHandler = new AtMostOnceHandler(cachedReply, monitorHandler);
//            this.renameHandler = new AtMostOnceHandler(cachedReply, renameHandler);
//            this.appendHandler = new AtMostOnceHandler(cachedReply, appendHandler);
            this.duplicateHandler = new AtMostOnceHandler(cachedReply, duplicateHandler);
            this.deleteHandler = new AtMostOnceHandler(cachedReply, deleteHandler);
        }else if(this.semantics == AT_LEAST_ONCE.getValue()){
            this.modTimeHandler =  modTimeHandler;
            this.readHandler = readHandler;
            this.insertHandler = insertHandler;
            this.monitorHandler = monitorHandler;
//            this.renameHandler = renameHandler;
//            this.appendHandler = appendHandler;
            this.duplicateHandler = duplicateHandler;
            this.deleteHandler = deleteHandler;
        }else{
            logger.fatal("Unrecognized semantics " + semantics);
        }
        logger.exit();
    }



}