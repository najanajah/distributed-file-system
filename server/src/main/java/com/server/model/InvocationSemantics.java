package com.server.model;

public enum InvocationSemantics {
    AT_MOST_ONCE(1),
    AT_LEAST_ONCE(2);

    private final int value;

    InvocationSemantics(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
