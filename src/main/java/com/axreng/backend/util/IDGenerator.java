package com.axreng.backend.util;

import java.security.SecureRandom;

public class IDGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static SecureRandom random;
    private static IDGenerator instance;

    private IDGenerator() {
        random = new SecureRandom();
    }

    public static IDGenerator getInstance() {
        if (instance == null) {
            synchronized (IDGenerator.class) {
                if (instance == null) {
                    instance = new IDGenerator();
                }
            }
        }
        return instance;
    }

    public String getId(int length) throws Exception {
        synchronized (this) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; i++) {
                sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
            }
            return sb.toString();
        }
    }
}
