package com.amalitech.smartecommerce.test;

import com.amalitech.smartecommerce.models.User;
import com.amalitech.smartecommerce.services.AuthService;

import java.sql.SQLException;
import java.util.Optional;

public class LoginFlowTest {
    public static void main(String[] args) {
        System.out.println("=== Testing Login Flow ===\n");
        
        AuthService authService = new AuthService();
        
        String[][] testCases = {
            {"admin@shop.com", "admin123", "admin"},
            {"manager@shop.com", "manager123", "manager"},
            {"customer@shop.com", "customer123", "customer"}
        };
        
        for (String[] testCase : testCases) {
            String email = testCase[0];
            String password = testCase[1];
            String expectedRole = testCase[2];
            
            System.out.println("Testing: " + email);
            System.out.println("  Password: " + password);
            
            try {
                Optional<User> userOpt = authService.login(email, password);
                
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    System.out.println("  ✓ Login successful");
                    System.out.println("  User ID: " + user.getUserId());
                    System.out.println("  Email: " + user.getEmail());
                    System.out.println("  Role: " + user.getRole());
                    System.out.println("  Full Name: " + user.getFullName());
                    
                    if (user.getRole().equalsIgnoreCase(expectedRole)) {
                        System.out.println("  ✓ Role matches expected: " + expectedRole);
                    } else {
                        System.out.println("  ✗ Role mismatch! Expected: " + expectedRole + ", Got: " + user.getRole());
                    }
                    
                    String fxmlFile = switch (user.getRole().toLowerCase()) {
                        case "admin" -> "/fxml/admin-dashboard.fxml";
                        case "manager" -> "/fxml/manager-dashboard.fxml";
                        default -> "/fxml/customer-dashboard.fxml";
                    };
                    System.out.println("  Dashboard: " + fxmlFile);
                    
                } else {
                    System.out.println("  ✗ Login failed - Invalid credentials");
                }
                
            } catch (SQLException e) {
                System.out.println("  ✗ Database error: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println();
        }
        
        System.out.println("Testing invalid credentials:");
        try {
            Optional<User> userOpt = authService.login("invalid@test.com", "wrongpassword");
            if (userOpt.isEmpty()) {
                System.out.println("  ✓ Correctly rejected invalid credentials\n");
            } else {
                System.out.println("  ✗ Should have rejected invalid credentials\n");
            }
        } catch (SQLException e) {
            System.out.println("  ✗ Database error: " + e.getMessage());
        }
        
        System.out.println("=== All Tests Complete ===");
    }
}
