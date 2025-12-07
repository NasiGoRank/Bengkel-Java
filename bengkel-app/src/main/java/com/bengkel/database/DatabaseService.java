package com.bengkel.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseService {

    // --- CUSTOMER ---
    public static boolean saveCustomer(String id, String nama, String noTelp, String alamat) {
        String sql;
        if (id == null || id.trim().isEmpty()) {
            id = UUID.randomUUID().toString();
            sql = "INSERT INTO customer (id, nama, no_telp, alamat) VALUES (?, ?, ?, ?)";
        } else {
            sql = "UPDATE customer SET nama = ?, no_telp = ?, alamat = ? WHERE id = ?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (sql.startsWith("INSERT")) {
                pstmt.setString(1, id);
                pstmt.setString(2, nama);
                pstmt.setString(3, noTelp);
                pstmt.setString(4, alamat);
            } else {
                pstmt.setString(1, nama);
                pstmt.setString(2, noTelp);
                pstmt.setString(3, alamat);
                pstmt.setString(4, id);
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteCustomer(String id) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM customer WHERE id = ?")) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String[]> getAllCustomers() {
        List<String[]> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT id, nama, no_telp, alamat FROM customer ORDER BY nama")) {
            while (rs.next()) {
                list.add(new String[] { rs.getString("id"), rs.getString("nama"), rs.getString("no_telp"),
                        rs.getString("alamat") });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<String[]> searchCustomers(String query) {
        List<String[]> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn
                        .prepareStatement("SELECT id, nama, no_telp, alamat FROM customer WHERE nama LIKE ?")) {
            pstmt.setString(1, "%" + query + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[] { rs.getString("id"), rs.getString("nama"), rs.getString("no_telp"),
                            rs.getString("alamat") });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- ITEM ---
    public static boolean saveItem(String id, String nama, int stok, double harga) {
        String sql = (id == null || id.trim().isEmpty())
                ? "INSERT INTO item (id, nama_barang, stok, harga) VALUES (?, ?, ?, ?)"
                : "UPDATE item SET nama_barang = ?, stok = ?, harga = ? WHERE id = ?";
        String finalId = (id == null || id.trim().isEmpty()) ? UUID.randomUUID().toString() : id;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (sql.startsWith("INSERT")) {
                pstmt.setString(1, finalId);
                pstmt.setString(2, nama);
                pstmt.setInt(3, stok);
                pstmt.setDouble(4, harga);
            } else {
                pstmt.setString(1, nama);
                pstmt.setInt(2, stok);
                pstmt.setDouble(3, harga);
                pstmt.setString(4, finalId);
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteItem(String id) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM item WHERE id = ?")) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String[]> getAllItems() {
        List<String[]> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt
                        .executeQuery("SELECT id, nama_barang, stok, harga FROM item ORDER BY nama_barang")) {
            while (rs.next()) {
                list.add(new String[] { rs.getString("id"), rs.getString("nama_barang"),
                        String.valueOf(rs.getInt("stok")), String.valueOf(rs.getDouble("harga")) });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<String[]> searchItems(String query) {
        List<String[]> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn
                        .prepareStatement("SELECT id, nama_barang, stok, harga FROM item WHERE nama_barang LIKE ?")) {
            pstmt.setString(1, "%" + query + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[] { rs.getString("id"), rs.getString("nama_barang"),
                            String.valueOf(rs.getInt("stok")), String.valueOf(rs.getDouble("harga")) });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- TRANSACTION ---
    public static boolean saveTransaction(String id, String custId, String custName, String tanggal, String keluhan,
            double total) {
        String sql = (id == null || id.trim().isEmpty())
                ? "INSERT INTO transactions (id, customer_id, customer_name, tanggal, keluhan, total_bayar) VALUES (?, ?, ?, ?, ?, ?)"
                : "UPDATE transactions SET customer_id=?, customer_name=?, tanggal=?, keluhan=?, total_bayar=? WHERE id=?";
        String finalId = (id == null || id.trim().isEmpty()) ? UUID.randomUUID().toString() : id;

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (sql.startsWith("INSERT")) {
                pstmt.setString(1, finalId);
                pstmt.setString(2, custId);
                pstmt.setString(3, custName);
                pstmt.setString(4, tanggal);
                pstmt.setString(5, keluhan);
                pstmt.setDouble(6, total);
            } else {
                pstmt.setString(1, custId);
                pstmt.setString(2, custName);
                pstmt.setString(3, tanggal);
                pstmt.setString(4, keluhan);
                pstmt.setDouble(5, total);
                pstmt.setString(6, finalId);
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteTransaction(String id) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement("DELETE FROM transactions WHERE id = ?")) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<String[]> getAllTransactions() {
        List<String[]> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(
                        "SELECT id, customer_id, customer_name, tanggal, keluhan, total_bayar FROM transactions ORDER BY tanggal DESC")) {
            while (rs.next()) {
                list.add(new String[] { rs.getString("id"), rs.getString("customer_id"), rs.getString("customer_name"),
                        rs.getString("tanggal"), rs.getString("keluhan"),
                        String.valueOf(rs.getDouble("total_bayar")) });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<String[]> getReport(String start, String end) {
        List<String[]> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT id, customer_id, customer_name, tanggal, keluhan, total_bayar FROM transactions WHERE tanggal BETWEEN ? AND ? ORDER BY tanggal")) {
            pstmt.setString(1, start);
            pstmt.setString(2, end);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[] { rs.getString("id"), rs.getString("customer_id"),
                            rs.getString("customer_name"), rs.getString("tanggal"), rs.getString("keluhan"),
                            String.valueOf(rs.getDouble("total_bayar")) });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- DASHBOARD & UTILS ---
    public static boolean checkAdminCredentials(String user, String pass) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn
                        .prepareStatement("SELECT COUNT(*) FROM admin WHERE username=? AND password=?")) {
            pstmt.setString(1, user);
            pstmt.setString(2, pass);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static double getRevenueByPeriod(String start, String end) {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT SUM(total_bayar) FROM transactions WHERE tanggal BETWEEN ? AND ?")) {
            pstmt.setString(1, start);
            pstmt.setString(2, end);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    public static int getCustomerCount() {
        return getCount("SELECT COUNT(*) FROM customer");
    }

    public static int getItemCount() {
        return getCount("SELECT COUNT(*) FROM item");
    }

    public static int getTransactionCount() {
        return getCount("SELECT COUNT(*) FROM transactions");
    }

    public static double getTotalRevenue() {
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT SUM(total_bayar) FROM transactions")) {
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (SQLException e) {
            return 0.0;
        }
    }

    private static int getCount(String sql) {
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    public static void addSampleData() {
        if (getCustomerCount() == 0) {
            saveCustomer(null, "John Doe", "0812345", "Jakarta");
            saveItem(null, "Oli Mesin", 10, 50000);
            System.out.println("Sample data added.");
        }
    }
}