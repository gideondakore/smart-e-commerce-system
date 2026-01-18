package com.amalitech.smartecommerce.controllers;

import com.amalitech.smartecommerce.models.User;
import com.amalitech.smartecommerce.services.AuthService;
import com.amalitech.smartecommerce.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password");
            return;
        }

        try {
            Optional<User> userOpt = authService.login(email, password);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                SessionManager.getInstance().setCurrentUser(user);
                navigateToDashboard(user);
            } else {
                showError("Invalid email or password");
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    private void navigateToDashboard(User user) {
        try {
            String fxmlFile = switch (user.getRole().toLowerCase()) {
                case "admin" -> "/fxml/admin-dashboard.fxml";
                case "manager" -> "/fxml/manager-dashboard.fxml";
                default -> "/fxml/customer-dashboard.fxml";
            };

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Smart E-Commerce - " + user.getRole().toUpperCase());
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Failed to load dashboard: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
    }
}
