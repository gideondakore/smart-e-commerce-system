package com.amalitech.smartecommerce.dao;

import com.amalitech.smartecommerce.models.Order;
import com.amalitech.smartecommerce.models.OrderItem;
import com.amalitech.smartecommerce.models.Product;
import com.amalitech.smartecommerce.utils.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderDAO {

  /**
   * Performs a transactional order placement. 1. Inserts Order 2. Inserts OrderItems 3. Updates
   * Product Stock Rolls back if any step fails (e.g., insufficient stock).
   */
  public boolean placeOrder(int userId, Map<Product, Integer> cartItems) throws SQLException {
    Connection conn = null;
    PreparedStatement orderStmt = null;
    PreparedStatement itemStmt = null;
    PreparedStatement stockStmt = null;
    ResultSet generatedKeys = null;

    try {
      conn = DatabaseConnection.getInstance().getConnection();
      // 1. Start Transaction
      conn.setAutoCommit(false);

      // Calculate Total
      double totalAmount = 0;
      for (Map.Entry<Product, Integer> entry : cartItems.entrySet()) {
        totalAmount += entry.getKey().getPrice() * entry.getValue();
      }

      // 2. Insert Order
      String insertOrderSQL =
          "INSERT INTO orders (user_id, order_date, total_amount) VALUES (?, NOW(), ?)";
      orderStmt = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS);
      orderStmt.setInt(1, userId);
      orderStmt.setDouble(2, totalAmount);
      int affectedRows = orderStmt.executeUpdate();

      if (affectedRows == 0) {
        throw new SQLException("Creating order failed, no rows affected.");
      }

      generatedKeys = orderStmt.getGeneratedKeys();
      int orderId;
      if (generatedKeys.next()) {
        orderId = generatedKeys.getInt(1);
      } else {
        throw new SQLException("Creating order failed, no ID obtained.");
      }

      // 3. Insert Items & Update Stock
      String insertItemSQL =
          "INSERT INTO order_items (order_id, product_id, quantity, price_at_purchase) VALUES (?,"
              + " ?, ?, ?)";
      // This query ensures we don't sell more than we have
      String updateStockSQL =
          "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ? AND"
              + " stock_quantity >= ?";

      itemStmt = conn.prepareStatement(insertItemSQL);
      stockStmt = conn.prepareStatement(updateStockSQL);

      for (Map.Entry<Product, Integer> entry : cartItems.entrySet()) {
        Product product = entry.getKey();
        int quantity = entry.getValue();

        // Add to Batch: Insert Item
        itemStmt.setInt(1, orderId);
        itemStmt.setInt(2, product.getProductId());
        itemStmt.setInt(3, quantity);
        itemStmt.setDouble(4, product.getPrice());
        itemStmt.addBatch();

        // Update Stock (Immediate execution to check constraints)
        stockStmt.setInt(1, quantity);
        stockStmt.setInt(2, product.getProductId());
        stockStmt.setInt(3, quantity); // Check if stock >= quantity
        int stockRows = stockStmt.executeUpdate();

        if (stockRows == 0) {
          throw new SQLException("Insufficient stock for product: " + product.getName());
        }
      }

      itemStmt.executeBatch();

      // 4. Commit Transaction
      conn.commit();
      System.out.println("Transaction Committed Successfully. Order ID: " + orderId);
      return true;

    } catch (SQLException e) {
      if (conn != null) {
        try {
          System.err.println("Transaction failed. Rolling back.");
          conn.rollback();
        } catch (SQLException ex) {
          ex.printStackTrace();
        }
      }
      throw e; // Re-throw to notify caller
    } finally {
      // 5. Reset AutoCommit and Close Resources
      if (conn != null) conn.setAutoCommit(true);
      if (generatedKeys != null) generatedKeys.close();
      if (orderStmt != null) orderStmt.close();
      if (itemStmt != null) itemStmt.close();
      if (stockStmt != null) stockStmt.close();
    }
  }

  public List<Order> findByUserId(int userId) throws SQLException {
    List<Order> orders = new ArrayList<>();
    String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC";
    
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, userId);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          Order order = new Order();
          order.setOrderId(rs.getInt("order_id"));
          order.setUserId(rs.getInt("user_id"));
          order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
          order.setTotalAmount(rs.getDouble("total_amount"));
          order.setStatus(rs.getString("status"));
          orders.add(order);
        }
      }
    }
    return orders;
  }

  public List<Order> findAll() throws SQLException {
    List<Order> orders = new ArrayList<>();
    String sql = "SELECT o.*, u.email FROM orders o JOIN users u ON o.user_id = u.user_id ORDER BY o.order_date DESC";
    
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setUserId(rs.getInt("user_id"));
        order.setOrderDate(rs.getTimestamp("order_date").toLocalDateTime());
        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setStatus(rs.getString("status"));
        order.setCustomerEmail(rs.getString("email"));
        orders.add(order);
      }
    }
    return orders;
  }

  public void updateStatus(int orderId, String status) throws SQLException {
    String sql = "UPDATE orders SET status = ? WHERE order_id = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, status);
      stmt.setInt(2, orderId);
      stmt.executeUpdate();
    }
  }

  public List<OrderItem> getOrderItems(int orderId) throws SQLException {
    List<OrderItem> items = new ArrayList<>();
    String sql = "SELECT oi.*, p.name as product_name FROM order_items oi " +
                 "JOIN products p ON oi.product_id = p.product_id WHERE oi.order_id = ?";
    
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, orderId);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          OrderItem item = new OrderItem(
            rs.getInt("product_id"),
            rs.getInt("quantity"),
            rs.getDouble("price_at_purchase")
          );
          item.setId(rs.getInt("id"));
          item.setOrderId(orderId);
          item.setProductName(rs.getString("product_name"));
          items.add(item);
        }
      }
    }
    return items;
  }
}
