module com.bengkel {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.graphics;
    requires jdk.jsobject;

    // SQLite and database
    requires java.sql;
    requires org.xerial.sqlitejdbc;

    // Apache PDFBox
    requires org.apache.pdfbox;

    opens com.bengkel to javafx.fxml;
    opens com.bengkel.database to javafx.fxml;
    opens com.bengkel.util to javafx.fxml;

    exports com.bengkel;
    exports com.bengkel.database;
    exports com.bengkel.util;
}