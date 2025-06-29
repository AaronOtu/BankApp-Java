
package org.gs.repository;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.gs.dto.AccountResponse;
import org.gs.dto.BalanceResponse;
import org.gs.dto.DepositRequest;
import org.gs.dto.Status;
import org.gs.dto.TransactionType;
import org.gs.dto.TransferRequest;
import org.gs.dto.TransferResult;
import org.gs.model.Transactions;
import org.gs.model.Transfer;
import org.gs.service.ExchangeService;
import org.gs.service.TransferService;
import org.jboss.logging.Logger;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TransactionRepository {
    private static final Logger logger = Logger.getLogger(TransactionRepository.class);

    private final ExchangeService exchangeService;

    @Inject
    AgroalDataSource dataSource;

    @Inject
    AccountRepository accountRepository;

    @Inject
    TransferService transferService;

    @Inject
    public TransactionRepository(AccountRepository accountRepository,
            ExchangeService exchangeService) {
        this.accountRepository = accountRepository;
        this.exchangeService = exchangeService;
    }

    @Transactional
    public Transactions deposit(DepositRequest request) {
        try {
            double depositAmount = request.getAmount();
            if (depositAmount <= 0) {
                throw new WebApplicationException(
                        Response.status(Response.Status.BAD_REQUEST).entity("Invalid deposit amount").build());
            }

            accountRepository.updateBalance(request.getAccountId(), request.getAmount());

            return recordTransaction(
                    request.getAccountId(),
                    TransactionType.DEPOSIT,
                    request.getAmount(),
                    request.getCurrency(),
                    generateReference(),
                    request.getDescription(),
                    Status.SUCCESS);
        } catch (Exception e) {
            logger.error("Error depositing money", e);
            return recordTransaction(
                    request.getAccountId(),
                    TransactionType.DEPOSIT,
                    request.getAmount(),
                    request.getCurrency(),
                    generateReference(),
                    "Failed: " + e.getMessage(),
                    Status.FAILED);

        }

    }

    @Transactional
    public Transactions withdraw(DepositRequest request) {
        try {

            double currentBalance = accountRepository.getAccount(request.getAccountId()).getBalance();
            if (request.getAmount() > currentBalance) {
                throw new WebApplicationException("Insufficient funds", Response.Status.BAD_REQUEST);
            }

            accountRepository.updateBalance(request.getAccountId(), -request.getAmount());

            return recordTransaction(
                    request.getAccountId(),
                    TransactionType.WITHDRAWAL,
                    request.getAmount(),
                    request.getCurrency(),
                    generateReference(),
                    request.getDescription(),
                    Status.SUCCESS);

        } catch (Exception e) {
            logger.error("Error depositing money", e);
            return recordTransaction(
                    request.getAccountId(),
                    TransactionType.DEPOSIT,
                    request.getAmount(),
                    request.getCurrency(),
                    generateReference(),
                    "Failed: " + e.getMessage(),
                    Status.FAILED);

        }

    }

    @Transactional
    public TransferResult transfer(TransferRequest request) {
        // Verify accounts exist
        String fromAccountId = accountRepository.getAccountIdByAccountNumber(request.getFromAccount());
        String toAccountId = accountRepository.getAccountIdByAccountNumber(request.getToAccount());
        double amount = request.getAmount();
        String description = request.getDescription();

        if (fromAccountId == null) {
            throw new WebApplicationException("Source account not found", Response.Status.NOT_FOUND);
        }
        if (toAccountId == null) {
            throw new WebApplicationException("Destination account not found", Response.Status.NOT_FOUND);
        }
        if (amount <= 0) {
            throw new WebApplicationException("Invalid amount", Response.Status.BAD_REQUEST);
        }

        // Verifying if balance is sufficient
        AccountResponse fromAccount = accountRepository.getAccount(fromAccountId);
        if (amount > fromAccount.getBalance()) {
            throw new WebApplicationException("Insufficient funds", Response.Status.BAD_REQUEST);
        }

        double exchangeRate = exchangeService.getExchangeRate(
                request.getFromCurrency(),
                request.getToCurrency());
        double convertedAmount = amount * exchangeRate;

        String reference = generateReference();
        LocalDateTime now = LocalDateTime.now();

        Transfer outgoingTx = recordTransaction(
                fromAccountId,
                toAccountId,
                amount,
                convertedAmount,
                request.getFromCurrency(),
                request.getToCurrency(),
                exchangeRate,
                TransactionType.TRANSFER_OUT,
                reference,
                description,
                Status.SUCCESS, // Start as pending
                now);

        Transfer incomingTx = recordTransaction(
                toAccountId,
                fromAccountId,
                convertedAmount,
                amount,
                request.getToCurrency(),
                request.getFromCurrency(),
                exchangeRate,
                TransactionType.TRANSFER_IN,
                reference,
                description,
                Status.SUCCESS,
                now);

        try {
            // 6. Perform transfer (deduct from source, add to destination)
            accountRepository.updateBalance(fromAccountId, -amount);
            accountRepository.updateBalance(toAccountId, convertedAmount);

            /*
             * // 7. Update transaction statuses to SUCCESS
             * 
             * outgoingTx.setStatus(Status.SUCCESS);
             * incomingTx.setStatus(Status.SUCCESS);
             */

            return new TransferResult(outgoingTx);
        } catch (Exception e) {

            outgoingTx.setStatus(Status.FAILED);
            incomingTx.setStatus(Status.FAILED);

            throw new WebApplicationException("Transfer failed: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private Transfer recordTransaction(
            String fromAccountId,
            String toAccountId,
            double amount,
            double convertedAmount,
            String fromCurrency,
            String toCurrency,
            double exchangeRate,
            TransactionType type,
            String reference,
            String description,
            Status status,
            LocalDateTime timestamp) {

        Transfer transaction = new Transfer();
        transaction.setFromAccountId(fromAccountId);
        transaction.setToAccountId(toAccountId);
        transaction.setAmount(amount);
        transaction.setConvertedAmount(convertedAmount);
        transaction.setFromCurrency(fromCurrency);
        transaction.setToCurrency(toCurrency);
        transaction.setExchangeRate(exchangeRate);
        transaction.setType(type.getValue().toLowerCase());
        transaction.setReference(reference);
        transaction.setDescription(description);
        transaction.setStatus(status);
        transaction.setTransactionDate(timestamp);

        return transferService.save(transaction);
    }

    public List<Transactions> getTransactionsByAccount(String accountId) {
        List<Transactions> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = ?  ORDER BY transaction_date DESC";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                transactions.add(mapTransaction(rs));
            }
        } catch (SQLException e) {
            logger.error("Error fetching transactions: " + e.getMessage());
            throw new WebApplicationException("Failed to fetch transactions", Response.Status.INTERNAL_SERVER_ERROR);
        }

        return transactions;
    }

    private Transactions recordTransaction(
            String accountId,
            TransactionType transactionType,
            double amount,
            String currency,
            String reference,
            String description,
            Status status) {
        String sql = "INSERT INTO transactions (account_id, transaction_type, " +
                "amount, currency, reference, description, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, accountId);
            ps.setString(2, transactionType.getValue().toLowerCase());
            ps.setDouble(3, amount);
            ps.setString(4, currency);
            ps.setString(5, reference);
            ps.setString(6, description);
            ps.setString(7, status.getValue().toLowerCase());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int transactionId = rs.getInt(1);
                    return getTransactionById(transactionId);
                }
            }
        } catch (SQLException e) {
            logger.error("Error recording transaction: " + e.getMessage());
            throw new WebApplicationException("Failed to record transaction", Response.Status.INTERNAL_SERVER_ERROR);
        }

        throw new WebApplicationException("Failed to retrieve created transaction",
                Response.Status.INTERNAL_SERVER_ERROR);
    }

    private Transactions getTransactionById(int transactionId) {
        String sql = "SELECT * FROM transactions WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, transactionId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapTransaction(rs);
            }
        } catch (SQLException e) {
            logger.error("Error fetching transaction: " + e.getMessage());
        }

        return null;
    }

    private Transactions mapTransaction(ResultSet rs) throws SQLException {
        Transactions transaction = new Transactions();
        transaction.setId(String.valueOf(rs.getInt("id")));
        transaction.setAccountId(rs.getString("account_id"));
        transaction.setTransactionType(rs.getString("transaction_type"));
        transaction.setAmount(rs.getDouble("amount"));
        transaction.setCurrency(rs.getString("currency"));
        transaction.setReference(rs.getString("reference"));
        transaction.setDescription(rs.getString("description"));
        transaction.setStatus(rs.getString("status"));
        transaction.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());
        return transaction;
    }

    private String generateReference() {
        return "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // TODO add username to the response
    public BalanceResponse getBalance(String accountId) {
        String sql = "SELECT * FROM accounts WHERE id = ?";
        try (Connection cnn = dataSource.getConnection();
                PreparedStatement ps = cnn.prepareStatement(sql)) {

            ps.setString(1, accountId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String userId = rs.getString("user_id");

                String userSql = "SELECT first_name, last_name FROM users WHERE id = ?";
                try (PreparedStatement userPs = cnn.prepareStatement(userSql)) {
                    userPs.setString(1, userId);
                    ResultSet userRs = userPs.executeQuery();
                    String userName = "";
                    if (userRs.next()) {
                        userName = userRs.getString("first_name") + " " + userRs.getString("last_name");
                    }
                    BalanceResponse response = new BalanceResponse();
                    response.setUserName(userName);
                    response.setBalance(rs.getDouble("balance"));
                    return response;
                }

            } else {
                throw new WebApplicationException("Account not found", Response.Status.NOT_FOUND);
            }
        } catch (SQLException e) {
            logger.error("Error while fetching user: " + e.getMessage());
            throw new WebApplicationException("Internal error: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }

    }

}