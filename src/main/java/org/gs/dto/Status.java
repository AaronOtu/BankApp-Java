package org.gs.dto;

public enum Status {
    SUCCESS("success"),
    FAILED("failed"),
    PENDING("pending");


    private final String value;

    Status(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
