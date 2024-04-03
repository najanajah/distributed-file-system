package com.server.constant;

import java.util.Arrays;
import java.util.List;

public class Constants {
    public static final String INSUFFICIENT_DATA_ERROR_MSG = "Insufficient data in buffer";
    public static final int MAX_PACKET_SIZE = 1024;
    public static final char REQUEST_CODE_READ = '1';
    public static final char REQUEST_CODE_INSERT = '2';
    public static final char REQUEST_CODE_MONITOR = '3';
    public static final char REQUEST_CODE_DELETE = '4';
    public static final char REQUEST_CODE_DUPLICATE = '5';
    public static final char REQUEST_CODE_GET_LAST_MODIFICATION_TIME = '6';

    public static final List<Class<?>> GetLastModTimeServiceExpectedRequestFormat = Arrays.asList(Character.class, Integer.class, String.class);
    public static final List<Class<?>> AtMostOnceExpectedRequestFormat = Arrays.asList(Character.class, Integer.class);
    public static final List<Class<?>> DeleteServiceExpectedRequestFormat = Arrays.asList(Character.class, Integer.class, String.class);
    public static final List<Class<?>> DuplicateServiceExpectedRequestFormat = Arrays.asList(Character.class, Integer.class, String.class, String.class);
    public static final List<Class<?>> InsertServiceExpectedRequestFormat = Arrays.asList(Character.class, Integer.class, String.class, Integer.class, String.class);
    public static final List<Class<?>> MonitorServiceExpectedRequestFormat = Arrays.asList(Character.class, Integer.class, String.class, Long.class);
    public static final List<Class<?>> ReadServiceExpectedRequestFormat = Arrays.asList(Character.class, Integer.class, String.class, Integer.class, Integer.class);
    public static final List<Class<?>> CallbackServiceExpectedRequestFormat = Arrays.asList(Character.class, Integer.class, String.class);
}
