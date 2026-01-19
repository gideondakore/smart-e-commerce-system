package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.models.Product;
import com.amalitech.smartecommerce.models.InventoryLog.ChangeType;
import com.amalitech.smartecommerce.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public void create(Product product) throws SQLException {
        String sql =
                "INSERT INTO products (category_id, name, price, stock_quantity) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (product.getCategoryId() > 0) {
                stmt.setInt(1, product.getCategoryId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, product.getName());
            stmt.setDouble(3, product.getPrice());
            stmt.setInt(4, product.getStockQuantity());

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setProductId(generatedKeys.getInt(1));
                    
                    // Log initial stock
                    if (product.getStockQuantity() > 0) {
                        String logSQL = "INSERT INTO inventory_logs (product_id, change_amount, previous_quantity, new_quantity, change_type, reason) VALUES (?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement logStmt = conn.prepareStatement(logSQL)) {
                            logStmt.setInt(1, product.getProductId());
                            logStmt.setInt(2, product.getStockQuantity());
                            logStmt.setInt(3, 0);
                            logStmt.setInt(4, product.getStockQuantity());
                            logStmt.setString(5, ChangeType.RESTOCK.getValue());
                            logStmt.setString(6, "Initial stock");
                            logStmt.executeUpdate();
                        }
                    }
                }
            }
        }
    }

    public Product findById(int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
        }
        return null;
    }

    public List<Product> findAll() throws SQLException {
//        IO.println("=======GETTING ALL PRODUCT=========");
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    public List<Product> findByCategoryId(int categoryId) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE category_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }

    /**
     * Searches products by name using SQL LIKE operator.
     * Case-insensitive search leveraging database index on LOWER(name).
     */
    public List<Product> searchByName(String query) throws SQLException {
        List<Product> products = new ArrayList<>();
        // Using LOWER() for case-insensitive search, leverages idx_products_name_lower index
        String sql = "SELECT * FROM products WHERE LOWER(name) LIKE LOWER(?) ORDER BY name";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + query + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }

    /**
     * Searches products by name or description.
     */
    public List<Product> searchByNameOrDescription(String query) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE LOWER(name) LIKE LOWER(?) " +
                "OR LOWER(description) LIKE LOWER(?) ORDER BY name";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + query + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }

    /**
     * Finds products within a price range.
     */
    public List<Product> findByPriceRange(double minPrice, double maxPrice) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE price BETWEEN ? AND ? ORDER BY price";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, minPrice);
            stmt.setDouble(2, maxPrice);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }

    /**
     * Finds products with low stock.
     */
    public List<Product> findLowStock(int threshold) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE stock_quantity > 0 AND stock_quantity <= ? ORDER BY stock_quantity";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, threshold);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }

    /**
     * Counts total number of products.
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public void update(Product product) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            
            // Get current stock
            int oldStock = 0;
            String getStockSQL = "SELECT stock_quantity FROM products WHERE product_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(getStockSQL)) {
                stmt.setInt(1, product.getProductId());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        oldStock = rs.getInt("stock_quantity");
                    }
                }
            }
            
            // Update product
            String sql = "UPDATE products SET category_id = ?, name = ?, price = ?, stock_quantity = ? WHERE product_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (product.getCategoryId() > 0) {
                    stmt.setInt(1, product.getCategoryId());
                } else {
                    stmt.setNull(1, Types.INTEGER);
                }
                stmt.setString(2, product.getName());
                stmt.setDouble(3, product.getPrice());
                stmt.setInt(4, product.getStockQuantity());
                stmt.setInt(5, product.getProductId());
                stmt.executeUpdate();
            }
            
            // Log stock change if different
            if (oldStock != product.getStockQuantity()) {
                int change = product.getStockQuantity() - oldStock;
                ChangeType changeType = change > 0 ? ChangeType.RESTOCK : ChangeType.ADJUSTMENT;
                String reason = change > 0 ? "Stock replenished" : "Stock adjusted";
                
                String logSQL = "INSERT INTO inventory_logs (product_id, change_amount, previous_quantity, new_quantity, change_type, reason) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement logStmt = conn.prepareStatement(logSQL)) {
                    logStmt.setInt(1, product.getProductId());
                    logStmt.setInt(2, change);
                    logStmt.setInt(3, oldStock);
                    logStmt.setInt(4, product.getStockQuantity());
                    logStmt.setString(5, changeType.getValue());
                    logStmt.setString(6, reason);
                    logStmt.executeUpdate();
                }
            }
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) conn.setAutoCommit(true);
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("product_id"),
                rs.getInt("category_id"),
                rs.getString("name"),
                rs.getDouble("price"),
                rs.getInt("stock_quantity"));
    }
}
