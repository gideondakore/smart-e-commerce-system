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
    @FXML private ProgressIndicator searchSpinner;

    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();
    private ObservableList<Product> productList;
    private ObservableList<CartEntry> cartList;
    private Map<Product, Integer> cartMap;

    @FXML
    private void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        welcomeLabel.setText("Welcome, " + user.getFullName());

        totalLabel.setText("$0.00");

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
        searchSpinner.setVisible(true);
        new Thread(() -> {
            try {
                Thread.sleep(query.isEmpty() ? 0 : 300);
                List<Product> results = query.isEmpty() ? 
                    productService.getAllProducts() : 
                    productService.searchProductsByName(query);
                javafx.application.Platform.runLater(() -> {
                    productList.setAll(results);
                    searchSpinner.setVisible(false);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    searchSpinner.setVisible(false);
                    showError("Search Error", e.getMessage());
                });
            }
        }).start();
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
        if (selected == null) {
            showWarning("No Selection", "Please select an item to remove from cart");
            return;
        }
        cartMap.remove(selected.getProduct());
        updateCartUI();
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
        try {
            User user = SessionManager.getInstance().getCurrentUser();
            java.util.List<com.amalitech.smartecommerce.models.Order> orders = orderService.getOrdersByUserId(user.getUserId());
            
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("My Orders");
            dialog.setHeaderText("Order History");
            dialog.getDialogPane().getButtonTypes().addAll(new ButtonType("View Items", ButtonBar.ButtonData.OK_DONE), ButtonType.CLOSE);
            
            TableView<com.amalitech.smartecommerce.models.Order> orderTable = new TableView<>();
            TableColumn<com.amalitech.smartecommerce.models.Order, Integer> colOrderId = new TableColumn<>("Order ID");
            TableColumn<com.amalitech.smartecommerce.models.Order, String> colDate = new TableColumn<>("Date");
            TableColumn<com.amalitech.smartecommerce.models.Order, String> colStatus = new TableColumn<>("Status");
            TableColumn<com.amalitech.smartecommerce.models.Order, Double> colTotal = new TableColumn<>("Total");
            
            colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
            colDate.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getOrderDate().toString()));
            colStatus.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus() != null ? cellData.getValue().getStatus() : "Completed"));
            colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
            
            colOrderId.setPrefWidth(80);
            colDate.setPrefWidth(180);
            colStatus.setPrefWidth(100);
            colTotal.setPrefWidth(100);
            
            orderTable.getColumns().addAll(colOrderId, colDate, colStatus, colTotal);
            orderTable.setItems(FXCollections.observableArrayList(orders));
            orderTable.setPrefHeight(400);
            
            dialog.getDialogPane().setContent(orderTable);
            
            dialog.setResultConverter(btn -> {
                if (btn.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    com.amalitech.smartecommerce.models.Order selected = orderTable.getSelectionModel().getSelectedItem();
                    if (selected != null) showOrderItems(selected.getOrderId());
                }
                return null;
            });
            
            dialog.showAndWait();
        } catch (Exception e) {
            showError("Error", "Could not load orders: " + e.getMessage());
        }
    }

    private void showOrderItems(int orderId) {
        try {
            java.util.List<com.amalitech.smartecommerce.models.OrderItem> items = orderService.getOrderItems(orderId);
            
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Order Items");
            dialog.setHeaderText("Order #" + orderId + " - Items");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            
            TableView<com.amalitech.smartecommerce.models.OrderItem> itemTable = new TableView<>();
            TableColumn<com.amalitech.smartecommerce.models.OrderItem, String> colName = new TableColumn<>("Product");
            TableColumn<com.amalitech.smartecommerce.models.OrderItem, Integer> colQty = new TableColumn<>("Quantity");
            TableColumn<com.amalitech.smartecommerce.models.OrderItem, Double> colPrice = new TableColumn<>("Price");
            TableColumn<com.amalitech.smartecommerce.models.OrderItem, Double> colSubtotal = new TableColumn<>("Subtotal");
            
            colName.setCellValueFactory(new PropertyValueFactory<>("productName"));
            colQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
            colPrice.setCellValueFactory(new PropertyValueFactory<>("priceAtPurchase"));
            colSubtotal.setCellValueFactory(cellData -> 
                new SimpleDoubleProperty(cellData.getValue().getQuantity() * cellData.getValue().getPriceAtPurchase()).asObject());
            
            colName.setPrefWidth(200);
            colQty.setPrefWidth(80);
            colPrice.setPrefWidth(100);
            colSubtotal.setPrefWidth(100);
            
            itemTable.getColumns().addAll(colName, colQty, colPrice, colSubtotal);
            itemTable.setItems(FXCollections.observableArrayList(items));
            itemTable.setPrefHeight(300);
            
            dialog.getDialogPane().setContent(itemTable);
            dialog.showAndWait();
        } catch (Exception e) {
            showError("Error", "Could not load order items: " + e.getMessage());
        }
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
