package com.bengkel.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    // Pastikan library sqlite-jdbc ada di pom.xml
    private static final String DATABASE_URL = "jdbc:sqlite:bengkel.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    public static void initialize() {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {

            // Tabel Admin
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS admin (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            username TEXT NOT NULL UNIQUE,
                            password TEXT NOT NULL
                        )
                    """);

            // Tabel Customer
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS customer (
                            id TEXT PRIMARY KEY,
                            nama TEXT NOT NULL,
                            no_telp TEXT NOT NULL,
                            alamat TEXT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        )
                    """);

            // Tabel Item
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS item (
                            id TEXT PRIMARY KEY,
                            nama_barang TEXT NOT NULL,
                            stok INTEGER NOT NULL DEFAULT 0,
                            harga REAL NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        )
                    """);

            // Tabel Transactions
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS transactions (
                            id TEXT PRIMARY KEY,
                            customer_id TEXT NOT NULL,
                            customer_name TEXT NOT NULL,
                            tanggal TEXT NOT NULL,
                            keluhan TEXT,
                            total_bayar REAL NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (customer_id) REFERENCES customer(id)
                        )
                    """);

            // Insert Default Admin
            stmt.execute("INSERT OR IGNORE INTO admin (username, password) VALUES ('admin', 'admin123')");

            System.out.println("Database tables initialized successfully.");

        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}