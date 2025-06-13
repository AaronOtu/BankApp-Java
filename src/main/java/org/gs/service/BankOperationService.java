package org.gs.service;

import java.util.HashMap;
import java.util.Map;

import org.gs.exception.AccountNotFoundException;
import org.gs.model.SavingsAccount;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BankOperationService {
    private final AccountService accountService;
     private final Map<String, SavingsAccount> accounts = new HashMap<>();

    public BankOperationService(AccountService accountService) {
        this.accountService = accountService;

    }

    public SavingsAccount deposit(String userId, Double amount) {
        SavingsAccount account = accountService.getAccount(userId);

        if (account == null) {
            throw new AccountNotFoundException("Account not found for user: " + userId);
        }

        account.deposit(amount);
        return account;
    }

}
