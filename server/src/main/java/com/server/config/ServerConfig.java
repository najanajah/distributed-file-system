package com.server.config;

import com.server.model.InvocationSemantics;

public class ServerConfig {
    public static final int PORT = 2222;
    public static final int INVOCATION_SEMANTICS = InvocationSemantics.AT_MOST_ONCE.getValue();
    public static final int LOST_REPLY_COUNT = 0;
    public static final int REPLY_DELAY_SEC = 0;
}
