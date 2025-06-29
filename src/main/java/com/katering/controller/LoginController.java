package com.katering.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.Interpolator;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.katering.database.DatabaseConnection;
import com.katering.util.Session;

import java.io.IOException;
import java.net.URL; // Import URL
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> roleBox;

    @FXML
    private Label errorLabel;

    @FXML
    private AnchorPane loginCard;

//    @FXML
//    public void initialize() {
//        roleBox.getItems().addAll("Pelanggan", "Admin Cabang", "Admin Pusat");
//        animateLoginCard();
//    }


    @FXML
    private void handleRegisterLink() {
        try {
            // Muat FXML untuk jendela registrasi
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/registration.fxml"));

            // Dapatkan stage saat ini dari elemen apapun di scene login
            Stage stage = (Stage) loginCard.getScene().getWindow();

            // Buat scene baru dan tampilkan
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle("Registrasi Pelanggan Baru");

        } catch (IOException e) {
            e.printStackTrace();
            showError("Gagal membuka halaman registrasi.");
        }
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleBox.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showError("Harap isi semua kolom.");
            return;
        }

        try (Connection conn = DatabaseConnection.connect()) {
            String query = "SELECT id_pengguna, nama, id_role, id_cabang FROM Pengguna WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int idPengguna = rs.getInt("id_pengguna");
                        String namaPengguna = rs.getString("nama");
                        int idRole = rs.getInt("id_role");
                        Integer idCabang = rs.getObject("id_cabang", Integer.class);

                        String roleNama = getRoleName(idRole);

                        if (role.equals(roleNama)) {
                            Session.createSession(idPengguna, namaPengguna, roleNama, idCabang);
                            showSuccess("Login berhasil sebagai " + roleNama + "!");
                            redirectToDashboard(role);
                        } else {
                            showError("Role tidak cocok dengan akun ini.");
                        }
                    } else {
                        showError("Username atau password salah.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Kesalahan database: " + e.getMessage());
        }
    }

    private String getRoleName(int idRole) throws SQLException {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT nama_role FROM Role_pengguna WHERE id_role = ?")) {
            stmt.setInt(1, idRole);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nama_role");
                }
            }
        }
        return "Tidak Dikenal";
    }

    private void redirectToDashboard(String role) {
        String fxmlPath = "";
        if ("Pelanggan".equals(role)) {
            fxmlPath = "/com/views/userDashboard.fxml";
        } else if ("Admin Cabang".equals(role)) {
            fxmlPath = "/com/views/adminCabangDashboard.fxml";
        } else if ("Admin Pusat".equals(role)) {
            fxmlPath = "/com/views/adminPusatDashboard.fxml";
        } else {
            showError("Role tidak dikenali.");
            return;
        }

        try {
            // Dapatkan URL untuk file FXML
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                // Jika file FXML tidak ditemukan, tampilkan error dan kembali.
                showError("Error: File FXML tidak ditemukan di " + fxmlPath);
                System.err.println("DEBUG ERROR: File FXML tidak ditemukan: " + fxmlPath + ". Pastikan lokasi dan case-sensitivity sudah benar.");
                return;
            }

            // Inisialisasi FXMLLoader dengan URL file FXML
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load(); // Muat konten FXML ke dalam node Parent

            // Buat Scene baru dengan node Parent yang sudah dimuat
            Scene scene = new Scene(root);

            // Muat CSS (pastikan file CSS ada di jalur yang ditentukan)
            URL cssUrl = getClass().getResource("/style/style.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("DEBUG WARNING: File CSS tidak ditemukan di /style/style.css. Pastikan lokasi dan case-sensitivity sudah benar.");
            }

            Stage stage = (Stage) loginCard.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(role + " Dashboard");
            stage.show();
        } catch (IOException e) {
            showError("Gagal membuka halaman: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: green;");
    }

    private void animateLoginCard() {
        if (loginCard == null) return;

        FadeTransition fade = new FadeTransition(Duration.seconds(1), loginCard);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setInterpolator(Interpolator.EASE_BOTH);

        TranslateTransition slide = new TranslateTransition(Duration.seconds(1), loginCard);
        slide.setFromY(40);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_BOTH);

        fade.play();
        slide.play();
    }
}
