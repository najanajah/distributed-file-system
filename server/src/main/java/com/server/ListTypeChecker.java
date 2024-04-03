package com.server;

import java.util.List;

class ListTypeMismatchException extends Exception {
    public ListTypeMismatchException(String message) {
        super(message);
    }
}

public class ListTypeChecker {
    public static void check(List<Object> list, List<Class<?>> expectedTypes) throws ListTypeMismatchException {
        for (int i = 0; i < expectedTypes.size(); i++) {
            Object obj = list.get(i);
            Class<?> expectedType = expectedTypes.get(i);
            if (!expectedType.isInstance(obj)) {
                throw new ListTypeMismatchException("Element at index " + i + " does not match the expected type: " + expectedType.getSimpleName());
            }
        }
    }
}

