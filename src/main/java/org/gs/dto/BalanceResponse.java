package org.gs.dto;

public class BalanceResponse {
    private double balance;
    private String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public BalanceResponse() {
    }

    public BalanceResponse(double balance, String userName) {
        this.balance = balance;
        this.userName = userName;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
