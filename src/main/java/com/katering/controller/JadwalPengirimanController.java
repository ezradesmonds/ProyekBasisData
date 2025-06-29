package com.katering.controller;

import com.katering.database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JadwalPengirimanController {

    @FXML
    private ComboBox<Integer> pemesananComboBox;

    @FXML
    private ComboBox<String> stafComboBox;

    @FXML
    private DatePicker tanggalPicker;

    @FXML
    private TextField waktuField;

    @FXML
    private TextField alamatField;

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        loadPemesananIds();
        loadStaf();
    }

    private void loadPemesananIds() {
        // Memuat ID pemesanan yang statusnya 'Processed' atau siap dikirim
        String sql = "SELECT p.id_pemesanan FROM Pemesanan p JOIN Status_pesanan s ON p.id_status = s.id_status WHERE s.status = 'Processed'";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                pemesananComboBox.getItems().add(rs.getInt("id_pemesanan"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadStaf() {
        // Memuat semua staf
        String sql = "SELECT nama_staf FROM Staf";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                stafComboBox.getItems().add(rs.getString("nama_staf"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleSubmitButton() {
        if (pemesananComboBox.getValue() == null || stafComboBox.getValue() == null || tanggalPicker.getValue() == null || waktuField.getText().isEmpty() || alamatField.getText().isEmpty()) {
            messageLabel.setText("Semua field harus diisi!");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        Integer idPemesanan = pemesananComboBox.getValue();
        String namaStaf = stafComboBox.getValue();
        String dateTimeString = tanggalPicker.getValue().toString() + " " + waktuField.getText();

        String sql = "INSERT INTO Jadwal_pengiriman (id_pengiriman, id_staf, waktu_kirim, alamat_pengiriman) VALUES (?, (SELECT id_staf FROM Staf WHERE nama_staf = ?), ?, ?)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPemesanan);
            stmt.setString(2, namaStaf);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            stmt.setString(4, alamatField.getText());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                messageLabel.setText("Jadwal pengiriman berhasil ditambahkan!");
                messageLabel.setStyle("-fx-text-fill: green;");

                // Update status pesanan menjadi 'Shipped'
                updateStatusPemesanan(idPemesanan);
            }

        } catch (SQLException e) {
            messageLabel.setText("Error: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    private void updateStatusPemesanan(int idPemesanan) {
        String sql = "UPDATE Pemesanan SET id_status = (SELECT id_status FROM Status_pesanan WHERE status = 'Shipped') WHERE id_pemesanan = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPemesanan);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}