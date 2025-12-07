package com.bengkel;

import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import com.bengkel.database.DatabaseConnection;
import com.bengkel.database.DatabaseService;
import java.util.Objects;

public class App extends Application {
    private JavaBridge bridge;

    @Override
    public void start(Stage stage) {
        // Optimasi rendering WebView
        System.setProperty("prism.order", "sw");
        System.setProperty("prism.text", "t2k");
        System.setProperty("java.awt.headless", "false");

        // --- DATABASE INIT ---
        System.out.println("Initializing database...");
        DatabaseConnection.initialize(); // Buat tabel
        DatabaseService.addSampleData(); // Isi data dummy jika kosong

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        webEngine.setJavaScriptEnabled(true);

        // Debugging: Tampilkan alert dari JS ke Terminal Java
        webEngine.setOnAlert(event -> {
            System.out.println("JavaScript Alert: " + event.getData());
        });

        // Error Handling
        webEngine.getLoadWorker().exceptionProperty().addListener((obs, oldExc, newExc) -> {
            if (newExc != null) {
                System.err.println("WebEngine Error: " + newExc.getMessage());
                newExc.printStackTrace();
            }
        });

        // Setup JavaBridge (Penghubung JS <-> Java)
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == Worker.State.SUCCEEDED) {
                try {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    this.bridge = new JavaBridge(webEngine);
                    window.setMember("app", this.bridge);
                    System.out.println("JavaBridge initialized successfully");
                    webEngine.executeScript("console.log('JavaFX WebView ready')");
                } catch (Exception e) {
                    System.err.println("Error initializing JavaBridge: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        // Load HTML
        try {
            String url = Objects.requireNonNull(getClass().getResource("/web/index.html")).toExternalForm();
            System.out.println("Loading URL: " + url);
            webEngine.load(url);
        } catch (Exception e) {
            System.err.println("Error loading HTML: " + e.getMessage());
            webEngine.loadContent(getSimpleHTML());
        }

        Scene scene = new Scene(webView, 1200, 800);
        stage.setScene(scene);
        stage.setTitle("Bengkel Khayangan Mobil - System");
        stage.show();

        System.out.println("Application started successfully");
    }

    private String getSimpleHTML() {
        return """
                <!DOCTYPE html>
                <html>
                <head><title>Error</title></head>
                <body style="background:#111;color:white;text-align:center;padding:50px;">
                    <h1>⚠️ File HTML Tidak Ditemukan</h1>
                    <p>Pastikan file index.html ada di src/main/resources/web/</p>
                </body>
                </html>
                """;
    }

    public static void main(String[] args) {
        launch(args);
    }
}