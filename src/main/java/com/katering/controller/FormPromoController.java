package com.katering.controller;

import com.katering.database.DatabaseConnection;
import com.katering.model.Promo;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;

public class FormPromoController {

    @FXML private Label titleLabel;
    @FXML private TextField namaField;
    @FXML private TextArea deskripsiArea;
    @FXML private Spinner<Integer> diskonSpinner;
    @FXML private DatePicker mulaiPicker;
    @FXML private DatePicker selesaiPicker;
    @FXML private ComboBox<String> targetTypeComboBox;
    @FXML private Label specificTargetLabel;
    @FXML private ComboBox<String> specificTargetComboBox;
    @FXML private Label messageLabel;

    private Promo currentPromo;
    private final String SEMUA = "Semua";
    private final String CABANG = "Cabang Spesifik";
    private final String KATEGORI = "Kategori Spesifik";

    @FXML
    public void initialize() {
        targetTypeComboBox.getItems().addAll(SEMUA, CABANG, KATEGORI);
        specificTargetLabel.setVisible(false);
        specificTargetComboBox.setVisible(false);
    }

    public void setPromo(Promo promo) {
        this.currentPromo = promo;
        if (promo != null) {
            // Mode Edit
            titleLabel.setText("Edit Promo");
            namaField.setText(promo.getNamaPromo());
            deskripsiArea.setText(promo.getDeskripsi());
            diskonSpinner.getValueFactory().setValue(promo.getDiskon());
            mulaiPicker.setValue(promo.getTglMulai());
            selesaiPicker.setValue(promo.getTglSelesai());

            if (promo.getTargetCabangId() != null && promo.getTargetCabangId() > 0) {
                targetTypeComboBox.setValue(CABANG);
                loadCabangList();
                specificTargetComboBox.setValue(promo.getTarget()); // Disesuaikan
            } else if (promo.getTargetKategori() != null) {
                targetTypeComboBox.setValue(KATEGORI);
                loadKategoriList();
                specificTargetComboBox.setValue(promo.getTargetKategori());
            } else {
                targetTypeComboBox.setValue(SEMUA);
            }
        } else {
            // Mode Tambah
            titleLabel.setText("Tambah Promo Baru");
        }
    }

    @FXML
    private void handleTargetTypeChange() {
        String selected = targetTypeComboBox.getValue();
        if (SEMUA.equals(selected)) {
            specificTargetLabel.setVisible(false);
            specificTargetComboBox.setVisible(false);
        } else if (CABANG.equals(selected)) {
            specificTargetLabel.setText("Pilih Cabang:");
            specificTargetLabel.setVisible(true);
            specificTargetComboBox.setVisible(true);
            loadCabangList();
        } else if (KATEGORI.equals(selected)) {
            specificTargetLabel.setText("Pilih Kategori:");
            specificTargetLabel.setVisible(true);
            specificTargetComboBox.setVisible(true);
            loadKategoriList();
        }
    }

    private void loadCabangList() {
        specificTargetComboBox.getItems().clear();
        String sql = "SELECT nama_cabang FROM Cabang";
        try (Connection conn = DatabaseConnection.connect();
             ResultSet rs = conn.createStatement().executeQuery(sql)) {
            while (rs.next()) {
                specificTargetComboBox.getItems().add(rs.getString("nama_cabang"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadKategoriList() {
        specificTargetComboBox.getItems().clear();
        // Kategori bisa di-hardcode jika pilihannya tetap
        specificTargetComboBox.getItems().addAll("Makanan", "Minuman", "Paket");
    }

    @FXML
    private void handleSimpan() {
        // Validasi input
        if (namaField.getText().isEmpty() || mulaiPicker.getValue() == null || selesaiPicker.getValue() == null) {
            messageLabel.setText("Nama promo dan tanggal harus diisi.");
            return;
        }

        String sql;
        if (currentPromo == null) {
            // INSERT
            sql = "INSERT INTO Promo (nama_promo, deskripsi, diskon, tgl_mulai, tgl_selesai, target_cabang_id, target_kategori) VALUES (?, ?, ?, ?, ?, ?, ?)";
        } else {
            // UPDATE
            sql = "UPDATE Promo SET nama_promo=?, deskripsi=?, diskon=?, tgl_mulai=?, tgl_selesai=?, target_cabang_id=?, target_kategori=? WHERE id_promo=?";
        }

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, namaField.getText());
            stmt.setString(2, deskripsiArea.getText());
            stmt.setInt(3, diskonSpinner.getValue());
            stmt.setDate(4, Date.valueOf(mulaiPicker.getValue()));
            stmt.setDate(5, Date.valueOf(selesaiPicker.getValue()));

            Integer cabangId = null;
            String kategori = null;
            String targetType = targetTypeComboBox.getValue();

            if (CABANG.equals(targetType)) {
                String namaCabang = specificTargetComboBox.getValue();
                // Perlu query untuk dapatkan ID dari nama cabang
                cabangId = getCabangIdByName(namaCabang);
            } else if (KATEGORI.equals(targetType)) {
                kategori = specificTargetComboBox.getValue();
            }

            if (cabangId == null) stmt.setNull(6, Types.INTEGER); else stmt.setInt(6, cabangId);
            if (kategori == null) stmt.setNull(7, Types.VARCHAR); else stmt.setString(7, kategori);

            if (currentPromo != null) {
                stmt.setInt(8, currentPromo.getIdPromo()); // Untuk klausa WHERE di UPDATE
            }

            stmt.executeUpdate();
            handleBatal(); // Tutup window jika berhasil

        } catch (SQLException e) {
            messageLabel.setText("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to get branch ID
    private Integer getCabangIdByName(String name) throws SQLException {
        if (name == null) return null;
        String sql = "SELECT id_cabang FROM Cabang WHERE nama_cabang = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id_cabang");
            }
        }
        return null;
    }

    @FXML
    private void handleBatal() {
        Stage stage = (Stage) namaField.getScene().getWindow();
        stage.close();
    }
}