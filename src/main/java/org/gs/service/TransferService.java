package org.gs.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import org.gs.model.Transfer;
import org.jboss.logging.Logger;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class TransferService {
     private static final Logger logger = Logger.getLogger(TransferService.class);

    @Inject
    AgroalDataSource dataSource;

   public Transfer save(Transfer transfer) {
    logger.info("Saving transfer: " + transfer);
        String sql = "INSERT INTO transactions (" +
                "account_id, to_account_id, transaction_type, " +
                "amount, converted_amount, currency, to_currency, exchange_rate, " +
                "reference, description, status, transaction_date" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, transfer.getFromAccountId());
            ps.setString(2, transfer.getToAccountId());
            ps.setString(3, transfer.getType());
            ps.setDouble(4, transfer.getAmount());
            ps.setDouble(5, transfer.getConvertedAmount());
            ps.setString(6, transfer.getFromCurrency());
            ps.setString(7, transfer.getToCurrency());
            ps.setDouble(8, transfer.getExchangeRate());
            ps.setString(9, transfer.getReference());
            ps.setString(10, transfer.getDescription());
            ps.setString(11, transfer.getStatus().getValue().toLowerCase());
            ps.setTimestamp(12, Timestamp.valueOf(transfer.getTransactionDate()));

            ps.executeUpdate();

            // Get the generated transaction ID
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    transfer.setId(rs.getString(1));
                    return transfer;
                }
            }
        } catch (SQLException e) {
            logger.error("Error saving transaction: " + e.getMessage());
            throw new WebApplicationException("Failed to save transaction",
                    Response.Status.INTERNAL_SERVER_ERROR);
        }

        throw new WebApplicationException("Failed to retrieve created transaction",
                Response.Status.INTERNAL_SERVER_ERROR);
    }

}
