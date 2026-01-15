package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.models.InventoryLog;
import com.amalitech.smartecommerce.models.InventoryLog.ChangeType;
import com.amalitech.smartecommerce.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for InventoryLog entity.
 * Tracks all inventory changes for auditing purposes.
 * Candidate for NoSQL storage for log analysis.
 */
public class InventoryLogDAO {

    /**
     * Creates a new inventory log entry.
     */
    public void create(InventoryLog log) throws SQLException {
        String sql = "INSERT INTO inventory_logs (product_id, change_amount, previous_quantity, " +
                     "new_quantity, change_type, reason, performed_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, log.getProductId());
            stmt.setInt(2, log.getChangeAmount());
            stmt.setInt(3, log.getPreviousQuantity());
            stmt.setInt(4, log.getNewQuantity());
            stmt.setString(5, log.getChangeType().getValue());
            stmt.setString(6, log.getReason());
            
            if (log.getPerformedBy() != null) {
                stmt.setInt(7, log.getPerformedBy());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    log.setLogId(generatedKeys.getInt(1));
                }
            }
        }
    }

    /**
     * Finds all logs for a specific product.
     */
    public List<InventoryLog> findByProductId(int productId) throws SQLException {
        List<InventoryLog> logs = new ArrayList<>();
        String sql = "SELECT il.*, p.name as product_name, " +
                     "CONCAT(u.first_name, ' ', u.last_name) as performed_by_name " +
                     "FROM inventory_logs il " +
                     "LEFT JOIN products p ON il.product_id = p.product_id " +
                     "LEFT JOIN users u ON il.performed_by = u.user_id " +
                     "WHERE il.product_id = ? ORDER BY il.change_date DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToInventoryLog(rs));
                }
            }
        }
        return logs;
    }

    /**
     * Finds all logs by change type.
     */
    public List<InventoryLog> findByChangeType(ChangeType changeType) throws SQLException {
        List<InventoryLog> logs = new ArrayList<>();
        String sql = "SELECT il.*, p.name as product_name, " +
                     "CONCAT(u.first_name, ' ', u.last_name) as performed_by_name " +
                     "FROM inventory_logs il " +
                     "LEFT JOIN products p ON il.product_id = p.product_id " +
                     "LEFT JOIN users u ON il.performed_by = u.user_id " +
                     "WHERE il.change_type = ? ORDER BY il.change_date DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, changeType.getValue());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToInventoryLog(rs));
                }
            }
        }
        return logs;
    }

    /**
     * Finds logs within a date range.
     */
    public List<InventoryLog> findByDateRange(Timestamp startDate, Timestamp endDate) throws SQLException {
        List<InventoryLog> logs = new ArrayList<>();
        String sql = "SELECT il.*, p.name as product_name, " +
                     "CONCAT(u.first_name, ' ', u.last_name) as performed_by_name " +
                     "FROM inventory_logs il " +
                     "LEFT JOIN products p ON il.product_id = p.product_id " +
                     "LEFT JOIN users u ON il.performed_by = u.user_id " +
                     "WHERE il.change_date BETWEEN ? AND ? ORDER BY il.change_date DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, startDate);
            stmt.setTimestamp(2, endDate);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToInventoryLog(rs));
                }
            }
        }
        return logs;
    }

    /**
     * Returns all inventory logs.
     */
    public List<InventoryLog> findAll() throws SQLException {
        List<InventoryLog> logs = new ArrayList<>();
        String sql = "SELECT il.*, p.name as product_name, " +
                     "CONCAT(u.first_name, ' ', u.last_name) as performed_by_name " +
                     "FROM inventory_logs il " +
                     "LEFT JOIN products p ON il.product_id = p.product_id " +
                     "LEFT JOIN users u ON il.performed_by = u.user_id " +
                     "ORDER BY il.change_date DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                logs.add(mapResultSetToInventoryLog(rs));
            }
        }
        return logs;
    }

    /**
     * Gets the most recent logs (for dashboard).
     */
    public List<InventoryLog> findRecent(int limit) throws SQLException {
        List<InventoryLog> logs = new ArrayList<>();
        String sql = "SELECT il.*, p.name as product_name, " +
                     "CONCAT(u.first_name, ' ', u.last_name) as performed_by_name " +
                     "FROM inventory_logs il " +
                     "LEFT JOIN products p ON il.product_id = p.product_id " +
                     "LEFT JOIN users u ON il.performed_by = u.user_id " +
                     "ORDER BY il.change_date DESC LIMIT ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToInventoryLog(rs));
                }
            }
        }
        return logs;
    }

    /**
     * Logs a stock change and updates the product stock in a transaction.
     */
    public void logStockChange(int productId, int changeAmount, ChangeType changeType, 
                               String reason, Integer performedBy) throws SQLException {
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            
            // Get current stock
            int currentStock = 0;
            String getStockSql = "SELECT stock_quantity FROM products WHERE product_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(getStockSql)) {
                stmt.setInt(1, productId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        currentStock = rs.getInt("stock_quantity");
                    }
                }
            }
            
            int newStock = currentStock + changeAmount;
            if (newStock < 0) {
                throw new SQLException("Insufficient stock. Current: " + currentStock + ", Change: " + changeAmount);
            }
            
            // Update product stock
            String updateStockSql = "UPDATE products SET stock_quantity = ? WHERE product_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateStockSql)) {
                stmt.setInt(1, newStock);
                stmt.setInt(2, productId);
                stmt.executeUpdate();
            }
            
            // Create log entry
            String logSql = "INSERT INTO inventory_logs (product_id, change_amount, previous_quantity, " +
                           "new_quantity, change_type, reason, performed_by) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(logSql)) {
                stmt.setInt(1, productId);
                stmt.setInt(2, changeAmount);
                stmt.setInt(3, currentStock);
                stmt.setInt(4, newStock);
                stmt.setString(5, changeType.getValue());
                stmt.setString(6, reason);
                if (performedBy != null) {
                    stmt.setInt(7, performedBy);
                } else {
                    stmt.setNull(7, Types.INTEGER);
                }
                stmt.executeUpdate();
            }
            
            conn.commit();
            
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Maps a ResultSet row to an InventoryLog object.
     */
    private InventoryLog mapResultSetToInventoryLog(ResultSet rs) throws SQLException {
        InventoryLog log = new InventoryLog(
            rs.getInt("log_id"),
            rs.getInt("product_id"),
            rs.getInt("change_amount"),
            rs.getInt("previous_quantity"),
            rs.getInt("new_quantity"),
            ChangeType.fromString(rs.getString("change_type")),
            rs.getTimestamp("change_date") != null ? rs.getTimestamp("change_date").toLocalDateTime() : null,
            rs.getString("reason"),
            rs.getObject("performed_by") != null ? rs.getInt("performed_by") : null
        );
        
        // Set transient fields if available
        try {
            log.setProductName(rs.getString("product_name"));
            log.setPerformedByName(rs.getString("performed_by_name"));
        } catch (SQLException ignored) {
            // These columns may not be present in all queries
        }
        
        return log;
    }
}
