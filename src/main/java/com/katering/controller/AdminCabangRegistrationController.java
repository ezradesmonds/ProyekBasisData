package com.katering.controller;

import com.katering.database.DatabaseConnection;
import com.katering.model.Cabang; // Anda mungkin perlu membuat model Cabang jika belum ada
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminCabangRegistrationController {

    @FXML private TextField namaField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<Cabang> cabangComboBox;
    @FXML private Label messageLabel;
    @FXML private Button registerButton;

    @FXML
    public void initialize() {
        loadCabangData();
    }

    private void loadCabangData() {
        ObservableList<Cabang> cabangList = FXCollections.observableArrayList();
        String sql = "SELECT id_cabang, nama_cabang, alamat, kota FROM Cabang";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                cabangList.add(new Cabang(
                        rs.getInt("id_cabang"),
                        rs.getString("nama_cabang"),
                        rs.getString("alamat"),
                        rs.getString("kota")
                ));
            }
            cabangComboBox.setItems(cabangList);

            // Tampilkan nama cabang di ComboBox, tapi simpan seluruh objek Cabang
            cabangComboBox.setConverter(new StringConverter<>() {
                @Override
                public String toString(Cabang cabang) {
                    return cabang == null ? null : cabang.getNamaCabang();
                }

                @Override
                public Cabang fromString(String string) {
                    return null; // Tidak perlu implementasi
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            messageLabel.setText("Gagal memuat data cabang.");
        }
    }

    @FXML
    private void handleRegisterButton() {
        String nama = namaField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        Cabang selectedCabang = cabangComboBox.getSelectionModel().getSelectedItem();

        if (nama.isEmpty() || username.isEmpty() || password.isEmpty() || selectedCabang == null) {
            messageLabel.setText("Semua field harus diisi!");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        // id_role = 2 untuk Admin Cabang
        String sql = "INSERT INTO Pengguna (nama, username, password, id_role, id_cabang) VALUES (?, ?, ?, 2, ?)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nama);
            stmt.setString(2, username);
            stmt.setString(3, password);
            stmt.setInt(4, selectedCabang.getIdCabang());

            stmt.executeUpdate();
            messageLabel.setText("Admin Cabang berhasil diregistrasi!");
            messageLabel.setStyle("-fx-text-fill: green;");
            registerButton.setDisable(true); // Disable tombol setelah berhasil

        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { // Cek error untuk username duplikat
                messageLabel.setText("Error: Username '" + username + "' sudah digunakan.");
            } else {
                messageLabel.setText("Error: " + e.getMessage());
            }
            messageLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackButton() {
        Stage stage = (Stage) registerButton.getScene().getWindow();
        stage.close();
    }
}