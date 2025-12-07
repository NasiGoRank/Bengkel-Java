module com.bengkel {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires jdk.jsobject;

    // Wajib ada untuk akses database JDBC & SQLite
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    // Untuk Java Desktop API (untuk Desktop.getDesktop() jika diperlukan)
    requires java.desktop;

    // Untuk iText7 PDF (nama modul dari META-INF/MANIFEST.MF di JAR)
    requires kernel;
    requires layout;
    requires io;

    opens com.bengkel to javafx.fxml;
    opens com.bengkel.database to javafx.fxml;
    opens com.bengkel.util to javafx.fxml;

    exports com.bengkel;
    exports com.bengkel.database;
    exports com.bengkel.util;
}