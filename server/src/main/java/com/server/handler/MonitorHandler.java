package com.server.handler;

import com.server.constant.Constants;
import com.server.exception.ListTypeMismatchException;
import com.server.helper.ListTypeChecker;
import com.server.helper.Util;
import com.server.model.RegisteredClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MonitorHandler implements RequestHandler {
    static Logger logger = LogManager.getLogger(MonitorHandler.class.getName());
    private final Map<Path, Set<RegisteredClient>> monitoringInfo;


    public MonitorHandler(Map<Path, Set<RegisteredClient>> monitoringInfo) {
        super();
        this.monitoringInfo = monitoringInfo;
    }

    @Override
    public List<Object> handleRequest(List<Object> request, InetAddress client, int clientPort) {
        logger.entry();
        // retrieve and validate parameters
        try {
            ListTypeChecker.check(request, Constants.MonitorServiceExpectedRequestFormat);
        } catch (ListTypeMismatchException e) {
            return Util.errorPacket(e.getMessage());
        }

        char requestType = (char) request.get(0);
        int requestId = (Integer) request.get(1);
        String filePath = (String) request.get(2);
        Long duration = (Long) request.get(3);

        Path monitoredPath = Paths.get(filePath);

        if (!monitoredPath.toFile().exists()) {
            String msg = Util.nonExistFileMsg(filePath);
            logger.error(msg);
            return Util.errorPacket(msg);
        }

        // add a record to the monitored file
        long expiration = System.currentTimeMillis() + duration;
        RegisteredClient clientInfo = new RegisteredClient(client, clientPort, expiration);

        Set<RegisteredClient> registeredClients = this.monitoringInfo.get(monitoredPath);
        if (registeredClients == null) {
            registeredClients = new HashSet<>();
        }
        registeredClients.add(clientInfo);

        logger.info("Add " + monitoredPath + " to File " + filePath + " monitoringList");
        this.monitoringInfo.put(monitoredPath, registeredClients);

        // construct the reply
        List<Object> reply = new ArrayList<>();
        reply.add(1);
        reply.add("Monitoring File " + filePath + " Started.");
        logger.exit();
        return reply;
    }
}