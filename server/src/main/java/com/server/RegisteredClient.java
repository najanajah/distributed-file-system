package com.server;

import java.net.InetAddress;
/**
 * An instance of main.java.com.server.MonitoringClientInfo is a record for the clients that monitoring a certain file.
 * The record consists of a client's IP, port number and the time when monitoring expires.
 *
 */
public class RegisteredClient {

    private final InetAddress clientAddr;
    private final int clientPort;
    private final long expiration;
    public RegisteredClient(InetAddress clientAddr, int clientPort, long expiration) {
        super();
        this.clientAddr = clientAddr;
        this.clientPort = clientPort;
        this.expiration = expiration;
    }
    public InetAddress getClientAddr() {
        return clientAddr;
    }
    public int getClientPort() {
        return clientPort;
    }
    public long getExpiration() {
        return expiration;
    }
    @Override
    public String toString() {
        return "main.java.com.server.MonitoringClientInfo [clientAddr=" + clientAddr + ", clientPort=" + clientPort + ", expiration="
                + expiration + "]";
    }
}
