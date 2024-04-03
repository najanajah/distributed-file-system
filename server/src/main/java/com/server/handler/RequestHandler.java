package com.server.handler;

import java.net.InetAddress;
import java.util.List;

/**
 * The genric request handler interface
 * @author ruanpingcheng
 *
 */
public interface RequestHandler {
    List<Object> handleRequest(List<Object> request, InetAddress client);
}