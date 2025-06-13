package org.gs.model;

public class SavingsAccount implements Account{
    private String firstName;
    private String lastName;
    private double balance = 0.0;
    private String cardDetails;
    private String userId;



    public String getCardDetails() {
        return cardDetails;
    }

    public void setCardDetails(String cardDetails) {
        this.cardDetails = cardDetails;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }



    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

  public double setBalance(double balance) {
        this.balance = balance;
        return this.balance;
    }

    public SavingsAccount() {
    }

    public SavingsAccount(String firstName, String lastName, String cardDetails, String userId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.cardDetails = cardDetails;
        this.userId = userId;
    }


   public SavingsAccount(String firstName, String lastName,String cardDetails ){
          this.firstName = firstName;
          this.lastName = lastName;
          this.cardDetails = cardDetails;
    }


    @Override
    public Double getBalance() {
        return balance;
    }

    @Override
    public void deposit(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("You can't deposit less than 0");
        }
        balance += amount;
        System.out.println("You have deposited " + amount + " current balance " + balance);

    }

    @Override
    public boolean withdraw(double amount) {
        if (amount > balance) {
            throw new IllegalArgumentException("You can't withdraw " + amount + " your balance is " + balance);
        }
        balance -= amount;
        System.out.println("You have withdraw " + amount + " your balance is " + balance);
        return true;
    }

    @Override
    public Double transfer(double amount,String phoneNumber) {
        if (amount > balance) {
            throw new IllegalArgumentException("You can't send " + amount + " your balance is " + balance);
        }
        var initial = balance;
        balance -= amount;
        System.out.println("You have sent " + amount + " to " + phoneNumber + " initial balance " + initial + " your current balance is " + balance);
        return amount;
    }
}
