package com.amalitech.smartecommerce.controllers;

import com.amalitech.smartecommerce.models.Product;
import com.amalitech.smartecommerce.models.Review;
import com.amalitech.smartecommerce.models.User;
import com.amalitech.smartecommerce.services.OrderService;
import com.amalitech.smartecommerce.services.ProductService;
import com.amalitech.smartecommerce.services.ReviewService;
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
    private final ReviewService reviewService = new ReviewService();
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

    @SuppressWarnings("unchecked")
    @FXML
    private void handleViewOrders() {
        try {
            User user = SessionManager.getInstance().getCurrentUser();
            java.util.List<com.amalitech.smartecommerce.models.Order> orders = orderService.getOrdersByUserId(user.getUserId());
            
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("My Orders");
            dialog.setHeaderText("Order History");
            dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType("View Items", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Edit Order", ButtonBar.ButtonData.LEFT),
                ButtonType.CLOSE);
            
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
                com.amalitech.smartecommerce.models.Order selected = orderTable.getSelectionModel().getSelectedItem();
                if (btn.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    if (selected != null) showOrderItems(selected.getOrderId());
                } else if (btn.getButtonData() == ButtonBar.ButtonData.LEFT) {
                    if (selected != null) {
                        if ("pending".equalsIgnoreCase(selected.getStatus())) {
                            handleEditOrder(selected.getOrderId());
                        } else {
                            showWarning("Cannot Edit", "Only pending orders can be edited");
                        }
                    } else {
                        showWarning("No Selection", "Please select an order to edit");
                    }
                }
                return null;
            });
            
            dialog.showAndWait();
        } catch (Exception e) {
            showError("Error", "Could not load orders: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
    private void handleEditOrder(int orderId) {
        try {
            java.util.List<com.amalitech.smartecommerce.models.OrderItem> items = orderService.getOrderItems(orderId);
            Map<Product, Integer> editCart = new HashMap<>();
            
            for (com.amalitech.smartecommerce.models.OrderItem item : items) {
                Product product = productService.getProductById(item.getProductId());
                if (product != null) {
                    editCart.put(product, item.getQuantity());
                }
            }
            
            Dialog<Map<Product, Integer>> dialog = new Dialog<>();
            dialog.setTitle("Edit Order");
            dialog.setHeaderText("Edit Order #" + orderId + " (Pending)");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(10);
            content.setPadding(new javafx.geometry.Insets(10));
            
            TableView<Product> availableProducts = new TableView<>();
            TableColumn<Product, String> colName = new TableColumn<>("Product");
            TableColumn<Product, Double> colPrice = new TableColumn<>("Price");
            TableColumn<Product, Integer> colStock = new TableColumn<>("Stock");
            
            colName.setCellValueFactory(new PropertyValueFactory<>("name"));
            colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
            colStock.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
            
            availableProducts.getColumns().addAll(colName, colPrice, colStock);
            availableProducts.setItems(FXCollections.observableArrayList(productService.getAllProducts()));
            availableProducts.setPrefHeight(200);
            
            TableView<CartEntry> editCartTable = new TableView<>();
            TableColumn<CartEntry, String> colCartName = new TableColumn<>("Product");
            TableColumn<CartEntry, Integer> colCartQty = new TableColumn<>("Quantity");
            TableColumn<CartEntry, Double> colCartTotal = new TableColumn<>("Total");
            
            colCartName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProduct().getName()));
            colCartQty.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
            colCartTotal.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getProduct().getPrice() * cellData.getValue().getQuantity()).asObject());
            
            editCartTable.getColumns().addAll(colCartName, colCartQty, colCartTotal);
            ObservableList<CartEntry> editCartList = FXCollections.observableArrayList();
            for (Map.Entry<Product, Integer> entry : editCart.entrySet()) {
                editCartList.add(new CartEntry(entry.getKey(), entry.getValue()));
            }
            editCartTable.setItems(editCartList);
            editCartTable.setPrefHeight(150);
            
            javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(10);
            Button addBtn = new Button("Add to Order");
            Button removeBtn = new Button("Remove from Order");
            
            addBtn.setOnAction(e -> {
                Product selected = availableProducts.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    int currentQty = editCart.getOrDefault(selected, 0);
                    if (currentQty < selected.getStockQuantity()) {
                        editCart.put(selected, currentQty + 1);
                        editCartList.clear();
                        for (Map.Entry<Product, Integer> entry : editCart.entrySet()) {
                            editCartList.add(new CartEntry(entry.getKey(), entry.getValue()));
                        }
                    } else {
                        showWarning("Stock Limit", "Cannot add more than available stock");
                    }
                }
            });
            
            removeBtn.setOnAction(e -> {
                CartEntry selected = editCartTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    editCart.remove(selected.getProduct());
                    editCartList.remove(selected);
                }
            });
            
            buttons.getChildren().addAll(addBtn, removeBtn);
            content.getChildren().addAll(
                new Label("Available Products:"), availableProducts,
                buttons,
                new Label("Current Order Items:"), editCartTable
            );
            
            dialog.getDialogPane().setContent(content);
            dialog.setResultConverter(btn -> btn == ButtonType.OK ? editCart : null);
            
            dialog.showAndWait().ifPresent(updatedCart -> {
                if (updatedCart.isEmpty()) {
                    showWarning("Empty Order", "Order must contain at least one item");
                    return;
                }
                boolean success = orderService.updateOrder(orderId, updatedCart);
                if (success) {
                    showInfo("Success", "Order updated successfully!");
                    loadData();
                } else {
                    showError("Update Failed", "Could not update order. Please try again.");
                }
            });
        } catch (Exception e) {
            showError("Error", "Could not edit order: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewReviews() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a product to view reviews");
            return;
        }
        showProductReviews(selected);
    }

    @FXML
    private void handleAddReview() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("No Selection", "Please select a product to review");
            return;
        }
        showAddReviewDialog(selected);
    }

    @SuppressWarnings("unchecked")
    private void showProductReviews(Product product) {
        try {
            List<Review> reviews = reviewService.getProductReviews(product.getProductId());
            double avgRating = reviewService.getAverageRating(product.getProductId());
            
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Product Reviews");
            dialog.setHeaderText(product.getName() + " - " + String.format("%.1f ★ (%d reviews)", avgRating, reviews.size()));
            dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType("Mark Helpful", ButtonBar.ButtonData.LEFT),
                ButtonType.CLOSE);
            
            TableView<Review> reviewTable = new TableView<>();
            TableColumn<Review, String> colUser = new TableColumn<>("User");
            TableColumn<Review, Integer> colRating = new TableColumn<>("Rating");
            TableColumn<Review, String> colTitle = new TableColumn<>("Title");
            TableColumn<Review, String> colComment = new TableColumn<>("Comment");
            TableColumn<Review, Integer> colHelpful = new TableColumn<>("Helpful");
            
            colUser.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUserName()));
            colRating.setCellValueFactory(new PropertyValueFactory<>("rating"));
            colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
            colComment.setCellValueFactory(new PropertyValueFactory<>("comment"));
            colHelpful.setCellValueFactory(new PropertyValueFactory<>("helpfulVotes"));
            
            colUser.setPrefWidth(120);
            colRating.setPrefWidth(60);
            colTitle.setPrefWidth(150);
            colComment.setPrefWidth(250);
            colHelpful.setPrefWidth(70);
            
            reviewTable.getColumns().addAll(colUser, colRating, colTitle, colComment, colHelpful);
            reviewTable.setItems(FXCollections.observableArrayList(reviews));
            reviewTable.setPrefHeight(400);
            
            dialog.getDialogPane().setContent(reviewTable);
            
            dialog.setResultConverter(btn -> {
                if (btn.getButtonData() == ButtonBar.ButtonData.LEFT) {
                    Review selected = reviewTable.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        try {
                            reviewService.markHelpful(selected.getReviewId());
                            showInfo("Success", "Marked review as helpful");
                            showProductReviews(product);
                        } catch (SQLException e) {
                            showError("Error", "Could not mark review as helpful");
                        }
                    } else {
                        showWarning("No Selection", "Please select a review");
                    }
                }
                return null;
            });
            
            dialog.showAndWait();
        } catch (SQLException e) {
            showError("Error", "Could not load reviews: " + e.getMessage());
        }
    }

    private void showAddReviewDialog(Product product) {
        Dialog<Review> dialog = new Dialog<>();
        dialog.setTitle("Add Review");
        dialog.setHeaderText("Write a review for " + product.getName());
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));
        
        ComboBox<Integer> ratingBox = new ComboBox<>(FXCollections.observableArrayList(1, 2, 3, 4, 5));
        ratingBox.setPromptText("Select rating");
        TextField titleField = new TextField();
        titleField.setPromptText("Review title");
        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Write your review...");
        commentArea.setPrefRowCount(5);
        
        grid.add(new Label("Rating:"), 0, 0);
        grid.add(ratingBox, 1, 0);
        grid.add(new Label("Title:"), 0, 1);
        grid.add(titleField, 1, 1);
        grid.add(new Label("Comment:"), 0, 2);
        grid.add(commentArea, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                if (ratingBox.getValue() == null || titleField.getText().trim().isEmpty()) {
                    showWarning("Invalid Input", "Please provide rating and title");
                    return null;
                }
                User user = SessionManager.getInstance().getCurrentUser();
                return new Review(product.getProductId(), user.getUserId(), 
                    ratingBox.getValue(), titleField.getText().trim(), commentArea.getText().trim());
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(review -> {
            try {
                reviewService.addReview(review);
                showInfo("Success", "Review added successfully!");
            } catch (SQLException e) {
                showError("Error", "Could not add review: " + e.getMessage());
            }
        });
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
