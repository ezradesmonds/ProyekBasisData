package com.katering.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.Interpolator;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.katering.database.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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

    @FXML
    public void initialize() {
        roleBox.getItems().addAll("Pelanggan", "Admin Cabang", "Admin Pusat");
        animateLoginCard();
    }

    @FXML
    public void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleBox.getValue();

        if (username.isEmpty() || password.isEmpty() || role == null) {
            showError("Harap isi semua field.");
            return;
        }

        try (Connection conn = DatabaseConnection.connect()) {
            String sql = """
                SELECT * FROM pengguna p 
                JOIN role_pengguna r ON p.id_role = r.id_role 
                WHERE p.username = ? AND p.password = ? AND r.nama_role = ?
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                showSuccess("Login berhasil!");
                navigateToDashboard(role);
            } else {
                showError("Username / password salah atau role tidak cocok.");
            }

        } catch (Exception e) {
            showError("Error koneksi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateToDashboard(String role) {
        String fxmlPath = switch (role) {
            case "Pelanggan" -> "/com/views/userDashboard.fxml";
            case "Admin Cabang" -> "/com/views/adminCabangDashboard.fxml";
            case "Admin Pusat" -> "/com/views/adminPusatDashboard.fxml";
            default -> null;
        };

        if (fxmlPath != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Scene scene = new Scene(loader.load());

                // Optional: Load CSS
                scene.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm());

                Stage stage = (Stage) loginCard.getScene().getWindow();
                stage.setScene(scene);
                stage.setTitle(role + " Dashboard");
                stage.show();
            } catch (IOException e) {
                showError("Gagal membuka halaman: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showError("Role tidak dikenali.");
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
        slide.setInterpolator(Interpolator.EASE_OUT);

        fade.play();
        slide.play();
    }
}
