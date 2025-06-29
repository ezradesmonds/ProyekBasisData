package com.katering.controller;

import com.katering.database.DatabaseConnection;
import com.katering.model.Promo; // Anda mungkin perlu membuat/memodifikasi model ini
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class KelolaPromoController {

    @FXML
    private TableView<Promo> promoTable;
    @FXML
    private TableColumn<Promo, Integer> idColumn;
    @FXML
    private TableColumn<Promo, String> namaColumn;
    @FXML
    private TableColumn<Promo, Integer> diskonColumn;
    @FXML
    private TableColumn<Promo, LocalDate> mulaiColumn;
    @FXML
    private TableColumn<Promo, LocalDate> selesaiColumn;
    @FXML
    private TableColumn<Promo, String> targetColumn;

    private ObservableList<Promo> promoList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idPromo"));
        namaColumn.setCellValueFactory(new PropertyValueFactory<>("namaPromo"));
        diskonColumn.setCellValueFactory(new PropertyValueFactory<>("diskon"));
        mulaiColumn.setCellValueFactory(new PropertyValueFactory<>("tglMulai"));
        selesaiColumn.setCellValueFactory(new PropertyValueFactory<>("tglSelesai"));
        // 'target' adalah properti buatan di model Promo
        targetColumn.setCellValueFactory(new PropertyValueFactory<>("target"));

        loadPromoData();
    }

    private void loadPromoData() {
        promoList.clear();
        String sql = "SELECT p.*, c.nama_cabang FROM Promo p LEFT JOIN Cabang c ON p.target_cabang_id = c.id_cabang";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                promoList.add(new Promo(
                        rs.getInt("id_promo"),
                        rs.getString("nama_promo"),
                        rs.getString("deskripsi"),
                        rs.getInt("diskon"),
                        rs.getDate("tgl_mulai").toLocalDate(),
                        rs.getDate("tgl_selesai").toLocalDate(),
                        rs.getInt("target_cabang_id"),
                        rs.getString("target_kategori"),
                        rs.getString("nama_cabang") // Untuk menampilkan nama cabang di tabel
                ));
            }
            promoTable.setItems(promoList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showPromoForm(Promo promo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/FormPromo.fxml"));
            Stage stage = new Stage();
            stage.setTitle(promo == null ? "Tambah Promo Baru" : "Edit Promo");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setScene(new Scene(loader.load()));

            FormPromoController controller = loader.getController();
            controller.setPromo(promo);

            stage.showAndWait();
            loadPromoData(); // Refresh tabel setelah form ditutup

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTambah() {
        showPromoForm(null); // Kirim null untuk mode 'Tambah'
    }

    @FXML
    private void handleEdit() {
        Promo selectedPromo = promoTable.getSelectionModel().getSelectedItem();
        if (selectedPromo == null) {
            new Alert(Alert.AlertType.WARNING, "Pilih promo yang ingin diedit terlebih dahulu.").show();
            return;
        }
        showPromoForm(selectedPromo); // Kirim promo terpilih untuk mode 'Edit'
    }

    @FXML
    private void handleHapus() {
        Promo selectedPromo = promoTable.getSelectionModel().getSelectedItem();
        if (selectedPromo == null) {
            new Alert(Alert.AlertType.WARNING, "Pilih promo yang ingin dihapus terlebih dahulu.").show();
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Apakah Anda yakin ingin menghapus promo: " + selectedPromo.getNamaPromo() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                String sql = "DELETE FROM Promo WHERE id_promo = ?";
                try (Connection conn = DatabaseConnection.connect();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, selectedPromo.getIdPromo());
                    stmt.executeUpdate();
                    loadPromoData(); // Refresh
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}