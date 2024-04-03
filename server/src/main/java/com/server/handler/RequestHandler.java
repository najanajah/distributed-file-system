package com.server.handler;

import java.net.InetAddress;
import java.util.List;

// generic request handler interface
public interface RequestHandler {
    List<Object> handleRequest(List<Object> request, InetAddress client);
}