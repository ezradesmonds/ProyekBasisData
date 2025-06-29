package com.katering.controller;

import com.katering.database.DatabaseConnection;
import com.katering.util.Session;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        // Query untuk mengambil performa, menjumlahkan total_harga dari tabel Pemesanan
        String sql = "SELECT c.nama_cabang, COUNT(p.id_pemesanan) as jumlah_pesanan, COALESCE(SUM(p.total_harga), 0) as total_pendapatan " +
                "FROM Cabang c " +
                "LEFT JOIN Pemesanan p ON c.id_cabang = p.id_cabang " +
                "GROUP BY c.nama_cabang ORDER BY c.nama_cabang";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                performList.add(new Performance(
                        rs.getString("nama_cabang"),
                        rs.getInt("jumlah_pesanan"),
                        rs.getInt("total_pendapatan")
                ));
            }
            performTable.setItems(performList);
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Gagal memuat data performa: " + e.getMessage()).show();
        }
    }

    public static class Performance {
        private final StringProperty cabang;
        private final IntegerProperty jumlah;
        private final IntegerProperty pendapatan;

        public Performance(String cabang, int jumlah, int pendapatan) {
            this.cabang = new SimpleStringProperty(cabang);
            this.jumlah = new SimpleIntegerProperty(jumlah);
            this.pendapatan = new SimpleIntegerProperty(pendapatan);
        }

        public StringProperty cabangProperty() { return cabang; }
        public IntegerProperty jumlahProperty() { return jumlah; }
        public IntegerProperty pendapatanProperty() { return pendapatan; }
    }

    @FXML
    private void lihatDaftarPengguna() {
        // Method ini tidak diubah, bisa dibiarkan
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT p.nama, p.username, rp.nama_role, c.nama_cabang " +
                             "FROM Pengguna p " +
                             "JOIN Role_pengguna rp ON p.id_role = rp.id_role " +
                             "LEFT JOIN Cabang c ON p.id_cabang = c.id_cabang " +
                             "ORDER BY rp.nama_role, p.nama")) {
            ResultSet rs = stmt.executeQuery();

            StringBuilder sb = new StringBuilder("Daftar Pengguna:\n");
            sb.append(String.format("%-20s %-15s %-15s %-15s\n", "Nama", "Username", "Role", "Cabang"));
            sb.append("------------------------------------------------------------------\n");

            while (rs.next()) {
                String nama = rs.getString("nama");
                String username = rs.getString("username");
                String role = rs.getString("nama_role");
                String cabang = rs.getString("nama_cabang") != null ? rs.getString("nama_cabang") : "-";
                sb.append(String.format("%-20s %-15s %-15s %-15s\n", nama, username, role, cabang));
            }

            TextArea textArea = new TextArea(sb.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Daftar Pengguna");
            alert.setHeaderText(null);
            alert.getDialogPane().setContent(textArea);
            alert.setResizable(true);
            alert.showAndWait();

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Gagal memuat daftar pengguna: " + e.getMessage()).show();
        }
    }

    @FXML
    private void lihatDaftarCabang() {
        // Method ini tidak diubah, bisa dibiarkan
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT nama_cabang, alamat, kota FROM Cabang");
             ResultSet rs = stmt.executeQuery()) {
            StringBuilder sb = new StringBuilder("Daftar Cabang:\n");
            while (rs.next()) {
                sb.append("Nama: ").append(rs.getString("nama_cabang"))
                        .append(", Alamat: ").append(rs.getString("alamat"))
                        .append(", Kota: ").append(rs.getString("kota"))
                        .append("\n");
            }
            new Alert(Alert.AlertType.INFORMATION, sb.toString()).show();
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Gagal memuat daftar cabang: " + e.getMessage()).show();
        }
    }

    // --- METHOD LAMA (tambahPromo dan kelolaPromo) SUDAH DIHAPUS ---

    @FXML
    private void kelolaPromo() { // Ini adalah method baru yang menggantikan semua logika promo lama
        try {
            // Kode ini akan membuka jendela manajemen promo yang baru
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/KelolaPromo.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Manajemen Promo");
            stage.setScene(new Scene(loader.load()));
            stage.showAndWait(); // Menggunakan showAndWait agar jendela utama tidak bisa di-klik

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Gagal membuka jendela manajemen promo.").show();
        }
    }


    @FXML
    private void handleLogout() {
        Session.getInstance().clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) performTable.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Gagal logout: " + e.getMessage()).show();
        }
    }
}