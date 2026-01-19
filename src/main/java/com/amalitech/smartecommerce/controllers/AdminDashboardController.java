package com.amalitech.smartecommerce.controllers;

import com.amalitech.smartecommerce.dao.CategoryDAO;
import com.amalitech.smartecommerce.dao.InventoryLogDAO;
import com.amalitech.smartecommerce.dao.UserDAO;
import com.amalitech.smartecommerce.models.Category;
import com.amalitech.smartecommerce.models.InventoryLog;
import com.amalitech.smartecommerce.models.Product;
import com.amalitech.smartecommerce.models.Review;
import com.amalitech.smartecommerce.models.User;
import com.amalitech.smartecommerce.services.OrderService;
import com.amalitech.smartecommerce.services.ProductService;
import com.amalitech.smartecommerce.services.ReviewService;
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
    @FXML private Label loadingLabel;
    @FXML private ProgressIndicator loadingSpinner;

    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();
    private final ReviewService reviewService = new ReviewService();
    private final UserDAO userDAO = new UserDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final InventoryLogDAO inventoryLogDAO = new InventoryLogDAO();
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
            showLoading("Loading products...");
            List<Product> products = productService.getAllProducts();
            productList.setAll(products);
            hideLoading();
        } catch (SQLException e) {
            hideLoading();
            showError("Error loading products", e.getMessage());
        }
    }

    private void showLoading(String message) {
        loadingLabel.setText(message);
        loadingLabel.setVisible(true);
        loadingSpinner.setVisible(true);
    }

    private void hideLoading() {
        loadingLabel.setVisible(false);
        loadingSpinner.setVisible(false);
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

    @SuppressWarnings("unchecked")
    @FXML
    private void handleManageUsers() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("User Management");
        dialog.setHeaderText("Manage System Users");
        
        ButtonType changeRoleButton = new ButtonType("Change Role", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeRoleButton, ButtonType.CLOSE);

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
        
        dialog.setResultConverter(btn -> {
            if (btn == changeRoleButton) {
                User selected = userTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    List<String> roles = java.util.Arrays.asList("admin", "manager", "customer");
                    ChoiceDialog<String> roleDialog = new ChoiceDialog<>(selected.getRole(), roles);
                    roleDialog.setTitle("Change User Role");
                    roleDialog.setHeaderText("User: " + selected.getEmail());
                    roleDialog.setContentText("Select new role:");
                    
                    roleDialog.showAndWait().ifPresent(role -> {
                        try {
                            userDAO.updateRole(selected.getUserId(), role);
                            showInfo("Success", "User role updated to " + role);
                            userTable.getItems().clear();
                            userTable.getItems().addAll(userDAO.findAll());
                        } catch (SQLException e) {
                            showError("Error", e.getMessage());
                        }
                    });
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }

    @SuppressWarnings("unchecked")
    @FXML
    private void handleManageCategories() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Category Management");
        dialog.setHeaderText("Manage Product Categories");
        
        ButtonType editButton = new ButtonType("Edit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(editButton, ButtonType.CLOSE);

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
        
        dialog.setResultConverter(btn -> {
            if (btn == editButton) {
                Category selected = categoryTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    Dialog<Category> editDialog = new Dialog<>();
                    editDialog.setTitle("Edit Category");
                    editDialog.setHeaderText("Edit: " + selected.getName());
                    ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
                    editDialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);
                    
                    javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
                    grid.setHgap(10);
                    grid.setVgap(10);
                    grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
                    
                    ComboBox<String> nameField = new ComboBox<>();
                    nameField.setItems(FXCollections.observableArrayList(
                        "Electronics", "Books", "Clothing", "Home & Kitchen", 
                        "Sports & Outdoors", "Beauty & Health", "Toys & Games", 
                        "Office Supplies", "Others"
                    ));
                    nameField.setValue(selected.getName());
                    nameField.setEditable(false);
                    TextField descField = new TextField(selected.getDescription());
                    
                    grid.add(new Label("Name:"), 0, 0);
                    grid.add(nameField, 1, 0);
                    grid.add(new Label("Description:"), 0, 1);
                    grid.add(descField, 1, 1);
                    
                    editDialog.getDialogPane().setContent(grid);
                    
                    editDialog.setResultConverter(b -> {
                        if (b == saveBtn) {
                            selected.setName(nameField.getValue());
                            selected.setDescription(descField.getText().trim());
                            return selected;
                        }
                        return null;
                    });
                    
                    editDialog.showAndWait().ifPresent(cat -> {
                        try {
                            categoryDAO.update(cat);
                            showInfo("Success", "Category updated");
                            categoryTable.getItems().clear();
                            categoryTable.getItems().addAll(categoryDAO.findAll());
                        } catch (SQLException e) {
                            showError("Error", e.getMessage());
                        }
                    });
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }

    @SuppressWarnings("unchecked")
    @FXML
    private void handleManageOrders() {
        try {
            List<com.amalitech.smartecommerce.models.Order> orders = orderService.getAllOrders();
            
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Order Management");
            dialog.setHeaderText("All Orders");
            
            ButtonType updateButton = new ButtonType("Update Status", ButtonBar.ButtonData.OK_DONE);
            ButtonType viewItemsButton = new ButtonType("View Items", ButtonBar.ButtonData.LEFT);
            dialog.getDialogPane().getButtonTypes().addAll(updateButton, viewItemsButton, ButtonType.CLOSE);
            
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
                com.amalitech.smartecommerce.models.Order selected = orderTable.getSelectionModel().getSelectedItem();
                if (dialogButton == updateButton && selected != null) {
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
                } else if (dialogButton == viewItemsButton && selected != null) {
                    showOrderItems(selected.getOrderId());
                }
                return null;
            });
            
            dialog.showAndWait();
        } catch (SQLException e) {
            showError("Error", "Could not load orders: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void showOrderItems(int orderId) {
        try {
            List<com.amalitech.smartecommerce.models.OrderItem> items = orderService.getOrderItems(orderId);
            
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
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getQuantity() * cellData.getValue().getPriceAtPurchase()).asObject());
            
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
    private void handlePerformanceDemo() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Performance Demonstration");
        dialog.setHeaderText("Cache & Index Performance Comparison");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(15);
        content.setPadding(new javafx.geometry.Insets(20));
        
        Label title1 = new Label("1. CACHING DEMONSTRATION");
        title1.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        javafx.scene.layout.HBox cacheTest = new javafx.scene.layout.HBox(10);
        Button testCacheBtn = new Button("Test Cache Performance");
        Label cacheResult = new Label("");
        cacheResult.setStyle("-fx-font-size: 12px;");
        cacheResult.setWrapText(true);
        cacheResult.setMaxWidth(600);
        cacheTest.getChildren().addAll(testCacheBtn, cacheResult);
        
        testCacheBtn.setOnAction(e -> {
            cacheResult.setText("Testing...");
            new Thread(() -> {
                try {
                    productService.clearCache();
                    long start1 = System.nanoTime();
                    productService.getAllProducts();
                    long dbTime = System.nanoTime() - start1;
                    
                    long start2 = System.nanoTime();
                    productService.getAllProducts();
                    long cacheTime = System.nanoTime() - start2;
                    
                    double improvement = ((double)(dbTime - cacheTime) / dbTime) * 100;
                    javafx.application.Platform.runLater(() -> 
                        cacheResult.setText(String.format(
                            "DB: %.2fms | Cache: %.2fms | %.1f%% faster",
                            dbTime/1_000_000.0, cacheTime/1_000_000.0, improvement
                        ))
                    );
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> cacheResult.setText("Error: " + ex.getMessage()));
                }
            }).start();
        });
        
        Label title2 = new Label("2. INDEXING DEMONSTRATION");
        title2.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 20 0 0 0;");
        
        javafx.scene.layout.VBox indexTest = new javafx.scene.layout.VBox(10);
        javafx.scene.layout.HBox indexInput = new javafx.scene.layout.HBox(10);
        TextField searchTerm = new TextField("Laptop");
        searchTerm.setPrefWidth(150);
        Button testIndexBtn = new Button("Test Index Performance");
        indexInput.getChildren().addAll(new Label("Search:"), searchTerm, testIndexBtn);
        Label indexResult = new Label("");
        indexResult.setStyle("-fx-font-size: 12px;");
        indexResult.setWrapText(true);
        indexResult.setMaxWidth(600);
        indexTest.getChildren().addAll(indexInput, indexResult);
        
        testIndexBtn.setOnAction(e -> {
            indexResult.setText("Testing...");
            String query = searchTerm.getText();
            new Thread(() -> {
                try {
                    productService.clearCache();
                    long start = System.nanoTime();
                    productService.searchProductsByName(query);
                    long indexTime = System.nanoTime() - start;
                    
                    javafx.application.Platform.runLater(() -> 
                        indexResult.setText(String.format(
                            "Search time: %.2fms\nUsing SQL index on LOWER(name) for fast case-insensitive search",
                            indexTime/1_000_000.0
                        ))
                    );
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> indexResult.setText("Error: " + ex.getMessage()));
                }
            }).start();
        });
        
        Label title3 = new Label("3. SORTING ALGORITHMS");
        title3.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 20 0 0 0;");
        
        javafx.scene.layout.HBox sortTest = new javafx.scene.layout.HBox(10);
        Button testSortBtn = new Button("Compare Sorting Algorithms");
        Label sortResult = new Label("");
        sortResult.setStyle("-fx-font-size: 12px;");
        sortResult.setWrapText(true);
        sortResult.setMaxWidth(600);
        sortTest.getChildren().addAll(testSortBtn, sortResult);
        
        testSortBtn.setOnAction(e -> {
            sortResult.setText("Testing...");
            new Thread(() -> {
                try {
                    List<Product> products = productService.getAllProducts();
                    
                    long start1 = System.nanoTime();
                    productService.quickSortByPrice(new java.util.ArrayList<>(products), true);
                    long quickTime = System.nanoTime() - start1;
                    
                    long start2 = System.nanoTime();
                    productService.sortProductsByPrice(new java.util.ArrayList<>(products), true);
                    long timTime = System.nanoTime() - start2;
                    
                    javafx.application.Platform.runLater(() -> 
                        sortResult.setText(String.format(
                            "QuickSort: %.2fms | TimSort: %.2fms",
                            quickTime/1_000_000.0, timTime/1_000_000.0
                        ))
                    );
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> sortResult.setText("Error: " + ex.getMessage()));
                }
            }).start();
        });
        
        Label explanation = new Label(
            "\n📊 Performance Insights:\n" +
            "• Caching: In-memory HashMap provides O(1) lookups vs O(n) database queries\n" +
            "• Indexing: SQL index on LOWER(name) enables fast case-insensitive search\n" +
            "• Sorting: QuickSort O(n log n) vs TimSort (optimized for real-world data)\n" +
            "• Hash-based cache mirrors database index logic for instant retrieval"
        );
        explanation.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280; -fx-padding: 10; -fx-background-color: #f3f4f6; -fx-background-radius: 5;");
        explanation.setWrapText(true);
        explanation.setMaxWidth(600);
        
        content.getChildren().addAll(title1, cacheTest, title2, indexTest, title3, sortTest, explanation);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(700, 500);
        
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefSize(750, 550);
        dialog.showAndWait();
    }

    @SuppressWarnings("unchecked")
    @FXML
    private void handleManageReviews() {
        try {
            List<Review> reviews = reviewService.getProductReviews(0); // Get all reviews
            if (reviews.isEmpty()) {
                // Fallback: get all reviews using DAO
                reviews = new com.amalitech.smartecommerce.dao.ReviewDAO().findAll();
            }
            
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Review Management");
            dialog.setHeaderText("All Product Reviews");
            
            ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(deleteButton, ButtonType.CLOSE);
            
            TableView<Review> reviewTable = new TableView<>();
            TableColumn<Review, Integer> colReviewId = new TableColumn<>("ID");
            TableColumn<Review, String> colProduct = new TableColumn<>("Product");
            TableColumn<Review, String> colUser = new TableColumn<>("User");
            TableColumn<Review, Integer> colRating = new TableColumn<>("Rating");
            TableColumn<Review, String> colTitle = new TableColumn<>("Title");
            TableColumn<Review, String> colComment = new TableColumn<>("Comment");
            TableColumn<Review, Integer> colHelpful = new TableColumn<>("Helpful");
            
            colReviewId.setCellValueFactory(new PropertyValueFactory<>("reviewId"));
            colProduct.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getProductName()));
            colUser.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUserName()));
            colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));
            colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
            colComment.setCellValueFactory(new PropertyValueFactory<>("comment"));
            colHelpful.setCellValueFactory(new PropertyValueFactory<>("helpfulVotes"));
            
            colReviewId.setPrefWidth(50);
            colProduct.setPrefWidth(120);
            colUser.setPrefWidth(120);
            colRating.setPrefWidth(60);
            colTitle.setPrefWidth(150);
            colComment.setPrefWidth(200);
            colHelpful.setPrefWidth(70);
            
            reviewTable.getColumns().addAll(colReviewId, colProduct, colUser, colRating, colTitle, colComment, colHelpful);
            reviewTable.setItems(FXCollections.observableArrayList(reviews));
            reviewTable.setPrefHeight(400);
            
            dialog.getDialogPane().setContent(reviewTable);
            dialog.getDialogPane().setPrefWidth(850);
            
            dialog.setResultConverter(btn -> {
                if (btn == deleteButton) {
                    Review selected = reviewTable.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setTitle("Confirm Delete");
                        confirm.setContentText("Delete this review?");
                        if (confirm.showAndWait().get() == ButtonType.OK) {
                            try {
                                new com.amalitech.smartecommerce.dao.ReviewDAO().delete(selected.getReviewId());
                                showInfo("Success", "Review deleted");
                                reviewTable.getItems().remove(selected);
                            } catch (SQLException e) {
                                showError("Error", e.getMessage());
                            }
                        }
                    } else {
                        showWarning("No Selection", "Please select a review to delete");
                    }
                }
                return null;
            });
            
            dialog.showAndWait();
        } catch (SQLException e) {
            showError("Error", "Could not load reviews: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @FXML
    private void handleViewInventoryLogs() {
        try {
            List<InventoryLog> logs = inventoryLogDAO.findRecent(100);
            
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Inventory Logs");
            dialog.setHeaderText("Recent Inventory Changes (Last 100)");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            
            TableView<InventoryLog> logTable = new TableView<>();
            TableColumn<InventoryLog, Integer> colLogId = new TableColumn<>("Log ID");
            TableColumn<InventoryLog, String> colProduct = new TableColumn<>("Product");
            TableColumn<InventoryLog, Integer> colChange = new TableColumn<>("Change");
            TableColumn<InventoryLog, Integer> colPrevQty = new TableColumn<>("Previous");
            TableColumn<InventoryLog, Integer> colNewQty = new TableColumn<>("New");
            TableColumn<InventoryLog, String> colType = new TableColumn<>("Type");
            TableColumn<InventoryLog, String> colDate = new TableColumn<>("Date");
            TableColumn<InventoryLog, String> colPerformedBy = new TableColumn<>("Performed By");
            
            colLogId.setCellValueFactory(new PropertyValueFactory<>("logId"));
            colProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
            colChange.setCellValueFactory(new PropertyValueFactory<>("changeAmount"));
            colPrevQty.setCellValueFactory(new PropertyValueFactory<>("previousQuantity"));
            colNewQty.setCellValueFactory(new PropertyValueFactory<>("newQuantity"));
            colType.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getChangeType().getValue()));
            colDate.setCellValueFactory(cellData -> 
                new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getChangeDate() != null ? 
                    cellData.getValue().getChangeDate().toString() : ""));
            colPerformedBy.setCellValueFactory(new PropertyValueFactory<>("performedByName"));
            
            colLogId.setPrefWidth(60);
            colProduct.setPrefWidth(150);
            colChange.setPrefWidth(70);
            colPrevQty.setPrefWidth(70);
            colNewQty.setPrefWidth(70);
            colType.setPrefWidth(90);
            colDate.setPrefWidth(150);
            colPerformedBy.setPrefWidth(120);
            
            logTable.getColumns().addAll(colLogId, colProduct, colChange, colPrevQty, colNewQty, colType, colDate, colPerformedBy);
            logTable.setItems(FXCollections.observableArrayList(logs));
            logTable.setPrefHeight(500);
            
            dialog.getDialogPane().setContent(logTable);
            dialog.getDialogPane().setPrefWidth(900);
            dialog.showAndWait();
        } catch (SQLException e) {
            showError("Error", "Could not load inventory logs: " + e.getMessage());
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
