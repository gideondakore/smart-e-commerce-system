package com.amalitech.smartecommerce.controllers;

import com.amalitech.smartecommerce.models.Product;
import com.amalitech.smartecommerce.models.User;
import com.amalitech.smartecommerce.services.OrderService;
import com.amalitech.smartecommerce.services.ProductService;
import com.amalitech.smartecommerce.utils.SessionManager;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TextField searchField;
    @FXML private TableView<CartEntry> cartTable;
    @FXML private TableColumn<CartEntry, String> colCartName;
    @FXML private TableColumn<CartEntry, Integer> colCartQty;
    @FXML private TableColumn<CartEntry, Double> colCartPrice;
    @FXML private Label totalLabel;

    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();
    private ObservableList<Product> productList;
    private ObservableList<CartEntry> cartList;
    private Map<Product, Integer> cartMap;

    @FXML
    private void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        welcomeLabel.setText("Welcome, " + user.getFullName());

        colId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

        colCartName.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getProduct().getName()));
        colCartQty.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        colCartPrice.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getProduct().getPrice() * 
                cellData.getValue().getQuantity()).asObject());

        productList = FXCollections.observableArrayList();
        cartList = FXCollections.observableArrayList();
        cartMap = new HashMap<>();

        productTable.setItems(productList);
        cartTable.setItems(cartList);

        loadData();
    }

    private void loadData() {
        try {
            List<Product> products = productService.getAllProducts();
            productList.setAll(products);
        } catch (SQLException e) {
            showError("Error loading products", e.getMessage());
        }
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        try {
            List<Product> results = query.isEmpty() ? 
                productService.getAllProducts() : 
                productService.searchProductsByName(query);
            productList.setAll(results);
        } catch (SQLException e) {
            showError("Search Error", e.getMessage());
        }
    }

    @FXML
    private void handleAddToCart() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a product to add to cart");
            return;
        }

        if (selected.getStockQuantity() <= 0) {
            showWarning("Out of Stock", "This product is currently out of stock");
            return;
        }

        int currentQty = cartMap.getOrDefault(selected, 0);
        if (currentQty >= selected.getStockQuantity()) {
            showWarning("Stock Limit", "Cannot add more than available stock");
            return;
        }

        cartMap.put(selected, currentQty + 1);
        updateCartUI();
    }

    @FXML
    private void handleRemoveFromCart() {
        CartEntry selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            cartMap.remove(selected.getProduct());
            updateCartUI();
        }
    }

    private void updateCartUI() {
        cartList.clear();
        double total = 0;
        for (Map.Entry<Product, Integer> entry : cartMap.entrySet()) {
            cartList.add(new CartEntry(entry.getKey(), entry.getValue()));
            total += entry.getKey().getPrice() * entry.getValue();
        }
        totalLabel.setText(String.format("$%.2f", total));
    }

    @FXML
    private void handleCheckout() {
        if (cartMap.isEmpty()) {
            showWarning("Empty Cart", "Add items to cart before checking out");
            return;
        }

        User user = SessionManager.getInstance().getCurrentUser();
        boolean success = orderService.checkout(user.getUserId(), cartMap);

        if (success) {
            showInfo("Success", "Order placed successfully!");
            cartMap.clear();
            updateCartUI();
            loadData();
        } else {
            showError("Checkout Failed", "Could not place order. Please try again.");
        }
    }

    @FXML
    private void handleViewOrders() {
        showInfo("My Orders", "Order history would open here");
    }

    @FXML
    private void handleLogout() {
        SessionManager.getInstance().logout();
        navigateToLogin();
    }

    private void navigateToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Navigation Error", e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class CartEntry {
        private final Product product;
        private final int quantity;

        public CartEntry(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public Product getProduct() { return product; }
        public int getQuantity() { return quantity; }
    }
}
