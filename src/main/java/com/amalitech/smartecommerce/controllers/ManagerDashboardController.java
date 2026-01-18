package com.amalitech.smartecommerce.controllers;

import com.amalitech.smartecommerce.models.Product;
import com.amalitech.smartecommerce.models.User;
import com.amalitech.smartecommerce.services.ProductService;
import com.amalitech.smartecommerce.utils.SessionManager;
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
import java.util.List;

public class ManagerDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TextField searchField;
    @FXML private Label statsLowStock;
    @FXML private Label statsTotal;

    private final ProductService productService = new ProductService();
    private ObservableList<Product> productList;

    @FXML
    private void initialize() {
        User user = SessionManager.getInstance().getCurrentUser();
        welcomeLabel.setText("Welcome, " + user.getFullName());

        colId.setCellValueFactory(new PropertyValueFactory<>("productId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));

        productList = FXCollections.observableArrayList();
        productTable.setItems(productList);

        loadData();
        updateStats();
    }

    private void loadData() {
        try {
            List<Product> products = productService.getAllProducts();
            productList.setAll(products);
        } catch (SQLException e) {
            showError("Error loading products", e.getMessage());
        }
    }

    private void updateStats() {
        long lowStock = productList.stream().filter(p -> p.getStockQuantity() < 10).count();
        statsLowStock.setText(String.valueOf(lowStock));
        statsTotal.setText(String.valueOf(productList.size()));
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        try {
            List<Product> results = query.isEmpty() ? 
                productService.getAllProducts() : 
                productService.searchProductsByName(query);
            productList.setAll(results);
            updateStats();
        } catch (SQLException e) {
            showError("Search Error", e.getMessage());
        }
    }

    @FXML
    private void handleUpdateStock() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a product to update stock");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(String.valueOf(selected.getStockQuantity()));
        dialog.setTitle("Update Stock");
        dialog.setHeaderText("Update stock for: " + selected.getName());
        dialog.setContentText("New stock quantity:");

        dialog.showAndWait().ifPresent(input -> {
            try {
                int newStock = Integer.parseInt(input);
                if (newStock < 0) {
                    showWarning("Invalid Input", "Stock cannot be negative");
                    return;
                }
                selected.setStockQuantity(newStock);
                productService.updateProduct(selected);
                loadData();
                updateStats();
                showInfo("Success", "Stock updated successfully");
            } catch (NumberFormatException e) {
                showWarning("Invalid Input", "Please enter a valid number");
            } catch (SQLException e) {
                showError("Error", e.getMessage());
            }
        });
    }

    @FXML
    private void handleViewOrders() {
        showInfo("View Orders", "Order management panel - Coming soon!");
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
}
