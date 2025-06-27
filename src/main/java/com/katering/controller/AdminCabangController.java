package com.katering.controller;

import com.katering.database.DatabaseConnection;
import com.katering.model.Pesanan;
import com.katering.util.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;

import java.sql.*;

public class AdminCabangController {

    @FXML private TableView<Pesanan> pesananTable;
    @FXML private TableColumn<Pesanan, Integer> idCol;
    @FXML private TableColumn<Pesanan, String> namaCol;
    @FXML private TableColumn<Pesanan, String> statusCol;

    private ObservableList<Pesanan> pesananList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getId()).asObject());
        namaCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaPelanggan()));
        statusCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getStatus()));
        loadPesanan();
    }

    private void loadPesanan() {
        pesananList.clear();
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT p.id_pemesanan, u.nama_lengkap, p.status FROM pemesanan p JOIN pengguna u ON p.id_pengguna = u.id_pengguna WHERE p.status = 'Proses'")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                pesananList.add(new Pesanan(
                        rs.getInt("id_pemesanan"),
                        rs.getString("nama_lengkap"),
                        rs.getString("status")
                ));
            }
            pesananTable.setItems(pesananList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void prosesPesanan() {
        Pesanan selected = pesananTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement("UPDATE pemesanan SET status = 'Selesai' WHERE id_pemesanan = ?")) {
                stmt.setInt(1, selected.getId());
                stmt.executeUpdate();
                loadPesanan();
                new Alert(AlertType.INFORMATION, "Pesanan telah diselesaikan.").show();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            new Alert(AlertType.WARNING, "Pilih pesanan terlebih dahulu!").show();
        }
    }

    @FXML
    private void tambahMenu() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Masukkan detail menu: nama,kategori,harga,stok");
        dialog.showAndWait().ifPresent(input -> {
            String[] parts = input.split(",");
            if (parts.length == 4) {
                try (Connection conn = DatabaseConnection.connect();
                     PreparedStatement stmt = conn.prepareStatement("INSERT INTO menu(nama,kategori,harga,stok,id_cabang) VALUES (?,?,?,?,?)")) {
                    stmt.setString(1, parts[0]);
                    stmt.setString(2, parts[1]);
                    stmt.setInt(3, Integer.parseInt(parts[2]));
                    stmt.setInt(4, Integer.parseInt(parts[3]));
                    stmt.setInt(5, Session.getInstance().getIdCabang());
                    stmt.executeUpdate();
                    new Alert(AlertType.INFORMATION, "Menu ditambahkan!").show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void editMenu() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Masukkan ID Menu yang ingin diedit dan data baru (nama,kategori,harga,stok)");
        dialog.setContentText("Format: id,nama,kategori,harga,stok");
        dialog.showAndWait().ifPresent(input -> {
            String[] parts = input.split(",");
            if (parts.length == 5) {
                try (Connection conn = DatabaseConnection.connect();
                     PreparedStatement stmt = conn.prepareStatement("UPDATE menu SET nama=?, kategori=?, harga=?, stok=? WHERE id_menu=? AND id_cabang=?")) {
                    stmt.setString(1, parts[1]);
                    stmt.setString(2, parts[2]);
                    stmt.setInt(3, Integer.parseInt(parts[3]));
                    stmt.setInt(4, Integer.parseInt(parts[4]));
                    stmt.setInt(5, Integer.parseInt(parts[0]));
                    stmt.setInt(6, Session.getInstance().getIdCabang());
                    int updated = stmt.executeUpdate();
                    if (updated > 0) {
                        new Alert(AlertType.INFORMATION, "Menu berhasil diedit!").show();
                    } else {
                        new Alert(AlertType.WARNING, "Gagal mengedit. Menu tidak ditemukan.").show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void hapusMenu() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Masukkan ID Menu yang ingin dihapus");
        dialog.showAndWait().ifPresent(idStr -> {
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM menu WHERE id_menu = ? AND id_cabang = ?")) {
                stmt.setInt(1, Integer.parseInt(idStr));
                stmt.setInt(2, Session.getInstance().getIdCabang());
                int deleted = stmt.executeUpdate();
                if (deleted > 0) {
                    new Alert(AlertType.INFORMATION, "Menu berhasil dihapus!").show();
                } else {
                    new Alert(AlertType.WARNING, "Menu tidak ditemukan atau bukan milik cabang ini.").show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void jadwalPengiriman() {
        new Alert(AlertType.INFORMATION, "Fitur penjadwalan pengiriman belum diimplementasikan.").show();
    }

    @FXML
    private void handleLogout() {
        Session.getInstance().clear(); // logout session
        new Alert(AlertType.INFORMATION, "Anda telah logout.").show();
        // TODO: Redirect ke login screen jika ingin


    }
}
