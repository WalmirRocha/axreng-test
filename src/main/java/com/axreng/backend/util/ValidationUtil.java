package com.axreng.backend.util;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;

public class ValidationUtil {

    private final static ValidationUtil INSTANCE = new ValidationUtil();
    private final Map<Class<?>, BiConsumer<Object, String>> validationStrategies = new HashMap<>();

    private ValidationUtil() {

        validationStrategies.put(String.class, (value, fieldName) -> {
            if (((String) value).trim().isEmpty()) {
                throw new IllegalArgumentException("The property '" + fieldName + "' is required and cannot be empty.");
            }
        });

        validationStrategies.put(Integer.class, (value, fieldName) -> {
            if ((Integer) value <= 0) {
                throw new IllegalArgumentException("The property '" + fieldName + "' must be a positive number.");
            }
        });
    }

    public static ValidationUtil getInstance() {
        return INSTANCE;
    }

    public void validateProperty(Object value, String fieldName) throws Exception {
        if (value == null) {
            throw new NoSuchElementException("The property '" + fieldName + "' is required and cannot be null.");
        }

        BiConsumer<Object, String> validator = validationStrategies.get(value.getClass());

        if (validator != null) {
            validator.accept(value, fieldName);
        } else {
            throw new NoSuchElementException("No validation strategy found for property '" + fieldName + "'.");
        }
    }
}
