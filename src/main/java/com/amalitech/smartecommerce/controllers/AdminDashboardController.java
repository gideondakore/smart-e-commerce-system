package com.amalitech.smartecommerce.controllers;

import com.amalitech.smartecommerce.dao.CategoryDAO;
import com.amalitech.smartecommerce.dao.UserDAO;
import com.amalitech.smartecommerce.models.Category;
import com.amalitech.smartecommerce.models.Product;
import com.amalitech.smartecommerce.models.User;
import com.amalitech.smartecommerce.services.OrderService;
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
    private final OrderService orderService = new OrderService();
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
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Add New Product");
        dialog.setHeaderText("Enter Product Details");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Product Name");
        TextField priceField = new TextField();
        priceField.setPromptText("Price");
        TextField stockField = new TextField();
        stockField.setPromptText("Stock Quantity");
        ComboBox<Category> categoryBox = new ComboBox<>();

        try {
            List<Category> categories = categoryDAO.findAll();
            categoryBox.setItems(FXCollections.observableArrayList(categories));
            categoryBox.setConverter(new javafx.util.StringConverter<Category>() {
                @Override
                public String toString(Category cat) {
                    return cat == null ? "" : cat.getName();
                }
                @Override
                public Category fromString(String string) {
                    return null;
                }
            });
            if (!categories.isEmpty()) categoryBox.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            showError("Error", "Could not load categories");
            return;
        }

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Price:"), 0, 1);
        grid.add(priceField, 1, 1);
        grid.add(new Label("Stock:"), 0, 2);
        grid.add(stockField, 1, 2);
        grid.add(new Label("Category:"), 0, 3);
        grid.add(categoryBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    String name = nameField.getText().trim();
                    double price = Double.parseDouble(priceField.getText());
                    int stock = Integer.parseInt(stockField.getText());
                    Category cat = categoryBox.getValue();
                    if (name.isEmpty() || cat == null) return null;
                    return new Product(cat.getCategoryId(), name, price, stock);
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(product -> {
            try {
                productService.createProduct(product);
                loadData();
                loadStats();
                showInfo("Success", "Product added successfully");
            } catch (SQLException e) {
                showError("Error", e.getMessage());
            }
        });
    }

    @FXML
    private void handleEditProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a product to edit");
            return;
        }

        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Edit Product");
        dialog.setHeaderText("Edit Product: " + selected.getName());

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField(selected.getName());
        TextField priceField = new TextField(String.valueOf(selected.getPrice()));
        TextField stockField = new TextField(String.valueOf(selected.getStockQuantity()));
        ComboBox<Category> categoryBox = new ComboBox<>();

        try {
            List<Category> categories = categoryDAO.findAll();
            categoryBox.setItems(FXCollections.observableArrayList(categories));
            categoryBox.setConverter(new javafx.util.StringConverter<Category>() {
                @Override
                public String toString(Category cat) {
                    return cat == null ? "" : cat.getName();
                }
                @Override
                public Category fromString(String string) {
                    return null;
                }
            });
            for (Category cat : categories) {
                if (cat.getCategoryId() == selected.getCategoryId()) {
                    categoryBox.getSelectionModel().select(cat);
                    break;
                }
            }
        } catch (SQLException e) {
            showError("Error", "Could not load categories");
            return;
        }

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Price:"), 0, 1);
        grid.add(priceField, 1, 1);
        grid.add(new Label("Stock:"), 0, 2);
        grid.add(stockField, 1, 2);
        grid.add(new Label("Category:"), 0, 3);
        grid.add(categoryBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String name = nameField.getText().trim();
                    double price = Double.parseDouble(priceField.getText());
                    int stock = Integer.parseInt(stockField.getText());
                    Category cat = categoryBox.getValue();
                    if (name.isEmpty() || cat == null) return null;
                    return new Product(selected.getProductId(), cat.getCategoryId(), name, price, stock);
                } catch (Exception e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(product -> {
            try {
                productService.updateProduct(product);
                loadData();
                showInfo("Success", "Product updated successfully");
            } catch (SQLException e) {
                showError("Error", e.getMessage());
            }
        });
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
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("User Management");
        dialog.setHeaderText("Manage System Users");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TableView<User> userTable = new TableView<>();
        TableColumn<User, Integer> colUserId = new TableColumn<>("ID");
        TableColumn<User, String> colEmail = new TableColumn<>("Email");
        TableColumn<User, String> colName = new TableColumn<>("Name");
        TableColumn<User, String> colRole = new TableColumn<>("Role");

        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colName.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFullName()));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        colUserId.setPrefWidth(50);
        colEmail.setPrefWidth(200);
        colName.setPrefWidth(150);
        colRole.setPrefWidth(100);

        userTable.getColumns().addAll(colUserId, colEmail, colName, colRole);

        try {
            List<User> users = userDAO.findAll();
            userTable.setItems(FXCollections.observableArrayList(users));
        } catch (SQLException e) {
            showError("Error", "Could not load users");
            return;
        }

        userTable.setPrefHeight(400);
        dialog.getDialogPane().setContent(userTable);
        dialog.showAndWait();
    }

    @FXML
    private void handleManageCategories() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Category Management");
        dialog.setHeaderText("Manage Product Categories");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        TableView<Category> categoryTable = new TableView<>();
        TableColumn<Category, Integer> colCatId = new TableColumn<>("ID");
        TableColumn<Category, String> colCatName = new TableColumn<>("Name");
        TableColumn<Category, String> colCatDesc = new TableColumn<>("Description");

        colCatId.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        colCatName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCatDesc.setCellValueFactory(new PropertyValueFactory<>("description"));

        colCatId.setPrefWidth(50);
        colCatName.setPrefWidth(150);
        colCatDesc.setPrefWidth(300);

        categoryTable.getColumns().addAll(colCatId, colCatName, colCatDesc);

        try {
            List<Category> categories = categoryDAO.findAll();
            categoryTable.setItems(FXCollections.observableArrayList(categories));
        } catch (SQLException e) {
            showError("Error", "Could not load categories");
            return;
        }

        categoryTable.setPrefHeight(400);
        dialog.getDialogPane().setContent(categoryTable);
        dialog.showAndWait();
    }

    @FXML
    private void handleManageOrders() {
        try {
            List<com.amalitech.smartecommerce.models.Order> orders = orderService.getAllOrders();
            
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Order Management");
            dialog.setHeaderText("All Orders");
            
            ButtonType updateButton = new ButtonType("Update Status", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(updateButton, ButtonType.CLOSE);
            
            TableView<com.amalitech.smartecommerce.models.Order> orderTable = new TableView<>();
            TableColumn<com.amalitech.smartecommerce.models.Order, Integer> colOrderId = new TableColumn<>("Order ID");
            TableColumn<com.amalitech.smartecommerce.models.Order, String> colCustomer = new TableColumn<>("Customer");
            TableColumn<com.amalitech.smartecommerce.models.Order, String> colDate = new TableColumn<>("Date");
            TableColumn<com.amalitech.smartecommerce.models.Order, String> colStatus = new TableColumn<>("Status");
            TableColumn<com.amalitech.smartecommerce.models.Order, Double> colTotal = new TableColumn<>("Total");
            
            colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
            colCustomer.setCellValueFactory(new PropertyValueFactory<>("customerEmail"));
            colDate.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getOrderDate().toString()));
            colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
            colTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
            
            colOrderId.setPrefWidth(80);
            colCustomer.setPrefWidth(180);
            colDate.setPrefWidth(180);
            colStatus.setPrefWidth(100);
            colTotal.setPrefWidth(100);
            
            orderTable.getColumns().addAll(colOrderId, colCustomer, colDate, colStatus, colTotal);
            orderTable.setItems(FXCollections.observableArrayList(orders));
            orderTable.setPrefHeight(400);
            
            dialog.getDialogPane().setContent(orderTable);
            
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == updateButton) {
                    com.amalitech.smartecommerce.models.Order selected = orderTable.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        List<String> choices = java.util.Arrays.asList("pending", "processing", "shipped", "delivered", "cancelled");
                        ChoiceDialog<String> statusDialog = new ChoiceDialog<>(selected.getStatus(), choices);
                        statusDialog.setTitle("Update Order Status");
                        statusDialog.setHeaderText("Order #" + selected.getOrderId());
                        statusDialog.setContentText("Select new status:");
                        
                        statusDialog.showAndWait().ifPresent(status -> {
                            try {
                                orderService.updateOrderStatus(selected.getOrderId(), status);
                                showInfo("Success", "Order status updated");
                            } catch (SQLException e) {
                                showError("Error", e.getMessage());
                            }
                        });
                    }
                }
                return null;
            });
            
            dialog.showAndWait();
        } catch (SQLException e) {
            showError("Error", "Could not load orders: " + e.getMessage());
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
}
