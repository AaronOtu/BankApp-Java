package org.gs.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;



import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.sql.ResultSet;
import org.jboss.logging.Logger;

@ApplicationScoped
public class UserRepository {
    private Logger logger = Logger.getLogger(UserRepository.class);

    @Inject
    // DataSource dataSource;
    AgroalDataSource dataSource;

    public void getAllUsers() {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM users")) {

            while (resultSet.next()) {
                String userName = resultSet.getString("user_name");
                String email = resultSet.getString("email");
                logger.info("User: " + userName + ", Email: " + email);
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }

    }
}
