package com.bengkel.util;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.io.font.constants.StandardFonts;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PDFGenerator {

    public static String generateReportPDF(List<String[]> reportData,
            String startDate,
            String endDate,
            double totalRevenue) throws IOException {

        // Create timestamp for filename
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = dateFormat.format(new Date());

        // Create directory if not exists
        File reportsDir = new File("reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdir();
        }

        String fileName = "reports/Laporan_" + startDate + "_to_" + endDate + "_" + timestamp + ".pdf";
        String filePath = new File(fileName).getAbsolutePath();

        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Set font
        PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
        PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

        // Header
        document.add(new Paragraph("LAPORAN TRANSAKSI BENGKEL KHAYANGAN MOBIL")
                .setFont(fontBold)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Periode: " + formatDate(startDate) + " s/d " + formatDate(endDate))
                .setFont(font)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));

        // Create table
        float[] columnWidths = { 2, 2, 3, 4, 2, 2 };
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // Table headers
        String[] headers = { "Tanggal", "ID", "Customer", "Keluhan", "Total", "Status" };
        for (String header : headers) {
            table.addHeaderCell(new Cell().add(new Paragraph(header)
                    .setFont(fontBold)
                    .setFontSize(10))
                    .setTextAlignment(TextAlignment.CENTER));
        }

        // Add data rows
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");

        for (String[] row : reportData) {
            try {
                Date date = inputFormat.parse(row[3]);
                String formattedDate = outputFormat.format(date);
                table.addCell(new Cell().add(new Paragraph(formattedDate))
                        .setFont(font).setFontSize(9));
            } catch (Exception e) {
                table.addCell(new Cell().add(new Paragraph(row[3]))
                        .setFont(font).setFontSize(9));
            }

            table.addCell(new Cell().add(new Paragraph(row[0].length() > 8 ? row[0].substring(0, 8) + "..." : row[0]))
                    .setFont(font).setFontSize(9));

            table.addCell(new Cell().add(new Paragraph(row[2]))
                    .setFont(font).setFontSize(9));

            table.addCell(new Cell().add(new Paragraph(row[4] != null ? row[4] : "-"))
                    .setFont(font).setFontSize(9));

            table.addCell(new Cell().add(new Paragraph(formatCurrency(Double.parseDouble(row[5]))))
                    .setFont(font).setFontSize(9)
                    .setTextAlignment(TextAlignment.RIGHT));

            table.addCell(new Cell().add(new Paragraph("Selesai"))
                    .setFont(font).setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        document.add(table);

        // Summary
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Ringkasan Laporan:")
                .setFont(fontBold)
                .setFontSize(12));

        document.add(new Paragraph("Total Transaksi: " + reportData.size())
                .setFont(font)
                .setFontSize(10));

        document.add(new Paragraph("Total Pendapatan: " + formatCurrency(totalRevenue))
                .setFont(fontBold)
                .setFontSize(11));

        document.add(new Paragraph("Tanggal Cetak: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()))
                .setFont(font)
                .setFontSize(9)
                .setTextAlignment(TextAlignment.RIGHT));

        document.close();

        return filePath;
    }

    private static String formatDate(String dateStr) {
        try {
            SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat output = new SimpleDateFormat("dd/MM/yyyy");
            return output.format(input.parse(dateStr));
        } catch (Exception e) {
            return dateStr;
        }
    }

    private static String formatCurrency(double amount) {
        return String.format("Rp %,.0f", amount);
    }
}