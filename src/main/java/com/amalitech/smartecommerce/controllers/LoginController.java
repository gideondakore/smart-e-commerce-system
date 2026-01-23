package com.amalitech.smartecommerce.controllers;

import com.amalitech.smartecommerce.models.User;
import com.amalitech.smartecommerce.services.AuthService;
import com.amalitech.smartecommerce.utils.SessionManager;
import com.amalitech.smartecommerce.utils.ValidationUtils;
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

        if (!ValidationUtils.isNotEmpty(email) || !ValidationUtils.isNotEmpty(password)) {
            showError("Please enter both email and password");
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            showError("Please enter a valid email address");
            return;
        }

        if (ValidationUtils.containsSqlInjection(email)) {
            showError("Invalid characters in email");
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

            System.out.println("Navigating to dashboard for user: " + user);
            String fxmlFile = switch (user.getRole().toLowerCase()) {
                case "admin" -> "/fxml/admin-dashboard.fxml";
                case "manager" -> "/fxml/manager-dashboard.fxml";
                default -> "/fxml/customer-dashboard.fxml";
            };

            System.out.println("Loading dashboard: " + fxmlFile + " for role: " + user.getRole());
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Smart E-Commerce - " + user.getRole().toUpperCase());
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to load dashboard: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Unexpected error: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    @FXML
    private void handleSignup() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/signup.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Navigation error: " + e.getMessage());
        }
    }

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
    }
}
