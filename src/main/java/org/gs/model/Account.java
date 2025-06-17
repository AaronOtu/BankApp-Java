package org.gs.model;

public interface Account {
   Double getBalance();

   void deposit(double amount);

   boolean withdraw(double amount);

   Double transfer(double amount, String phoneNumber);

}
