package com.amalitech.smartecommerce.services;

import com.amalitech.smartecommerce.dao.OrderDAO;
import com.amalitech.smartecommerce.models.Order;
import com.amalitech.smartecommerce.models.OrderItem;
import com.amalitech.smartecommerce.models.Product;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class OrderService {
  private OrderDAO orderDAO;

  public OrderService() {
    this.orderDAO = new OrderDAO();
  }

  public boolean checkout(int userId, Map<Product, Integer> cart) {
    if (cart.isEmpty()) return false;
    try {
      return orderDAO.placeOrder(userId, cart);
    } catch (SQLException e) {
      System.err.println("Checkout failed: " + e.getMessage());
      return false;
    }
  }

  public List<Order> getOrdersByUserId(int userId) throws SQLException {
    return orderDAO.findByUserId(userId);
  }

  public List<Order> getAllOrders() throws SQLException {
    return orderDAO.findAll();
  }

  public void updateOrderStatus(int orderId, String status) throws SQLException {
    orderDAO.updateStatus(orderId, status);
  }

  public List<OrderItem> getOrderItems(int orderId) throws SQLException {
    return orderDAO.getOrderItems(orderId);
  }
}
