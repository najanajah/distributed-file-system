package Driver;

import Exceptions.CorruptMessageException;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Connection Driver for Client. 
 * Establishes a UDP connect to server. 
 * Stores Server IP, Port, HashMap containing Cache Entries
 * Takes 3 variables on creation 
 * freshness interval - Used to determine the cache updates 
 * at_most_once - Used to demonstrate semantics 
 * network_failure_rate - Used to demonstrate network failure.
 */
public class Connection {

    public Scanner scanner;
    public InetAddress host;
    public DatagramSocket socket;
    public HashMap<String, CacheEntry> cache;

    private int server_port;
    private int request_id = 0;

    public int freshness_interval;
    public boolean at_most_once;
    public double network_failure_rate;

    /**
     * Connection driver class
     * @param s scanner
     * @param s_name server name
     * @param s_port server port
     * @param amo at-most-once ot at-least-once
     * @param nfr network failure rate
     * @param f_interval freshness interval
     * @throws UnknownHostException
     * @throws SocketException
     */
    public Connection(Scanner s, String s_name, int s_port, int amo, double nfr, int f_interval)
            throws UnknownHostException, SocketException {
        socket = new DatagramSocket(2222);
        scanner = s;
        server_port = s_port;
        host = InetAddress.getByName(s_name);
        cache = new HashMap<>();
        freshness_interval = f_interval;
        at_most_once = amo == 1;
        network_failure_rate = nfr;
    }

    /**Send datagram to the server through DatagramPacket 
     * Simulate packets being lost in transmission
     * @param packet packet to be sent
     * @throws IOException from sending packet
     */
    public void send_packet(List<Byte> packet_ls) throws IOException{
        byte[] packet = Util.to_primitive(packet_ls);
        DatagramPacket request = new DatagramPacket(packet,
                packet.length, host, server_port);
        double random = Math.random();
        if (random >= network_failure_rate) {
            socket.send(request);
        }
        else if (Constants.DEBUG) {
            System.out.println("Simulating send failure");
        }
    }

    /**Receive one packet from the server
     * @return byte array of the entire packet
     * @throws IOException from receiving packet
     */
    public byte[] receive_packet() throws IOException {
        byte[] buffer = new byte[Constants.MAX_PACKET_SIZE];
        
        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
        System.out.println("[log] trying to receive packet");
        socket.receive(reply);
        System.out.println("[log] packet received");
        byte[] data = Arrays.copyOf(reply.getData(), reply.getLength());
        return data;
    }

    /** 
     * closes connection and throws away any pending packages
     * @throws IOException 
     *
     */
    public void close() throws IOException {
        if (at_most_once) {
            socket.setSoTimeout(Constants.TIMEOUT);
            if (Constants.DEBUG) System.out.println("[log] Begin acknowledging old replies");
            while(true) {
                try {
                    Util.receive_message(request_id, this);
                }
                catch (SocketTimeoutException t) {
                    if (Constants.DEBUG) System.out.println("[log] Socket timeout; cleanup done ");
                    break;
                }
                catch (CorruptMessageException c) {
                    if (Constants.DEBUG) System.out.println("[log] throw away corrupt message");
                }
            }
        }
        socket.close();
    }
    /**
     * increment request ID whenever new request is made
     */
    public void increment_request_id() {
        request_id ++;
    }
    /**
     * return request_id
     */
    public int get_request_id() {
        return request_id;
    }

}
