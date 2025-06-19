package org.gs.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.gs.dto.AccountRequest;
import org.gs.dto.AccountResponse;
import org.gs.model.User;
import org.jboss.logging.Logger;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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
            ps.setString(2, request.getAccountNumber());
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

}
