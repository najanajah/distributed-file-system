package com.server.handler;

import com.server.constant.Constants;
import com.server.exception.ListTypeMismatchException;
import com.server.helper.ListTypeChecker;
import com.server.helper.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

public class AtMostOnceHandler implements RequestHandler {
    static Logger logger = LogManager.getLogger(AtMostOnceHandler.class.getName());
    private final Map<String, List<Object>> responseCache;
    private final RequestHandler nextRqHdler;

    public AtMostOnceHandler(Map<String, List<Object>> responseCache, RequestHandler nextRqHdler) {
        super();
        this.responseCache = responseCache;
        this.nextRqHdler = nextRqHdler;
    }

    @Override
    public List<Object> handleRequest(List<Object> request, InetAddress client) {
        logger.entry();

        try {
            ListTypeChecker.check(request, Constants.AtMostOnceExpectedRequestFormat);
        } catch (ListTypeMismatchException e) {
            return Util.errorPacket(e.getMessage());
        }

        char requestType = (char) request.get(0);
        int requestId = (Integer) request.get(1);

        // search in the cache
        String key = client.getHostName() + "." + requestId;
        List<Object> preReply = this.responseCache.get(key);

        if (preReply != null) {
            // return the cached reply if found
            logger.info("Find the cached reply " + preReply + " for " + key);
            return preReply;
        }

        // pass the request to next response handler
        List<Object> nextResponse = this.nextRqHdler.handleRequest(request, client);

        // update the cache using the client ip and timestamp
        this.responseCache.put(key, nextResponse);
        logger.info("Cache the reply " + nextResponse + " for client " + key);

        logger.exit();
        return nextResponse;
    }

}