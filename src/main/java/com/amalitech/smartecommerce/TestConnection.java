package com.amalitech.smartecommerce;

import java.sql.Connection;
import java.sql.DriverManager;

public class TestConnection {
    public static void main(String[] args) {

        String url = "jdbc:postgresql://localhost:5432/" + System.getenv("DB_NAME");
        String user = System.getenv("POSTGRES_USER");
        String password = System.getenv("POSTGRES_PASSWORD");

        IO.println("DB_NAME: " + System.getenv("DB_NAME"));
        IO.println("BASE_URL: " + url);
        IO.println("USER: " + user);
        IO.println("PASSWORD: " + password);
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            IO.println("✅ Database connection successful! " + conn.hashCode());
        } catch (Exception e) {
            IO.println("❌ Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
