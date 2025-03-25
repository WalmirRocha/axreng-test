package com.axreng.backend.init.context;

import java.util.HashMap;
import java.util.Map;

public class BeanContext {

    private static BeanContext instance = new BeanContext();
    private Map<Class<?>, Object> registry = new HashMap<>();

    private BeanContext() {
    }

    public static BeanContext getContext() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz) {
        return (T) registry.get(clazz);
    }

    protected void set(Class<?> clazz, Object object) {
        registry.put(clazz, object);
    }
}
