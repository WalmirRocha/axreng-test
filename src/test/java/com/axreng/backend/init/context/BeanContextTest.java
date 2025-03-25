package com.axreng.backend.init.context;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

public class BeanContextTest {

    @Test
    void testGetContextReturnsSameInstance() {
        BeanContext context1 = BeanContext.getContext();
        BeanContext context2 = BeanContext.getContext();
        assertSame(context1, context2, "getContext should return the same instance");
    }

    @Test
    void testGetReturnsNullForNonRegisteredClass() {
        BeanContext context = BeanContext.getContext();
        assertNull(context.get(BeanContext.class), "Should return null for non-registered class");
    }

    @Test
    void testSetAndGetBean() {
        BeanContext context = BeanContext.getContext();
        String testString = "test string";
        context.set(String.class, testString);
        assertEquals(testString, context.get(String.class), "Should return the registered bean");
    }

    @Test
    void testSetAndGetDifferentBeans() {
        BeanContext context = BeanContext.getContext();
        String testString = "test string";
        Integer testInteger = 123;

        context.set(String.class, testString);
        context.set(Integer.class, testInteger);

        assertEquals(testString, context.get(String.class), "Should return the registered String bean");
        assertEquals(testInteger, context.get(Integer.class), "Should return the registered Integer bean");
    }
}