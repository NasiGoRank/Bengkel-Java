package com.bengkel.util;

import com.bengkel.model.Transaction;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PDFGenerator {

    public static ByteArrayOutputStream generateToStream(List<Transaction> transactions, String start, String end,
            double revenue) throws IOException {
        try (PDDocument document = new PDDocument();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Logika layout PDF sama seperti sebelumnya...
                // Hanya saja sekarang data diambil dari List<Transaction> bukan List<String[]>

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("LAPORAN BENGKEL KHAYANGAN MOBIL");
                contentStream.endText();

                // ... (Implementasi detail tabel disederhanakan untuk ringkas)
                // Iterasi transaksi:
                int y = 700;
                for (Transaction t : transactions) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 10);
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText(
                            t.getTanggal() + " - " + t.getCustomerName() + " - Rp " + formatRupiah(t.getTotalBayar()));
                    contentStream.endText();
                    y -= 20;
                }
            }

            document.save(out);
            return out;
        }
    }

    private static String formatRupiah(Double val) {
        return NumberFormat.getNumberInstance(Locale.US).format(val);
    }
}