package com.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Server {
    static Logger logger = LogManager.getLogger(Server.class.getName());

    public static int AT_MOST_ONCE = 1;
    public static int AT_LEAST_ONCE = 2;
    private int port = 8888;
    private int semantics = AT_MOST_ONCE;

    public Server(int port, int semantics){
        this.port = port;
        this.semantics = semantics;
        String semantic = (this.semantics == AT_MOST_ONCE?"At_Most_Once":"At_Least_Once");
        logger.info("main.java.com.server.Server Semantics: " + semantic);
        configureRequestHandler();
    }

    public Server(int port){
        this(port, AT_MOST_ONCE);
    }

    public Server(){
        this(8888, AT_MOST_ONCE);
    }
    private  RequestHandler modTimeHandler = null;
    private  RequestHandler readHandler = null;
    private  RequestHandler insertHandler = null;
    private  RequestHandler monitorHandler = null;
    private  RequestHandler renameHandler = null;
    private  RequestHandler appendHandler = null;

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
                Map<String,Object> request = null;
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
                //Try to unmarshal the received packet
                try{
                    request = Util.unmarshal(data);
                }catch(Exception e){
                    String msg = Util.failUnMarshalMsg(data);
                    msg += " Marshalling Failed: " + e.getMessage();
                    logger.error(msg);

                    Util.sendPacket(clientAddr, clientPort, Util.errorPacket(msg));
                    continue;
                }

                logger.info("Received Request " + request.toString() + "From " + clientAddr.getHostAddress() + " At Port " + clientPort);
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
                //Retrieve and validate requests
                List<String> missingFields = new LinkedList<String>();
                if(request.get("code") == null){
                    missingFields.add("code");
                }
                if(missingFields.size() > 0){
                    String msg = Util.missingFieldMsg(missingFields);
                    logger.error(msg);
                    Util.sendPacket(clientAddr, clientPort, Util.errorPacket(msg));
                    continue;
                }

                if(!(request.get("code") instanceof Integer)){
                    String msg = Util.inconsistentFieldTypeMsg("code", "integer");
                    logger.error(msg);
                    Util.sendPacket(clientAddr, clientPort, Util.errorPacket(msg));
                    continue;
                }
                int code = (Integer)request.get("code");
//////////////////////////////////////////////////////////////

//////////////////////////////////////////////////////////////
                //Route the request to specific request handlers based on the request code
                Map<String,Object> reply = null;
                if(code == 0){
                    reply = this.modTimeHandler.handleRequest(request, clientAddr);
                }else if(code == 1){
                    reply = this.readHandler.handleRequest(request, clientAddr);
                }else if(code == 2){
                    reply = this.insertHandler.handleRequest(request, clientAddr);
                }else if(code == 3){
                    reply = this.monitorHandler.handleRequest(request, clientAddr);
                }else if(code == 4){
                    reply = this.renameHandler.handleRequest(request, clientAddr);
                }else if(code == 5){
                    reply = this.appendHandler.handleRequest(request, clientAddr);
                }else{
                    String msg = "Unrecognized code " + code;
                    logger.error(msg);
                    reply = Util.errorPacket(msg);
                }

                Util.sendPacket(clientAddr, clientPort, reply);
//////////////////////////////////////////////////////////////

            }//End of while(true)
        } catch (SocketException e1) {
            logger.fatal(e1.getMessage());
            e1.printStackTrace();
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
        Map<String,Map<String,Object>> cachedReply = new HashMap<>();
        Map<Path,Set<MonitoringClientInfo>> monitoringInfo = new HashMap<>();

        RequestHandler modTimeHandler = new ModificationTimeHandler();
        RequestHandler readHandler = new ReadHandler();
        RequestHandler insertHandler = new UpdateHandler(monitoringInfo,new InsertHandler());
        RequestHandler appendHandler = new UpdateHandler(monitoringInfo,new AppendHandler());
        RequestHandler renameHandler = new RenameHandler();
        RequestHandler moniterHandler = new MonitorHandler(monitoringInfo);

        if(this.semantics == AT_MOST_ONCE){
            this.modTimeHandler = new AtMostOnceHandler(cachedReply, modTimeHandler);
            this.readHandler = new AtMostOnceHandler(cachedReply, readHandler);
            this.insertHandler = new AtMostOnceHandler(cachedReply, insertHandler);
            this.monitorHandler = new AtMostOnceHandler(cachedReply, moniterHandler);
            this.renameHandler = new AtMostOnceHandler(cachedReply, renameHandler);
            this.appendHandler = new AtMostOnceHandler(cachedReply, appendHandler);
        }else if(this.semantics == AT_LEAST_ONCE){
            this.modTimeHandler =  modTimeHandler;
            this.readHandler = readHandler;
            this.insertHandler = insertHandler;
            this.monitorHandler = moniterHandler;
            this.renameHandler = renameHandler;
            this.appendHandler = appendHandler;

        }else{
            logger.fatal("Unrecognized semantics " + semantics);
        }
        logger.exit();
    }



}