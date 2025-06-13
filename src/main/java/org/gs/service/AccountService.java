package org.gs.service;

import java.util.HashMap;
import java.util.Map;

import org.gs.exception.AccountNotFoundException;

import org.gs.dto.AccountRequest;
import org.gs.model.SavingsAccount;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AccountService {
    private final UserService userService;
    private final Map<String, SavingsAccount> accounts = new HashMap<>();

    @Inject
    public AccountService(UserService userService) {
        this.userService = userService;
    }

    public SavingsAccount createAccount(AccountRequest request) {
        var user = userService.getUser(request.getUserId());

        if (user == null) {
            throw new IllegalArgumentException("User not found: " + request.getUserId());
        }
        SavingsAccount account = new SavingsAccount();
        account.setFirstName(user.getFirstName());
        account.setLastName(user.getLastName());
        account.setUserId(user.getId());
        account.setCardDetails(request.getAccountNumber());
        account.setBalance(0.0);
        accounts.put(account.getUserId(), account);

        return account;
    }

    public SavingsAccount getAccount(String userId) {
        return accounts.get(userId);
    }


    public Map<String, SavingsAccount> getAllAccounts() {
        return accounts;
    }
    

    public SavingsAccount updateAccount(String userId, AccountRequest request) {
        if (!accounts.containsKey(userId)) {
            throw new AccountNotFoundException("Account not found for user: " + userId);
        }
        SavingsAccount account = accounts.get(userId);
        account.setCardDetails(request.getAccountNumber());
        return account;
    }


    public void deleteAccount(String userId){
        if (!accounts.containsKey(userId)) {
            throw new AccountNotFoundException("Account not found for user: " + userId);
        }
        accounts.remove(userId);
    }


}
