package org.gs.dto;

public enum AccountType {
    SAVINGS("savings"),
    CURRENT("current");

     private final String value;

    AccountType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
