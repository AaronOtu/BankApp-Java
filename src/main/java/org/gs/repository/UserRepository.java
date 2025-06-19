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
    static final String FIRST_NAME = "first_name";
    static final String LAST_NAME = "last_name";
    static final String EMAIL = "email";
    static final String CREATED_AT = "created_at";
    static final String ID = "id";

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement
                        .executeQuery("SELECT  * FROM users")) {

            while (resultSet.next()) {
                String firstName = resultSet.getString(FIRST_NAME);
                String lastName = resultSet.getString(LAST_NAME);
                String email = resultSet.getString(EMAIL);

                User user = new User();
                user.setId(String.valueOf(resultSet.getInt(ID)));
                user.setCreatedAt(resultSet.getString(CREATED_AT));
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setEmail(email);

                users.add(user);

                String userName = firstName + " " + lastName;
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
                    createdUser.setFirstName(rs.getString(FIRST_NAME));
                    createdUser.setLastName(rs.getString(LAST_NAME));
                    createdUser.setEmail(rs.getString(EMAIL));
                    createdUser.setCreatedAt(rs.getString(CREATED_AT));
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

    public User getUser(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(String.valueOf(rs.getInt(ID)));
                user.setFirstName(rs.getString(FIRST_NAME));
                user.setLastName(rs.getString(LAST_NAME));
                user.setEmail(rs.getString(EMAIL));
                user.setCreatedAt(rs.getString(CREATED_AT));
                return user;
            } else {
                throw new WebApplicationException("User not found", Response.Status.NOT_FOUND);
            }
        } catch (SQLException e) {
            logger.error("Error while fetching user: " + e.getMessage());
            throw new WebApplicationException("Internal error: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteUser(String id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new WebApplicationException("User not found", Response.Status.NOT_FOUND);
            }
        } catch (SQLException e) {
            logger.error("Error while deleting user: " + e.getMessage());
            throw new WebApplicationException("Internal error: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public User updateUser(String id, UserRequest user) {
        String sql = "UPDATE users SET first_name = ?, last_name = ?, email = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, id);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new WebApplicationException("User not found", Response.Status.NOT_FOUND);
            }
            return getUser(id);
        } catch (SQLException e) {
            logger.error("Error while updating user: " + e.getMessage());
            throw new WebApplicationException("Internal error: " + e.getMessage(),
                    Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
    public boolean userExists(String userId) {
    String sql = "SELECT 1 FROM users WHERE id = ?";
    try (Connection conn = dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, userId);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    } catch (Exception e) {
        e.printStackTrace();
        return false;
    }
}



}
