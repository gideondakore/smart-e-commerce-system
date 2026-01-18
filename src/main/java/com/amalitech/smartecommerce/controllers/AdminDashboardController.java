package com.amalitech.smartecommerce.controllers;

import com.amalitech.smartecommerce.dao.CategoryDAO;
import com.amalitech.smartecommerce.dao.UserDAO;
import com.amalitech.smartecommerce.models.Category;
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

public class AdminDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TextField searchField;
    @FXML private Label statsProducts;
    @FXML private Label statsUsers;
    @FXML private Label statsCategories;

    private final ProductService productService = new ProductService();
    private final UserDAO userDAO = new UserDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
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
        loadStats();
    }

    private void loadData() {
        try {
            List<Product> products = productService.getAllProducts();
            productList.setAll(products);
        } catch (SQLException e) {
            showError("Error loading products", e.getMessage());
        }
    }

    private void loadStats() {
        try {
            statsProducts.setText(String.valueOf(productList.size()));
            statsUsers.setText(String.valueOf(userDAO.count()));
            statsCategories.setText(String.valueOf(categoryDAO.findAll().size()));
        } catch (SQLException e) {
            showError("Error loading statistics", e.getMessage());
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
    private void handleAddProduct() {
        showInfo("Add Product", "Product creation dialog would open here");
    }

    @FXML
    private void handleEditProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a product to edit");
            return;
        }
        showInfo("Edit Product", "Edit dialog for: " + selected.getName());
    }

    @FXML
    private void handleDeleteProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a product to delete");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setContentText("Delete product: " + selected.getName() + "?");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                productService.deleteProduct(selected.getProductId());
                loadData();
                loadStats();
            } catch (SQLException e) {
                showError("Delete Error", e.getMessage());
            }
        }
    }

    @FXML
    private void handleManageUsers() {
        showInfo("Manage Users", "User management panel would open here");
    }

    @FXML
    private void handleManageCategories() {
        showInfo("Manage Categories", "Category management panel would open here");
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
