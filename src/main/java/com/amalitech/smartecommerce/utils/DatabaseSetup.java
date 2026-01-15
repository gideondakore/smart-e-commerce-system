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
 * Reads schema.sql and seed_data.sql from resources and executes them.
 */
public class DatabaseSetup {

    private static final String BASE_URL = "jdbc:postgresql://localhost:5432/";
    private static final String DB_NAME = System.getenv("DB_NAME");
    private static final String USER = System.getenv("POSTGRES_USER");
    private static final String PASSWORD = System.getenv("POSTGRES_PASSWORD");

    public static void main(String[] args) {
        IO.println("╔══════════════════════════════════════════════════════════════╗");
        IO.println("║           SMART E-COMMERCE DATABASE SETUP                    ║");
        IO.println("╚══════════════════════════════════════════════════════════════╝\n");

        try {

            // 1. Connect to PostgreSQL Server
            IO.println("📡 Connecting to PostgreSQL server...");
            
            // Try connecting to the database directly (it should exist)
            IO.println("📦 Connecting to database '" + DB_NAME + "'...");
            try (Connection conn = DriverManager.getConnection(BASE_URL + DB_NAME, USER, PASSWORD);
                 Statement stmt = conn.createStatement()) {

                // 2. Execute Schema SQL
                IO.println("\n📋 Reading and executing schema.sql...");
                executeSqlFile(stmt, "/sql/schema.sql");
                IO.println("✓ Schema created successfully!\n");

                // 3. Execute Seed Data SQL
                IO.println("🌱 Reading and executing seed_data.sql...");
                executeSqlFile(stmt, "/sql/seed_data.sql");
                IO.println("✓ Seed data inserted successfully!\n");

                // 4. Verify setup
                IO.println("🔍 Verifying database setup...");
                verifySetup(stmt);

                IO.println("\n✅ Database setup completed successfully!");
                IO.println("━".repeat(60));
                IO.println("You can now run the application.");
            }

        } catch (Exception e) {
            System.err.println("\n❌ Database setup failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
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

        // PostgreSQL can execute multiple statements in one go with proper handling
        // Split by semicolon but handle functions/triggers specially
        String[] statements = splitSqlStatements(sqlContent);
        
        int successCount = 0;
        for (String sql : statements) {
            sql = sql.trim();
            if (!sql.isEmpty() && !sql.startsWith("--")) {
                try {
                    stmt.execute(sql);
                    successCount++;
                } catch (Exception e) {
                    // Log but continue for DROP IF EXISTS errors
                    if (!e.getMessage().contains("does not exist")) {
                        IO.println("  ⚠ Warning: " + e.getMessage().split("\n")[0]);
                    }
                }
            }
        }
        IO.println("  Executed " + successCount + " SQL statements");
    }

    /**
     * Splits SQL content into individual statements, handling functions and triggers.
     */
    private static String[] splitSqlStatements(String sqlContent) {
        // Handle PostgreSQL functions that contain semicolons within $$ blocks
        StringBuilder processed = new StringBuilder();
        boolean inDollarQuote = false;
        
        for (int i = 0; i < sqlContent.length(); i++) {
            char c = sqlContent.charAt(i);
            
            // Check for $$ dollar quoting
            if (c == '$' && i + 1 < sqlContent.length() && sqlContent.charAt(i + 1) == '$') {
                inDollarQuote = !inDollarQuote;
                processed.append(c);
            } else if (c == ';' && !inDollarQuote) {
                processed.append(";\n---STATEMENT_SEPARATOR---\n");
            } else {
                processed.append(c);
            }
        }
        
        return processed.toString().split("---STATEMENT_SEPARATOR---");
    }

    private static void verifySetup(Statement stmt) throws Exception {
        String[] tables = {"users", "categories", "products", "orders", "order_items", "reviews", "inventory_logs"};
        
        IO.println("\n  Table                   Row Count");
        IO.println("  " + "─".repeat(40));
        
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
        IO.println("⚠ Resetting database - all data will be lost!");
        main(new String[]{});
    }
}
