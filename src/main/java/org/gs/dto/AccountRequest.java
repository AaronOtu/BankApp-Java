package org.gs.dto;

public class AccountRequest {
    private String userId;
    //private String accountNumber;
    private AccountType accountType;

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    // public String getAccountNumber() {
    //     return accountNumber;
    // }

    // public void setAccountNumber(String accountNumber) {
    //     this.accountNumber = accountNumber;
    // }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

}
