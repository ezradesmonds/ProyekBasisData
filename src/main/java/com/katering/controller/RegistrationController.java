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

        // Di database Anda, tabel Pengguna tidak punya kolom alamat & telepon, jadi kita sesuaikan
        // Kita akan masukkan nama ke kolom 'nama'
        String sql = "INSERT INTO Pengguna (nama, username, password, id_role, id_cabang) VALUES (?, ?, ?, 1, CURRENT_DATE)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nama);
            stmt.setString(2, username);
            stmt.setString(3, password); // Ingat: simpan password sebagai plain text tidak aman!

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                messageLabel.setText("Registrasi berhasil! Silakan kembali ke halaman login.");
                messageLabel.setStyle("-fx-text-fill: green;");
                registerButton.setDisable(true); // Disable tombol setelah berhasil
            }

        } catch (SQLException e) {
            messageLabel.setText("Error: " + e.getMessage());
            messageLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
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