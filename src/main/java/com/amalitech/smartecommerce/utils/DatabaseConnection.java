package com.amalitech.smartecommerce.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DB_NAME = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "ecommerce_db";
    private static final String URL = "jdbc:postgresql://localhost:5432/" + DB_NAME;
    private static final String USER = System.getenv("POSTGRES_USER") != null ? System.getenv("POSTGRES_USER") : "spycon";
    private static final String PASSWORD = System.getenv("POSTGRES_PASSWORD") != null ? System.getenv("POSTGRES_PASSWORD") : "postgressPassword12345";
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Failed to connect to database. Please run DatabaseSetup first.");
            System.err.println("Run: mvn compile exec:java -Dexec.mainClass=\"com.amalitech.smartecommerce.utils.DatabaseSetup\"");
            e.printStackTrace();
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        } else {
            try {
                if (instance.getConnection().isClosed()) {
                    instance = new DatabaseConnection();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
