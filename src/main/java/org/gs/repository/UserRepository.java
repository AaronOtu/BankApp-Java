package org.gs.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.sql.ResultSet;

import org.gs.dto.UserRequest;
import org.gs.model.User;
import org.jboss.logging.Logger;

@ApplicationScoped
public class UserRepository {
    private Logger logger = Logger.getLogger(UserRepository.class);

    @Inject
    AgroalDataSource dataSource;

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement
                        .executeQuery("SELECT  * FROM users")) {

            while (resultSet.next()) {
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String email = resultSet.getString("email");
                // Timestamp createdAt = resultSet.getTimestamp("created_at");

                User user = new User();
                user.setId(String.valueOf(resultSet.getInt("id")));
                user.setCreatedAt(resultSet.getString("created_at"));
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setEmail(email);
                // user.setCreatedAt(createdAt);
                users.add(user);

                String userName = firstName + "" + lastName;
                logger.info("User: " + userName + ", Email: " + email);
            }
            System.out.println("Database connection established successfully.");
        } catch (SQLException e) {
            logger.error("Error while fetching users: " + e.getMessage());
            if (e.getMessage().contains("Duplicate entry")) {
                throw new WebApplicationException("Email already exists", Response.Status.CONFLICT);
            }
            throw new WebApplicationException("Internal error: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);

        }

        return users;

    }

    public User addUser(UserRequest user) {
        String sql = "INSERT INTO users (first_name,last_name, email) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());

            String userN = user.getFirstName() + " " + user.getLastName();
            ps.executeUpdate();

            ResultSet generatedKeys = ps.getGeneratedKeys();
            int generatedId = -1;
            if (generatedKeys.next()) {
                generatedId = generatedKeys.getInt(1);
            }

            String selectSql = "SELECT * FROM users WHERE id = ?";
            try (PreparedStatement selectPs = connection.prepareStatement(selectSql)) {
                selectPs.setInt(1, generatedId);
                ResultSet rs = selectPs.executeQuery();
                if (rs.next()) {
                    User createdUser = new User();
                    createdUser.setId(String.valueOf(rs.getInt("id")));
                    createdUser.setFirstName(rs.getString("first_name"));
                    createdUser.setLastName(rs.getString("last_name"));
                    createdUser.setEmail(rs.getString("email"));
                    createdUser.setCreatedAt(rs.getString("created_at"));
                    return createdUser;
                }
            }
            logger.info("User added successfully: " + userN);

        } catch (SQLException e) {
            logger.error("Error while adding user: " + e.getMessage());
            if (e.getMessage().contains("Duplicate entry")) {
                throw new WebApplicationException("Email already exists", Response.Status.CONFLICT);
            }
            throw new WebApplicationException("Internal error: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
        return null;

    }

}
