package org.gs.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.gs.dto.AccountRequest;
import org.gs.dto.AccountResponse;
import org.gs.model.User;
import org.jboss.logging.Logger;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class AccountRepository {
    private final UserRepository userRepository;

    private Logger logger = Logger.getLogger(AccountRepository.class);

    public AccountRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Inject
    AgroalDataSource dataSource;

    public AccountResponse createAccount(AccountRequest request) {

        if (!userRepository.userExists(request.getUserId())) {
            throw new IllegalArgumentException("User does not exist.");
        }
        User user = userRepository.getUser(request.getUserId());
        String userName = user.getFirstName() + " " + user.getLastName();
        String sql = "INSERT INTO accounts (user_id, account_number,account_type, balance) VALUES (?,?,?,?)";
        try (Connection cnn = dataSource.getConnection();
                PreparedStatement ps = cnn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, request.getUserId());
            ps.setString(2, /* request.getAccountNumber() */generateAccountNumber());
            ps.setString(3, request.getAccountType().getValue().toLowerCase());
            ps.setDouble(4, 0.0);
            ps.executeUpdate();

            ResultSet generatedKeys = ps.getGeneratedKeys();
            int generatedId = -1;
            if (generatedKeys.next()) {
                generatedId = generatedKeys.getInt(1);
            }
            String selectSql = "SELECT * FROM accounts WHERE id = ?";
            try (PreparedStatement selectPs = cnn.prepareStatement(selectSql)) {
                selectPs.setInt(1, generatedId);
                ResultSet rs = selectPs.executeQuery();
                if (rs.next()) {
                    AccountResponse response = new AccountResponse();
                    response.setId(String.valueOf(rs.getInt("id")));
                    response.setUserId(rs.getString("user_id"));
                    response.setUserName(userName);
                    response.setAccountNumber(rs.getString("account_number"));
                    response.setAccountType(rs.getString("account_type"));
                    response.setCreatedAt(rs.getString("created_at"));
                    response.setBalance(rs.getDouble("balance"));

                    return response;

                }
                logger.info("User added successfully: " + userName);

            }

        } catch (SQLException e) {
            logger.error("Error while creating account", e);
            if (e.getMessage().contains("Duplicate entry")) {
                throw new WebApplicationException("Email already exists", Response.Status.CONFLICT);
            }
            throw new WebApplicationException("Internal error: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);

        }
        return null;

    }

    // TODO: join the accounts and users table to fetch userName
    public List<AccountResponse> getAllAccounts() {
        List<AccountResponse> accounts = new ArrayList<>();

        String sql = "SELECT * FROM accounts ";
        try (Connection cnn = dataSource.getConnection();
                Statement statement = cnn.createStatement();
                ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                AccountResponse response = new AccountResponse();
                response.setId(String.valueOf(rs.getInt("id")));
                response.setUserId(rs.getString("user_id"));
                // response.setUserName(userName);
                response.setAccountNumber(rs.getString("account_number"));
                response.setAccountType(rs.getString("account_type"));
                response.setCreatedAt(rs.getString("created_at"));
                response.setBalance(rs.getDouble("balance"));
                accounts.add(response);

            }
        } catch (SQLException e) {
            logger.error("Error while getting all accounts", e);
            throw new WebApplicationException("Internal error: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);

        }
        return accounts;
    }

    public AccountResponse getAccount(String id) {
        String sql = "SELECT * FROM accounts WHERE id = ?";
        try (Connection cnn = dataSource.getConnection();
                PreparedStatement ps = cnn.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                AccountResponse response = new AccountResponse();
                response.setId(String.valueOf(rs.getInt("id")));
                response.setUserId(rs.getString("user_id"));
                response.setAccountNumber(rs.getString("account_number"));
                response.setAccountType(rs.getString("account_type"));
                response.setCreatedAt(rs.getString("created_at"));
                response.setBalance(rs.getDouble("balance"));
                return response;
            } else {
                throw new WebApplicationException("Account not found", Response.Status.NOT_FOUND);
            }
        } catch (SQLException e) {
            logger.error("Error while fetching user: " + e.getMessage());
            throw new WebApplicationException("Internal error: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }

    }

    // TODO: add delete message respone
    public String deleteAccount(String id) {
        String sql = "DELETE FROM accounts WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new WebApplicationException("Account not found", Response.Status.NOT_FOUND);
            }
            return "Account deleted successfully";
        } catch (SQLException e) {
            logger.error("Error while deleting account: " + e.getMessage());
            throw new WebApplicationException("Internal error: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public boolean accountExists(String accountId) {
        String sql = "SELECT 1 FROM accounts WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            logger.error("Error checking account existence: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public void updateBalance(String accountId, double amount) {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setString(2, accountId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new WebApplicationException("Account not found", Response.Status.NOT_FOUND);
            }
        } catch (SQLException e) {
            logger.error("Error updating balance: " + e.getMessage());
            throw new WebApplicationException("Failed to update balance", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public String getAccountIdByAccountNumber(String accountNumber) {
        String sql = "SELECT id FROM accounts WHERE account_number = ?";
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("id");
            }
        } catch (SQLException e) {
            logger.error("Error fetching account ID by account number: " + e.getMessage());
        }
        return null;
    }

    private String generateAccountNumber() {
        Random random = new Random();
        long number = Math.abs(random.nextLong() % 1_000_000_0000L);
        return String.format("%010d", number);
    }
}
