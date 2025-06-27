package com.katering.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.*;
import com.katering.database.DatabaseConnection;
import javafx.stage.Stage;

public class AdminPusatController {
    @FXML private TableView<Performance> performTable;
    @FXML private TableColumn<Performance, String> cabangCol;
    @FXML private TableColumn<Performance, Integer> jumlahCol;
    @FXML private TableColumn<Performance, Integer> pendapatanCol;

    private ObservableList<Performance> performList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cabangCol.setCellValueFactory(cell -> cell.getValue().cabangProperty());
        jumlahCol.setCellValueFactory(cell -> cell.getValue().jumlahProperty().asObject());
        pendapatanCol.setCellValueFactory(cell -> cell.getValue().pendapatanProperty().asObject());
        loadData();
    }

    private void loadData() {
        performList.clear();
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT cabang, COUNT(*) as jumlah, SUM(m.harga * d.jumlah) as total " +
                     "FROM pemesanan p JOIN detail_pemesanan d ON p.id_pemesanan = d.id_pemesanan " +
                     "JOIN menu m ON d.id_menu = m.id_menu GROUP BY cabang")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                performList.add(new Performance(
                        rs.getString("cabang"),
                        rs.getInt("jumlah"),
                        rs.getInt("total")
                ));
            }
            performTable.setItems(performList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class Performance {
        private final javafx.beans.property.StringProperty cabang;
        private final javafx.beans.property.IntegerProperty jumlah;
        private final javafx.beans.property.IntegerProperty pendapatan;

        public Performance(String cabang, int jumlah, int pendapatan) {
            this.cabang = new javafx.beans.property.SimpleStringProperty(cabang);
            this.jumlah = new javafx.beans.property.SimpleIntegerProperty(jumlah);
            this.pendapatan = new javafx.beans.property.SimpleIntegerProperty(pendapatan);
        }

        public javafx.beans.property.StringProperty cabangProperty() { return cabang; }
        public javafx.beans.property.IntegerProperty jumlahProperty() { return jumlah; }
        public javafx.beans.property.IntegerProperty pendapatanProperty() { return pendapatan; }
    }
    @FXML
    private void handleLogout() {
        // Misalnya kembali ke halaman login
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) performTable.getScene().getWindow(); // Ambil window saat ini
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void kelolaDiskon() {
        // Contoh implementasi
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Kelola Diskon");
        alert.setHeaderText(null);
        alert.setContentText("Fitur kelola diskon belum diimplementasikan.");
        alert.showAndWait();
    }

    @FXML
    private void tambahPromo() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Masukkan nama promo dan persentase (contoh: Diskon15,15)");
        dialog.showAndWait().ifPresent(input -> {
            String[] parts = input.split(",");
            if (parts.length == 2) {
                try (Connection conn = DatabaseConnection.connect();
                     PreparedStatement stmt = conn.prepareStatement("INSERT INTO promo(nama, diskon) VALUES (?, ?);")) {
                    stmt.setString(1, parts[0]);
                    stmt.setInt(2, Integer.parseInt(parts[1]));
                    stmt.executeUpdate();
                    new Alert(Alert.AlertType.INFORMATION, "Promo ditambahkan!").show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
