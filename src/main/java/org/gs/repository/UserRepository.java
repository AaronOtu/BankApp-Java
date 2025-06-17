package org.gs.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.ResultSet;
import org.jboss.logging.Logger;

@ApplicationScoped
public class UserRepository {
    private Logger logger = Logger.getLogger(UserRepository.class);

    @Inject
    AgroalDataSource dataSource;

    public void getAllUsers() {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement
                        .executeQuery("SELECT first_name, last_name, email, created_at FROM users")) {

            while (resultSet.next()) {
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String email = resultSet.getString("email");
                String userName = firstName + "" + lastName;
                System.out.println("Database connection established successfully.");
                logger.info("User: " + userName + ", Email: " + email);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Error while fetching users: " + e.getMessage());
        }

    }

    public void addUser(String firstName, String lastName, String email) {
        String sql = "INSERT INTO users (first_name,last_name, email) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, email);
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            String userN = firstName + " " + lastName;
            ps.executeUpdate();
            logger.info("User added successfully: " + userN);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Error while adding user: " + e.getMessage());
        }
    }

}
