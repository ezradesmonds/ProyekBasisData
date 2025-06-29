package com.katering.controller;

import com.katering.database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class RegistrationController {

    @FXML
    private TextField namaField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField alamatField;
    @FXML
    private TextField teleponField;
    @FXML
    private Button registerButton;
    @FXML
    private Button backButton;
    @FXML
    private Label messageLabel;

    // Lokasi: src/main/java/com/katering/controller/RegistrationController.java

    @FXML
    private void handleRegisterButton() {
        String nama = namaField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String alamat = alamatField.getText();
        String telepon = teleponField.getText();

        if (nama.isEmpty() || username.isEmpty() || password.isEmpty() || alamat.isEmpty() || telepon.isEmpty()) {
            messageLabel.setText("Semua field harus diisi!");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // --- KODE YANG DIPERBAIKI ---
        // Kita hanya akan insert kolom yang kita punya nilainya.
        // id_role untuk 'Pelanggan' adalah 1.
        String sql = "INSERT INTO Pengguna (nama, username, password, id_role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set 4 parameter sesuai dengan jumlah '?'
            stmt.setString(1, nama);
            stmt.setString(2, username);
            stmt.setString(3, password);
            stmt.setInt(4, 1); // id_role = 1 untuk Pelanggan

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                messageLabel.setText("Registrasi berhasil! Silakan kembali ke halaman login.");
                messageLabel.setStyle("-fx-text-fill: green;");
                registerButton.setDisable(true); // Disable tombol setelah berhasil
            }

        } catch (SQLException e) {
            // Cek jika error karena username sudah ada
            if (e.getSQLState().equals("23505")) { // 23505 adalah kode error untuk unique violation
                messageLabel.setText("Error: Username '" + username + "' sudah digunakan. Pilih username lain.");
            } else {
                messageLabel.setText("Error: " + e.getMessage());
            }
            messageLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
        // --- AKHIR DARI KODE YANG DIPERBAIKI ---
    }

    @FXML
    private void handleBackButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/login.fxml"));
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}