package com.server;

import java.util.List;

class ListTypeMismatchException extends Exception {
    public ListTypeMismatchException(String message) {
        super(message);
    }
}

public class ListTypeChecker {
    public static void check(List<Object> list, List<Class<?>> expectedTypes) throws ListTypeMismatchException {
        if (list.size() != expectedTypes.size()) {
            throw new ListTypeMismatchException("Number of elements in the list does not match the number of expected types.");
        }

        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);
            Class<?> expectedType = expectedTypes.get(i);
            if (!expectedType.isInstance(obj)) {
                throw new ListTypeMismatchException("Element at index " + i + " does not match the expected type: " + expectedType.getSimpleName());
            }
        }
    }
}

