
package org.gs.repository;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.gs.dto.DepositRequest;
import org.gs.dto.Status;
import org.gs.dto.TransactionType;
import org.gs.model.Transactions;
import org.jboss.logging.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TransactionRepository {
    private static final Logger logger = Logger.getLogger(TransactionRepository.class);

    @Inject
    AgroalDataSource dataSource;

    @Inject
    AccountRepository accountRepository;

    @Transactional
    public Transactions deposit(DepositRequest request) {
        try {

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

    /*
     * @Transactional
     * public Transactions transfer(String fromAccountId, String toAccountId, double
     * amount, String description) {
     * // 1. Verify accounts exist
     * if (!accountRepository.accountExists(fromAccountId)) {
     * throw new WebApplicationException("Sender account not found",
     * Response.Status.NOT_FOUND);
     * }
     * if (!accountRepository.accountExists(toAccountId)) {
     * throw new WebApplicationException("Recipient account not found",
     * Response.Status.NOT_FOUND);
     * }
     * 
     * // 2. Verify sufficient balance
     * double currentBalance =
     * accountRepository.getAccount(fromAccountId).getBalance();
     * if (amount > currentBalance) {
     * throw new WebApplicationException("Insufficient funds",
     * Response.Status.BAD_REQUEST);
     * }
     * 
     * // 3. Perform transfer
     * accountRepository.updateBalance(fromAccountId, -amount);
     * accountRepository.updateBalance(toAccountId, amount);
     * 
     * // 4. Record transaction
     * return recordTransaction(
     * fromAccountId,
     * toAccountId,
     * amount,
     * TransactionType.TRANSFER,
     * "NGN",
     * generateReference(),
     * description,
     * "success");
     * }
     */

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

            // Get the generated transaction ID
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
}