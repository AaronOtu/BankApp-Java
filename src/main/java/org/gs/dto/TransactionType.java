package org.gs.dto;

public enum TransactionType {
    DEPOSIT("deposit"),
    WITHDRAWAL("withdrawal"),
    TRANSFER_IN("transfer_in"),
    TRANSFER_OUT("transfer_out");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
