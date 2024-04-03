package com.server.constant;

import java.util.Arrays;
import java.util.List;

public class Constant {
    public static final String INSUFFICIENT_DATA_ERROR_MSG = "Insufficient data in buffer";

    public static final List<Class<?>> GetLastModTimeServiceExpectedRequestFormat = Arrays.asList(Character.class, Integer.class, String.class);
    public static final List<Class<?>> AtMostOnceExpectedRequestFormat = Arrays.asList(Character.class, Integer.class);
    public static final List<Class<?>> DeleteServiceExpectedRequestFormat = Arrays.asList(Character.class, Integer.class, String.class);
    public static final List<Class<?>> DuplicateServiceExpectedRequestFormat = Arrays.asList(Character.class, Integer.class, String.class, String.class);
    public static final List<Class<?>> InsertServiceExpectedRequestFormat = Arrays.asList(Character.class, Integer.class, String.class, Integer.class, String.class);
    public static final List<Class<?>> MonitorServiceExpectedRequestFormat = Arrays.asList(Character.class, Integer.class, String.class, Long.class, Integer.class);
    public static final List<Class<?>> ReadServiceExpectedRequestFormat = Arrays.asList(Character.class, Integer.class, String.class, Integer.class, Integer.class);
    public static final List<Class<?>> CallbackServiceExpectedRequestFormat = Arrays.asList(Character.class, Integer.class, String.class);
}
