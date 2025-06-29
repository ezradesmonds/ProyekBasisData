package com.katering.controller;

import com.katering.database.DatabaseConnection;
import com.katering.model.Menu;
import com.katering.util.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserDashboardController {

    @FXML private ComboBox<String> cabangComboBox;
    @FXML private ComboBox<String> kategoriComboBox;
    @FXML private TableView<Menu> menuTable;
    @FXML private TableColumn<Menu, String> namaCol;
    @FXML private TableColumn<Menu, String> kategoriCol;
    @FXML private TableColumn<Menu, Integer> hargaCol;
    @FXML private TableColumn<Menu, Integer> stokCol;

    private ObservableList<Menu> menuList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        namaCol.setCellValueFactory(new PropertyValueFactory<>("nama"));
        kategoriCol.setCellValueFactory(new PropertyValueFactory<>("kategori"));
        hargaCol.setCellValueFactory(new PropertyValueFactory<>("harga"));
        stokCol.setCellValueFactory(new PropertyValueFactory<>("stok"));

        loadCabang();
        cabangComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadKategori(newVal);
                loadMenuByCabang(newVal);
            }
        });

        kategoriComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadMenuByKategori(cabangComboBox.getValue(), newVal);
            } else {
                loadMenuByCabang(cabangComboBox.getValue()); // Reload all menus for the branch if category is cleared
            }
        });
    }

    private void loadCabang() {
        ObservableList<String> cabangList = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT nama_cabang FROM Cabang");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                cabangList.add(rs.getString("nama_cabang"));
            }
            cabangComboBox.setItems(cabangList);
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Gagal memuat data cabang: " + e.getMessage()).show();
        }
    }

    private void loadKategori(String namaCabang) {
        ObservableList<String> kategoriList = FXCollections.observableArrayList();
        kategoriComboBox.getSelectionModel().clearSelection(); // Clear previous selection
        kategoriComboBox.setItems(null); // Clear previous items
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT DISTINCT m.kategori FROM Menu m JOIN Cabang c ON m.id_cabang = c.id_cabang WHERE c.nama_cabang = ?")) {
            stmt.setString(1, namaCabang);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    kategoriList.add(rs.getString("kategori"));
                }
                kategoriComboBox.setItems(kategoriList);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Gagal memuat data kategori: " + e.getMessage()).show();
        }
    }


    private void loadMenuByCabang(String namaCabang) {
        menuList.clear();
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT m.id_menu, m.nama_menu, m.deskripsi, m.harga, m.kategori, m.stok, m.tersedia, m.id_cabang " +
                             "FROM Menu m JOIN Cabang c ON m.id_cabang = c.id_cabang WHERE c.nama_cabang = ?")) {
            stmt.setString(1, namaCabang);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    menuList.add(new Menu(
                            rs.getInt("id_menu"),
                            rs.getString("nama_menu"),
                            rs.getString("deskripsi"),
                            rs.getInt("harga"),
                            rs.getString("kategori"),
                            rs.getInt("stok"),
                            rs.getBoolean("tersedia"),
                            rs.getInt("id_cabang")
                    ));
                }
                menuTable.setItems(menuList);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Gagal memuat menu: " + e.getMessage()).show();
        }
    }

    private void loadMenuByKategori(String namaCabang, String kategori) {
        menuList.clear();
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT m.id_menu, m.nama_menu, m.deskripsi, m.harga, m.kategori, m.stok, m.tersedia, m.id_cabang " +
                             "FROM Menu m JOIN Cabang c ON m.id_cabang = c.id_cabang WHERE c.nama_cabang = ? AND m.kategori = ?")) {
            stmt.setString(1, namaCabang);
            stmt.setString(2, kategori);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    menuList.add(new Menu(
                            rs.getInt("id_menu"),
                            rs.getString("nama_menu"),
                            rs.getString("deskripsi"),
                            rs.getInt("harga"),
                            rs.getString("kategori"),
                            rs.getInt("stok"),
                            rs.getBoolean("tersedia"),
                            rs.getInt("id_cabang")
                    ));
                }
                menuTable.setItems(menuList);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Gagal memuat menu berdasarkan kategori: " + e.getMessage()).show();
        }
    }

    @FXML
    private void tambahKeKeranjang() {
        Menu selectedMenu = menuTable.getSelectionModel().getSelectedItem();
        if (selectedMenu != null) {
            TextInputDialog dialog = new TextInputDialog("1");
            dialog.setTitle("Tambah ke Keranjang");
            dialog.setHeaderText("Masukkan jumlah untuk " + selectedMenu.getNama());
            dialog.setContentText("Jumlah:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(quantityStr -> {
                try {
                    int quantity = Integer.parseInt(quantityStr);
                    if (quantity > 0 && quantity <= selectedMenu.getStok()) {
                        new Alert(AlertType.INFORMATION, quantity + "x " + selectedMenu.getNama() + " ditambahkan ke keranjang.").show();
                    } else {
                        new Alert(AlertType.WARNING, "Jumlah tidak valid atau melebihi stok yang tersedia (" + selectedMenu.getStok() + ").").show();
                    }
                } catch (NumberFormatException e) {
                    new Alert(AlertType.ERROR, "Jumlah harus berupa angka.").show();
                }
            });
        } else {
            new Alert(AlertType.WARNING, "Pilih menu yang ingin ditambahkan ke keranjang.").show();
        }
    }


    @FXML
    private void lihatKeranjang() {
        new Alert(AlertType.INFORMATION, "Fitur Lihat Keranjang belum diimplementasikan.").show();
    }


    @FXML
    private void lihatRiwayatPemesanan() {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT p.id_pemesanan, sp.status, p.tgl_pesan " +
                             "FROM Pemesanan p JOIN Status_pesanan sp ON p.id_status = sp.id_status " +
                             "WHERE p.id_pengguna = ? ORDER BY p.tgl_pesan DESC")) {
            stmt.setInt(1, Session.getInstance().getIdPengguna());
            ResultSet rs = stmt.executeQuery();

            StringBuilder sb = new StringBuilder("Histori Pemesanan:\n");
            while (rs.next()) {
                sb.append("ID: ").append(rs.getInt("id_pemesanan"))
                        .append(" - Status: ").append(rs.getString("status"))
                        .append(" - Tanggal: ").append(rs.getDate("tgl_pesan"))
                        .append("\n");
            }

            new Alert(AlertType.INFORMATION, sb.toString()).show();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Gagal memuat riwayat pemesanan: " + e.getMessage()).show();
        }
    }

    @FXML
    private void daftarAkun() {
        new Alert(AlertType.INFORMATION, "Fitur Daftar Akun Baru masih dalam pengembangan.").show();
    }

    @FXML
    private void pantauStatus() {
        new Alert(AlertType.INFORMATION, "Fitur Pantau Status Pesanan belum tersedia.").show();
    }

    @FXML
    private void beriRating() {
        new Alert(AlertType.INFORMATION, "Fitur Beri Rating belum tersedia.").show();
    }

    @FXML
    private void handleLogout() {
        Session.getInstance().clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) menuTable.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Gagal logout: " + e.getMessage()).show();
        }
    }
}