package com.amalitech.smartecommerce.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Database setup utility for initializing the PostgreSQL database.
 * Automatically creates database if it doesn't exist and sets up all tables.
 */
public class DatabaseSetup {

    private static final String BASE_URL = "jdbc:postgresql://localhost:5432/";
    private static final String DB_NAME = System.getenv("DB_NAME") != null ? System.getenv("DB_NAME") : "ecommerce_db";
    private static final String USER = System.getenv("POSTGRES_USER") != null ? System.getenv("POSTGRES_USER") : "spycon";
    private static final String PASSWORD = System.getenv("POSTGRES_PASSWORD") != null ? System.getenv("POSTGRES_PASSWORD") : "postgressPassword12345";

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           SMART E-COMMERCE DATABASE SETUP                    ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

        try {
            // 1. Create database if it doesn't exist
            System.out.println("📡 Connecting to PostgreSQL server...");
            createDatabaseIfNotExists();
            
            // 2. Connect to the database and setup tables
            System.out.println("📦 Connecting to database '" + DB_NAME + "'...");
            try (Connection conn = DriverManager.getConnection(BASE_URL + DB_NAME, USER, PASSWORD);
                 Statement stmt = conn.createStatement()) {

                // 3. Execute Schema SQL
                System.out.println("\n📋 Reading and executing schema.sql...");
                executeSqlFile(stmt, "/sql/schema.sql");
                System.out.println("✓ Schema created successfully!\n");

                // 4. Execute Seed Data SQL
                System.out.println("🌱 Reading and executing seed_data.sql...");
                executeSqlFile(stmt, "/sql/seed_data.sql");
                System.out.println("✓ Seed data inserted successfully!\n");

                // 5. Verify setup
                System.out.println("🔍 Verifying database setup...");
                verifySetup(stmt);

                System.out.println("\n✅ Database setup completed successfully!");
                System.out.println("━".repeat(60));
                System.out.println("You can now run the application.");
            }

        } catch (Exception e) {
            System.err.println("\n❌ Database setup failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void createDatabaseIfNotExists() {
        try (Connection conn = DriverManager.getConnection(BASE_URL + "postgres", USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            
            // Check if database exists
            var rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = '" + DB_NAME + "'");
            if (!rs.next()) {
                System.out.println("🔨 Database '" + DB_NAME + "' does not exist. Creating...");
                stmt.execute("CREATE DATABASE " + DB_NAME);
                System.out.println("✓ Database '" + DB_NAME + "' created successfully!");
            } else {
                System.out.println("✓ Database '" + DB_NAME + "' already exists.");
            }
            rs.close();
        } catch (Exception e) {
            System.err.println("❌ Failed to create database: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void executeSqlFile(Statement stmt, String resourcePath) throws Exception {
        InputStream is = DatabaseSetup.class.getResourceAsStream(resourcePath);
        if (is == null) {
            throw new RuntimeException(resourcePath + " not found in resources!");
        }

        String sqlContent;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            sqlContent = reader.lines().collect(Collectors.joining("\n"));
        }

        String[] statements = splitSqlStatements(sqlContent);
        
        int successCount = 0;
        for (String sql : statements) {
            sql = sql.trim();
            if (!sql.isEmpty() && !sql.startsWith("--")) {
                try {
                    stmt.execute(sql);
                    successCount++;
                } catch (Exception e) {
                    if (!e.getMessage().contains("does not exist") && !e.getMessage().contains("already exists")) {
                        System.out.println("  ⚠ Warning: " + e.getMessage().split("\n")[0]);
                    }
                }
            }
        }
        System.out.println("  Executed " + successCount + " SQL statements");
    }

    private static String[] splitSqlStatements(String sqlContent) {
        sqlContent = sqlContent.replaceAll("--[^\n]*\n", "\n");
        
        java.util.List<String> statements = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inDollarQuote = false;
        
        for (int i = 0; i < sqlContent.length(); i++) {
            char c = sqlContent.charAt(i);
            
            if (c == '$' && i + 1 < sqlContent.length() && sqlContent.charAt(i + 1) == '$') {
                inDollarQuote = !inDollarQuote;
                current.append("$$");
                i++;
            } else if (c == ';' && !inDollarQuote) {
                String stmt = current.toString().trim();
                if (!stmt.isEmpty()) {
                    statements.add(stmt);
                }
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        String lastStmt = current.toString().trim();
        if (!lastStmt.isEmpty()) {
            statements.add(lastStmt);
        }
        
        return statements.toArray(new String[0]);
    }

    private static void verifySetup(Statement stmt) throws Exception {
        String[] tables = {"users", "categories", "products", "orders", "order_items", "reviews", "inventory_logs"};
        
        System.out.println("\n  Table                   Row Count");
        System.out.println("  " + "─".repeat(40));
        
        for (String table : tables) {
            try {
                var rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table);
                if (rs.next()) {
                    System.out.printf("  %-24s %d%n", table, rs.getInt(1));
                }
                rs.close();
            } catch (Exception e) {
                System.out.printf("  %-24s ⚠ Error%n", table);
            }
        }
    }

    /**
     * Resets the database by dropping all tables and recreating them.
     */
    public static void resetDatabase() {
        System.out.println("⚠ Resetting database - all data will be lost!");
        main(new String[]{});
    }
}
