package com.amalitech.smartecommerce.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class InitDB {

    private String readResourceFile(String path) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new FileNotFoundException("Resource not found: " + path);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public void createTables() throws SQLException {

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            String sql = readResourceFile("sql/schema.sql");

            stmt.execute(sql);

            IO.println("Tables created successfully");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void seedTableWithData() throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            String sql = readResourceFile("sql/seed_data.sql");
            stmt.execute(sql);
            IO.println("Seeding table with data created successfully");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

