package com.server.handler;

import com.server.constant.Constant;
import com.server.exception.ListTypeMismatchException;
import com.server.helper.ListTypeChecker;
import com.server.helper.Util;
import com.server.model.RegisteredClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CallbackHandler implements RequestHandler {

    static Logger logger = LogManager.getLogger(CallbackHandler.class.getName());
    private final Map<Path, Set<RegisteredClient>> monitoringInfo;
    private final RequestHandler nextRqHdler;

    public CallbackHandler(Map<Path, Set<RegisteredClient>> monitoringInfo, RequestHandler nextRqHdler) {
        super();
        this.monitoringInfo = monitoringInfo;
        this.nextRqHdler = nextRqHdler;
    }

    @Override
    public List<Object> handleRequest(List<Object> request, InetAddress client) {
        logger.entry();

        // pass the request to next request handlers in the chain
        List<Object> nextReply = this.nextRqHdler.handleRequest(request, client);

        int code = (int) nextReply.get(0);
        if (code == 0) {
            // if operation fails (Code=0), just return response. No callback message.
            return nextReply;
        }

        try {
            ListTypeChecker.check(request, Constant.CallbackServiceExpectedRequestFormat);
        } catch (ListTypeMismatchException e) {
            return Util.errorPacket(e.getMessage());
        }

        char requestType = (char) request.get(0);
        int requestId = (Integer) request.get(1);
        String filePath = (String) request.get(2);

        // retrieve the records of the specified file
        // for each registered client, check if still valid
        // then, construct the callback message with newly updated file contents
        Set<RegisteredClient> registeredClients = this.monitoringInfo.get(Paths.get(filePath));
        if (registeredClients != null) {
            for (RegisteredClient clientInfo : registeredClients) {
                InetAddress clientAddr = clientInfo.getClientAddr();
                int clientPort = clientInfo.getClientPort();
                long expiration = clientInfo.getExpiration();

                System.out.println(System.currentTimeMillis() < expiration);
                System.out.println(System.currentTimeMillis());

                if (System.currentTimeMillis() < expiration) {
                    Util.sendPacket(clientAddr, clientPort, requestType, requestId, nextReply);
                }
            }
        }

        logger.exit();
        return nextReply;
    }
}