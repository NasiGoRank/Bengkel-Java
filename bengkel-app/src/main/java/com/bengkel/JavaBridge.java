package com.bengkel;

import javafx.scene.web.WebEngine;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.bengkel.database.DatabaseService;
import com.bengkel.util.PDFGenerator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JavaBridge {
    private WebEngine webEngine;
    private Stage primaryStage;

    public JavaBridge(WebEngine webEngine) {
        this.webEngine = webEngine;
    }

    // Setter untuk primaryStage (akan dipanggil dari App.java)
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    // ------------------------------------------------
    // LOGIN ADMIN
    // ------------------------------------------------
    public boolean loginAdmin(String username, String password) {
        try {
            System.out.println("Login attempt - Username: " + username);

            if (username == null || password == null ||
                    username.trim().isEmpty() || password.trim().isEmpty()) {
                showAlert("Username dan password harus diisi!");
                return false;
            }

            boolean isValid = DatabaseService.checkAdminCredentials(username.trim(), password.trim());

            if (isValid) {
                System.out.println("Login successful for user: " + username);
                return true;
            } else {
                showAlert("Username atau Password salah!");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            showAlert("Error saat login: " + e.getMessage());
            return false;
        }
    }

    // ------------------------------------------------
    // CUSTOMER CRUD
    // ------------------------------------------------
    public void saveCustomer(String id, String nama, String noTelp, String alamat) {
        try {
            System.out.println("Saving customer - Name: " + nama);

            if (nama == null || nama.trim().isEmpty() ||
                    noTelp == null || noTelp.trim().isEmpty()) {
                showAlert("Nama dan No Telp wajib diisi!");
                return;
            }

            String trimmedId = (id != null && !id.trim().isEmpty()) ? id.trim() : "";
            String trimmedNama = nama.trim();
            String trimmedTelp = noTelp.trim();
            String trimmedAlamat = (alamat != null) ? alamat.trim() : "";

            boolean success = DatabaseService.saveCustomer(trimmedId, trimmedNama, trimmedTelp, trimmedAlamat);

            if (success) {
                if (trimmedId.isEmpty()) {
                    showAlert("Customer berhasil ditambahkan!");
                } else {
                    showAlert("Customer berhasil diupdate!");
                }
            } else {
                showAlert("Gagal menyimpan customer!");
            }

            refreshTable("customer");

        } catch (Exception e) {
            System.err.println("Error saving customer: " + e.getMessage());
            showAlert("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteCustomer(String id) {
        try {
            System.out.println("Deleting customer - ID: " + id);

            if (id == null || id.trim().isEmpty()) {
                showAlert("ID Customer tidak valid!");
                return;
            }

            boolean removed = DatabaseService.deleteCustomer(id.trim());

            if (removed) {
                showAlert("Customer berhasil dihapus!");
            } else {
                showAlert("Customer tidak ditemukan!");
            }

            refreshTable("customer");

        } catch (Exception e) {
            System.err.println("Error deleting customer: " + e.getMessage());
            showAlert("Error: " + e.getMessage());
        }
    }

    public String searchCustomer(String query) {
        try {
            System.out.println("Searching customer - Query: " + query);

            List<String[]> customers;

            if (query == null || query.trim().isEmpty()) {
                customers = DatabaseService.getAllCustomers();
            } else {
                customers = DatabaseService.searchCustomers(query.trim());
            }

            System.out.println("Found " + customers.size() + " customers");
            return listToJson(customers, "customer");
        } catch (Exception e) {
            System.err.println("Error searching customer: " + e.getMessage());
            showAlert("Error searching: " + e.getMessage());
            return "[]";
        }
    }

    // ------------------------------------------------
    // ITEM CRUD
    // ------------------------------------------------
    public void saveItem(String id, String nama, String stokStr, String hargaStr) {
        try {
            System.out.println("Saving item - Name: " + nama);

            if (nama == null || nama.trim().isEmpty()) {
                showAlert("Nama barang wajib diisi!");
                return;
            }

            if (stokStr == null || stokStr.trim().isEmpty() ||
                    hargaStr == null || hargaStr.trim().isEmpty()) {
                showAlert("Stok dan harga wajib diisi!");
                return;
            }

            int stok;
            double harga;

            try {
                stok = Integer.parseInt(stokStr.trim());
                harga = Double.parseDouble(hargaStr.trim());
            } catch (NumberFormatException e) {
                showAlert("Stok dan harga harus berupa angka yang valid!");
                return;
            }

            if (stok < 0) {
                showAlert("Stok tidak boleh negatif!");
                return;
            }

            if (harga < 0) {
                showAlert("Harga tidak boleh negatif!");
                return;
            }

            String trimmedId = (id != null && !id.trim().isEmpty()) ? id.trim() : "";
            String trimmedNama = nama.trim();

            boolean success = DatabaseService.saveItem(trimmedId, trimmedNama, stok, harga);

            if (success) {
                if (trimmedId.isEmpty()) {
                    showAlert("Barang berhasil ditambahkan!");
                } else {
                    showAlert("Barang berhasil diupdate!");
                }
            } else {
                showAlert("Gagal menyimpan barang!");
            }

            refreshTable("item");

        } catch (Exception e) {
            System.err.println("Error saving item: " + e.getMessage());
            showAlert("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteItem(String id) {
        try {
            System.out.println("Deleting item - ID: " + id);

            if (id == null || id.trim().isEmpty()) {
                showAlert("ID Barang tidak valid!");
                return;
            }

            boolean removed = DatabaseService.deleteItem(id.trim());

            if (removed) {
                showAlert("Barang berhasil dihapus!");
            } else {
                showAlert("Barang tidak ditemukan!");
            }

            refreshTable("item");

        } catch (Exception e) {
            System.err.println("Error deleting item: " + e.getMessage());
            showAlert("Error: " + e.getMessage());
        }
    }

    public String searchItem(String query) {
        try {
            System.out.println("Searching item - Query: " + query);

            List<String[]> items;

            if (query == null || query.trim().isEmpty()) {
                items = DatabaseService.getAllItems();
            } else {
                items = DatabaseService.searchItems(query.trim());
            }

            System.out.println("Found " + items.size() + " items");
            return listToJson(items, "item");
        } catch (Exception e) {
            System.err.println("Error searching item: " + e.getMessage());
            showAlert("Error searching: " + e.getMessage());
            return "[]";
        }
    }

    // ------------------------------------------------
    // TRANSAKSI CRUD
    // ------------------------------------------------
    public void saveTransaction(String id, String custId, String tanggal,
            String keluhan, String totalStr) {
        try {
            System.out.println("Saving transaction - Customer ID: " + custId);

            if (custId == null || custId.trim().isEmpty()) {
                showAlert("Customer wajib dipilih!");
                return;
            }

            if (tanggal == null || tanggal.trim().isEmpty()) {
                showAlert("Tanggal wajib diisi!");
                return;
            }

            if (totalStr == null || totalStr.trim().isEmpty()) {
                showAlert("Total bayar wajib diisi!");
                return;
            }

            double total;
            try {
                total = Double.parseDouble(totalStr.trim());
            } catch (NumberFormatException e) {
                showAlert("Total bayar harus berupa angka yang valid!");
                return;
            }

            if (total < 0) {
                showAlert("Total bayar tidak boleh negatif!");
                return;
            }

            // Get customer name from database
            String custName = getCustomerNameById(custId.trim());
            if ("Unknown".equals(custName)) {
                showAlert("Customer tidak ditemukan!");
                return;
            }

            String trimmedId = (id != null && !id.trim().isEmpty()) ? id.trim() : "";
            String trimmedTanggal = tanggal.trim();
            String trimmedKeluhan = (keluhan != null) ? keluhan.trim() : "";

            boolean success = DatabaseService.saveTransaction(
                    trimmedId, custId.trim(), custName,
                    trimmedTanggal, trimmedKeluhan, total);

            if (success) {
                if (trimmedId.isEmpty()) {
                    showAlert("Transaksi berhasil dibuat!");
                } else {
                    showAlert("Transaksi berhasil diupdate!");
                }
            } else {
                showAlert("Gagal menyimpan transaksi!");
            }

            refreshTable("transaction");

        } catch (Exception e) {
            System.err.println("Error saving transaction: " + e.getMessage());
            showAlert("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteTransaction(String id) {
        try {
            System.out.println("Deleting transaction - ID: " + id);

            if (id == null || id.trim().isEmpty()) {
                showAlert("ID Transaksi tidak valid!");
                return;
            }

            boolean removed = DatabaseService.deleteTransaction(id.trim());

            if (removed) {
                showAlert("Transaksi berhasil dihapus!");
            } else {
                showAlert("Transaksi tidak ditemukan!");
            }

            refreshTable("transaction");

        } catch (Exception e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
            showAlert("Error: " + e.getMessage());
        }
    }

    // ------------------------------------------------
    // LAPORAN
    // ------------------------------------------------
    public String getReport(String startDate, String endDate) {
        try {
            System.out.println("Getting report - Start: " + startDate + ", End: " + endDate);

            if (startDate == null || startDate.trim().isEmpty() ||
                    endDate == null || endDate.trim().isEmpty()) {
                showAlert("Tanggal mulai dan tanggal akhir wajib diisi!");
                return "[]";
            }

            List<String[]> report = DatabaseService.getReport(startDate.trim(), endDate.trim());
            System.out.println("Report found " + report.size() + " transactions");
            return listToJson(report, "transaction");

        } catch (Exception e) {
            System.err.println("Error getting report: " + e.getMessage());
            showAlert("Error: " + e.getMessage());
            return "[]";
        }
    }

    // ------------------------------------------------
    // LAPORAN PDF
    // ------------------------------------------------
    public void generatePDFReport(String startDate, String endDate) {
        try {
            System.out.println("Generating PDF report from " + startDate + " to " + endDate);

            if (startDate == null || startDate.trim().isEmpty() ||
                    endDate == null || endDate.trim().isEmpty()) {
                showAlert("Tanggal mulai dan tanggal akhir wajib diisi!");
                return;
            }

            // Validasi tanggal
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date start = sdf.parse(startDate.trim());
            Date end = sdf.parse(endDate.trim());

            if (start.after(end)) {
                showAlert("Tanggal mulai tidak boleh lebih besar dari tanggal akhir!");
                return;
            }

            // Get report data
            List<String[]> reportData = DatabaseService.getReport(startDate.trim(), endDate.trim());

            if (reportData.isEmpty()) {
                showAlert("Tidak ada data transaksi untuk periode yang dipilih!");
                return;
            }

            // Calculate total revenue
            double totalRevenue = DatabaseService.getRevenueByPeriod(startDate.trim(), endDate.trim());

            // Show FileChooser untuk memilih lokasi penyimpanan
            String savePath = showSaveDialog(startDate.trim(), endDate.trim());

            if (savePath == null || savePath.isEmpty()) {
                showAlert("Penyimpanan PDF dibatalkan!");
                return;
            }

            // Generate PDF di lokasi yang dipilih
            String pdfPath = PDFGenerator.generateReportPDF(reportData, startDate.trim(), endDate.trim(), totalRevenue,
                    savePath);

            // Show success message
            File pdfFile = new File(pdfPath);
            showAlert("Laporan PDF berhasil disimpan di:\n" + pdfFile.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Error generating PDF: " + e.getMessage());
            showAlert("Error membuat PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

private String showSaveDialog(String startDate, String endDate) {
    try {
        // Buat nama file default
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = dateFormat.format(new Date());
        String defaultFileName = "Laporan_" + startDate + "_to_" + endDate + "_" + timestamp + ".pdf";

        // Gunakan Platform.runLater untuk FileChooser
        final String[] savePath = new String[1];
        final CountDownLatch latch = new CountDownLatch(1);
        
        javafx.application.Platform.runLater(() -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Simpan Laporan PDF");
                fileChooser.setInitialFileName(defaultFileName);
                
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                        "PDF files (*.pdf)", "*.pdf");
                fileChooser.getExtensionFilters().add(extFilter);
                
                String userHome = System.getProperty("user.home");
                File documentsDir = new File(userHome, "Documents");
                if (documentsDir.exists() && documentsDir.isDirectory()) {
                    fileChooser.setInitialDirectory(documentsDir);
                } else {
                    fileChooser.setInitialDirectory(new File(userHome));
                }
                
                File file = fileChooser.showSaveDialog(primaryStage);
                
                if (file != null) {
                    String filePath = file.getAbsolutePath();
                    if (!filePath.toLowerCase().endsWith(".pdf")) {
                        filePath += ".pdf";
                    }
                    savePath[0] = filePath;
                }
            } catch (Exception e) {
                System.err.println("Error in FileChooser: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        
        latch.await(30, TimeUnit.SECONDS);
        return savePath[0];
        
    } catch (Exception e) {
        System.err.println("Error showing save dialog: " + e.getMessage());
        showAlert("Error membuka dialog penyimpanan: " + e.getMessage());
        return null;
    }
}

    // ------------------------------------------------
    // DASHBOARD DATA
    // ------------------------------------------------
    public String getDashboardData() {
        try {
            int customerCount = DatabaseService.getCustomerCount();
            int itemCount = DatabaseService.getItemCount();
            int transactionCount = DatabaseService.getTransactionCount();
            double revenue = DatabaseService.getTotalRevenue();

            return String.format(
                    "{\"customers\":%d,\"items\":%d,\"transactions\":%d,\"revenue\":%.2f}",
                    customerCount, itemCount, transactionCount, revenue);
        } catch (Exception e) {
            System.err.println("Error getting dashboard data: " + e.getMessage());
            return "{\"customers\":0,\"items\":0,\"transactions\":0,\"revenue\":0}";
        }
    }

    // ------------------------------------------------
    // HELPER METHODS
    // ------------------------------------------------
    public String getAllData(String type) {
        try {
            System.out.println("Getting all data - Type: " + type);

            List<String[]> data;

            switch (type.toLowerCase()) {
                case "customers":
                    data = DatabaseService.getAllCustomers();
                    break;
                case "items":
                    data = DatabaseService.getAllItems();
                    break;
                case "transactions":
                    data = DatabaseService.getAllTransactions();
                    break;
                default:
                    return "[]";
            }

            System.out.println("Found " + data.size() + " " + type);
            return listToJson(data, type.substring(0, type.length() - 1)); // Remove 's'
        } catch (Exception e) {
            System.err.println("Error getting data: " + e.getMessage());
            showAlert("Error getting data: " + e.getMessage());
            return "[]";
        }
    }

    private String getCustomerNameById(String id) {
        try {
            List<String[]> customers = DatabaseService.getAllCustomers();
            for (String[] customer : customers) {
                if (customer[0].equals(id)) {
                    return customer[1];
                }
            }
            return "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private void showAlert(String msg) {
        try {
            System.out.println("Showing alert: " + msg);

            // Escape single quotes untuk JavaScript
            String escapedMsg = msg.replace("'", "\\'")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
            webEngine.executeScript("alert('" + escapedMsg + "')");
        } catch (Exception e) {
            System.err.println("Error showing alert: " + e.getMessage());
        }
    }

    private void refreshTable(String type) {
        try {
            System.out.println("Refreshing table: " + type);
            webEngine.executeScript("loadTable('" + type + "')");
        } catch (Exception e) {
            System.err.println("Error refreshing table: " + e.getMessage());
        }
    }

    private String listToJson(List<String[]> list, String type) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            String[] item = list.get(i);

            if (type.equals("customer")) {
                json.append(String.format(
                        "{\"id\":\"%s\",\"nama\":\"%s\",\"noTelp\":\"%s\",\"alamat\":\"%s\"}",
                        escapeJson(item[0]), escapeJson(item[1]),
                        escapeJson(item[2]), escapeJson(item[3])));
            } else if (type.equals("item")) {
                json.append(String.format(
                        "{\"id\":\"%s\",\"nama\":\"%s\",\"stok\":%s,\"harga\":%s}",
                        escapeJson(item[0]), escapeJson(item[1]),
                        item[2], item[3]));
            } else if (type.equals("transaction")) {
                json.append(String.format(
                        "{\"id\":\"%s\",\"custId\":\"%s\",\"custName\":\"%s\"," +
                                "\"tanggal\":\"%s\",\"keluhan\":\"%s\",\"total\":%s}",
                        escapeJson(item[0]), escapeJson(item[1]),
                        escapeJson(item[2]), escapeJson(item[3]),
                        escapeJson(item[4]), item[5]));
            }

            if (i < list.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    private String escapeJson(String str) {
        if (str == null)
            return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\f", "\\f");
    }
}