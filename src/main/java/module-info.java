// Saya sarankan ganti nama modul agar sesuai dengan nama package utama Anda
module com.katering {
    // Kebutuhan untuk JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics; // <--- PERLU DITAMBAHKAN

    // Kebutuhan untuk Database
    requires java.sql;
    requires org.postgresql.jdbc; // <--- INI PALING PENTING UNTUK KONEKSI DATABASE

    // Buka package Model & Controller agar bisa diakses oleh JavaFX
    opens com.katering.model to javafx.base;
    opens com.katering.controller to javafx.fxml;

    // Export package utama
    exports com.katering;
}