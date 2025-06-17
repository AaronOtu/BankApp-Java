package org.gs.service;

import org.gs.exception.AccountNotFoundException;
import org.gs.model.SavingsAccount;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BankOperationService {
    private final AccountService accountService;

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
