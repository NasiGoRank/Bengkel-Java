package com.bengkel.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class PDFGenerator {

        public static String generateReportPDF(List<String[]> reportData,
                        String startDate,
                        String endDate,
                        double totalRevenue,
                        String savePath) throws IOException {

                // Buat parent directory jika belum ada
                File pdfFile = new File(savePath);
                File parentDir = pdfFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                        parentDir.mkdirs();
                }

                try (PDDocument document = new PDDocument()) {
                        List<PDPage> pages = new java.util.ArrayList<>();
                        PDPage currentPage = new PDPage(PDRectangle.A4);
                        pages.add(currentPage);
                        document.addPage(currentPage);

                        PDPageContentStream contentStream = null;

                        try {
                                contentStream = new PDPageContentStream(document, currentPage);

                                // Set margin dan posisi awal
                                float margin = 50;
                                float yPosition = currentPage.getMediaBox().getHeight() - margin;
                                float leading = 14;
                                float bottomMargin = 70;

                                // Judul
                                contentStream.beginText();
                                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                                contentStream.newLineAtOffset(margin, yPosition);
                                contentStream.showText("LAPORAN TRANSAKSI BENGKEL KHAYANGAN MOBIL");
                                contentStream.endText();

                                yPosition -= 30;
                                contentStream.beginText();
                                contentStream.setFont(PDType1Font.HELVETICA, 12);
                                contentStream.newLineAtOffset(margin, yPosition);
                                contentStream.showText(
                                                "Periode: " + formatDate(startDate) + " s/d " + formatDate(endDate));
                                contentStream.endText();

                                yPosition -= 30;
                                contentStream.beginText();
                                contentStream.setFont(PDType1Font.HELVETICA, 10);
                                contentStream.newLineAtOffset(margin, yPosition);
                                contentStream.showText("Tanggal Cetak: "
                                                + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
                                contentStream.endText();

                                // Garis pemisah
                                yPosition -= 20;
                                contentStream.moveTo(margin, yPosition);
                                contentStream.lineTo(currentPage.getMediaBox().getWidth() - margin, yPosition);
                                contentStream.stroke();

                                // Header tabel
                                yPosition -= 20;
                                float[] columnWidths = { 80, 80, 120, 120, 80 };
                                String[] headers = { "Tanggal", "ID", "Customer", "Keluhan", "Total" };

                                // Tulis header tabel
                                float tableX = margin;
                                for (int i = 0; i < headers.length; i++) {
                                        contentStream.beginText();
                                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                                        contentStream.newLineAtOffset(tableX, yPosition);
                                        contentStream.showText(headers[i]);
                                        contentStream.endText();
                                        tableX += columnWidths[i];
                                }

                                // Garis bawah header
                                yPosition -= 2;
                                contentStream.moveTo(margin, yPosition);
                                contentStream.lineTo(currentPage.getMediaBox().getWidth() - margin, yPosition);
                                contentStream.stroke();

                                // Data tabel
                                yPosition -= 15;
                                contentStream.setFont(PDType1Font.HELVETICA, 9);

                                int currentPageIndex = 0;
                                int maxRowsPerPage = calculateMaxRows(reportData.size());

                                for (int i = 0; i < reportData.size(); i++) {
                                        String[] row = reportData.get(i);

                                        // Jika halaman habis, buat halaman baru
                                        if (yPosition < bottomMargin && i < reportData.size() - 1) {
                                                // Tutup stream yang lama
                                                contentStream.close();

                                                // Buat halaman baru
                                                currentPage = new PDPage(PDRectangle.A4);
                                                pages.add(currentPage);
                                                document.addPage(currentPage);
                                                contentStream = new PDPageContentStream(document, currentPage);
                                                currentPageIndex++;

                                                // Reset posisi
                                                yPosition = currentPage.getMediaBox().getHeight() - margin;
                                                contentStream.setFont(PDType1Font.HELVETICA, 9);

                                                // Header halaman baru
                                                contentStream.beginText();
                                                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                                                contentStream.newLineAtOffset(margin, yPosition);
                                                contentStream.showText(
                                                                "Laporan (lanjutan) - Halaman "
                                                                                + (currentPageIndex + 1));
                                                contentStream.endText();
                                                yPosition -= 30;

                                                // Tulis header tabel lagi
                                                tableX = margin;
                                                for (int j = 0; j < headers.length; j++) {
                                                        contentStream.beginText();
                                                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                                                        contentStream.newLineAtOffset(tableX, yPosition);
                                                        contentStream.showText(headers[j]);
                                                        contentStream.endText();
                                                        tableX += columnWidths[j];
                                                }

                                                yPosition -= 15;
                                        }

                                        // Tulis baris data
                                        tableX = margin;

                                        // Tanggal
                                        contentStream.beginText();
                                        contentStream.newLineAtOffset(tableX, yPosition);
                                        contentStream.showText(formatDate(row[3]));
                                        contentStream.endText();
                                        tableX += columnWidths[0];

                                        // ID (singkat)
                                        String shortId = row[0].length() > 8 ? row[0].substring(0, 8) + "..." : row[0];
                                        contentStream.beginText();
                                        contentStream.newLineAtOffset(tableX, yPosition);
                                        contentStream.showText(shortId);
                                        contentStream.endText();
                                        tableX += columnWidths[1];

                                        // Customer
                                        contentStream.beginText();
                                        contentStream.newLineAtOffset(tableX, yPosition);
                                        contentStream.showText(truncateText(row[2], 20));
                                        contentStream.endText();
                                        tableX += columnWidths[2];

                                        // Keluhan
                                        String keluhan = row[4] != null && !row[4].isEmpty() ? row[4] : "-";
                                        contentStream.beginText();
                                        contentStream.newLineAtOffset(tableX, yPosition);
                                        contentStream.showText(truncateText(keluhan, 25));
                                        contentStream.endText();
                                        tableX += columnWidths[3];

                                        // Total
                                        contentStream.beginText();
                                        contentStream.newLineAtOffset(tableX, yPosition);
                                        contentStream.showText("Rp " + formatCurrency(Double.parseDouble(row[5])));
                                        contentStream.endText();

                                        yPosition -= 15;
                                }

                                // Summary di halaman terakhir
                                if (currentPageIndex == pages.size() - 1 || reportData.size() <= maxRowsPerPage) {
                                        yPosition -= 20;
                                        contentStream.beginText();
                                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                                        contentStream.newLineAtOffset(margin, yPosition);
                                        contentStream.showText("Ringkasan Laporan:");
                                        contentStream.endText();

                                        yPosition -= 15;
                                        contentStream.beginText();
                                        contentStream.setFont(PDType1Font.HELVETICA, 10);
                                        contentStream.newLineAtOffset(margin, yPosition);
                                        contentStream.showText("Total Transaksi: " + reportData.size());
                                        contentStream.endText();

                                        yPosition -= 15;
                                        contentStream.beginText();
                                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                                        contentStream.newLineAtOffset(margin, yPosition);
                                        contentStream.showText("Total Pendapatan: Rp " + formatCurrency(totalRevenue));
                                        contentStream.endText();
                                }

                        } finally {
                                // Pastikan contentStream ditutup
                                if (contentStream != null) {
                                        contentStream.close();
                                }
                        }

                        // Simpan PDF
                        document.save(savePath);
                        System.out.println("PDF saved to: " + savePath);

                } catch (Exception e) {
                        System.err.println("Error in PDF generation: " + e.getMessage());
                        e.printStackTrace();
                        throw e;
                }

                return savePath;
        }

        private static int calculateMaxRows(int totalRows) {
                // Perkiraan maksimal baris per halaman
                if (totalRows <= 25)
                        return 25;
                if (totalRows <= 50)
                        return 25;
                return 30; // Untuk data yang sangat banyak
        }

        /**
         * Format date string from yyyy-MM-dd to dd/MM/yyyy
         */
        private static String formatDate(String dateString) {
                if (dateString == null || dateString.isEmpty()) {
                        return "-";
                }

                try {
                        // Try to parse various common date formats
                        SimpleDateFormat[] possibleFormats = {
                                        new SimpleDateFormat("yyyy-MM-dd"),
                                        new SimpleDateFormat("dd/MM/yyyy"),
                                        new SimpleDateFormat("MM/dd/yyyy"),
                                        new SimpleDateFormat("yyyy/MM/dd")
                        };

                        Date date = null;
                        for (SimpleDateFormat sdf : possibleFormats) {
                                try {
                                        sdf.setLenient(false);
                                        date = sdf.parse(dateString);
                                        break;
                                } catch (ParseException e) {
                                        // Try next format
                                }
                        }

                        if (date != null) {
                                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
                                return outputFormat.format(date);
                        }
                } catch (Exception e) {
                        // If parsing fails, return the original string
                }

                return dateString; // Return original if cannot parse
        }

        /**
         * Truncate text to specified length and add ellipsis if needed
         */
        private static String truncateText(String text, int maxLength) {
                if (text == null) {
                        return "-";
                }

                if (text.length() <= maxLength) {
                        return text;
                }

                return text.substring(0, maxLength - 3) + "...";
        }

        /**
         * Format currency with thousands separator
         */
        private static String formatCurrency(double amount) {
                DecimalFormat formatter = new DecimalFormat("#,###");
                return formatter.format(amount);
        }
}