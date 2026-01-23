package com.amalitech.smartecommerce.controllers;

import com.amalitech.smartecommerce.dao.UserDAO;
import com.amalitech.smartecommerce.models.User;
import com.amalitech.smartecommerce.utils.ValidationUtils;
import com.amalitech.smartecommerce.services.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class SignupController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;
    @FXML private Button signupButton;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void handleSignup() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!ValidationUtils.isNotEmpty(firstName) || !ValidationUtils.isNotEmpty(lastName) || 
            !ValidationUtils.isNotEmpty(email) || !ValidationUtils.isNotEmpty(password)) {
            showError("Please fill in all fields");
            return;
        }

        if (!ValidationUtils.isValidLength(firstName, 2, 100)) {
            showError("First name must be between 2 and 100 characters");
            return;
        }

        if (!ValidationUtils.isValidLength(lastName, 2, 100)) {
            showError("Last name must be between 2 and 100 characters");
            return;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            showError("Please enter a valid email address");
            return;
        }

        if (ValidationUtils.containsSqlInjection(email) || ValidationUtils.containsSqlInjection(firstName) || 
            ValidationUtils.containsSqlInjection(lastName)) {
            showError("Invalid characters detected");
            return;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            showError(ValidationUtils.getPasswordStrengthFeedback(password));
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        try {
            if (userDAO.findByEmail(email).isPresent()) {
                showError("Email already registered");
                return;
            }

            String passwordHash = AuthService.hashPassword(password);
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setPasswordHash(passwordHash);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setRole("customer");

            userDAO.create(newUser);

            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Success");
            success.setHeaderText(null);
            success.setContentText("Account created successfully! Please login.");
            success.showAndWait();

            navigateToLogin();

        } catch (SQLException e) {
            showError("Registration failed: " + e.getMessage());
        } catch (Exception e) {
            showError("An error occurred: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToLogin() {
        navigateToLogin();
    }

    private void navigateToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) signupButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Navigation error: " + e.getMessage());
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
