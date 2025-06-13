package org.gs.dto;

public class AccountRequest {
     private String userId;
     private String accountNumber;

     public String getAccountNumber() {
        return accountNumber;
    }

     public void setAccountNumber(String accountNumber) {
         this.accountNumber = accountNumber;
     }

     public String getUserId() {
         return userId;
     }

     public void setUserId(String userId) {
         this.userId = userId;
     }

}
