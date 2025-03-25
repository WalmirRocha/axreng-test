package com.axreng.backend.vo;

public enum Status {

    ACTIVE("active"),
    DONE("done");

    private final String Value;

    Status(String value) {
        this.Value = value;
    }

    public String getValue() {
        return Value;
    }

    public static Status getStatusByValue(String value) {
        for (Status status : values()) {
            if (value.equals(status.Value)) {
                return status;
            }
        }
        return null;
    }
}
