package com.server.model;

public enum RequestCode {
    READ('1'),
    INSERT('2'),
    MONITOR('3'),
    DELETE('4'),
    DUPLICATE('5'),
    GETLASTMODIFICATIONTIME('6');

    private final char value;

    RequestCode(char value) {
        this.value = value;
    }

    public char getValue() {
        return value;
    }
}
