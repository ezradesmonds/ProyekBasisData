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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
        loadKategori();

        cabangComboBox.setOnAction(e -> {
            if (cabangComboBox.getValue() != null) {
                loadMenu();
            }
        });

        kategoriComboBox.setOnAction(e -> {
            if (cabangComboBox.getValue() != null) {
                loadMenu();
            }
        });
    }

    private void loadCabang() {
        try (Connection conn = DatabaseConnection.connect();
             ResultSet rs = conn.createStatement().executeQuery("SELECT DISTINCT nama_cabang FROM cabang")) {
            while (rs.next()) {
                cabangComboBox.getItems().add(rs.getString("nama_cabang"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        cabangComboBox.setPromptText("Pilih Cabang");
    }

    private void loadKategori() {
        kategoriComboBox.getItems().addAll("Semua", "Makanan", "Minuman", "Paket");
        kategoriComboBox.getSelectionModel().selectFirst();
        kategoriComboBox.setPromptText("Pilih Kategori");
    }

    private void loadMenu() {
        menuList.clear();
        String cabang = cabangComboBox.getValue();
        
        String kategori = kategoriComboBox.getValue();

        if (cabang == null || cabang.isEmpty()) return;

        String sql = "SELECT m.* FROM menu m " +
                "JOIN cabang c ON m.id_cabang = c.id_cabang " +
                "WHERE c.nama_cabang = ?";
        if (kategori != null && !kategori.equals("Semua")) {
            sql += " AND m.kategori = ?";
        }

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int index = 1;
            stmt.setString(index++, cabang);
            if (kategori != null && !kategori.equals("Semua")) {
                stmt.setString(index, kategori);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Menu menu = new Menu(
                        rs.getInt("id_menu"),
                        rs.getString("nama_menu"),
                        rs.getString("deskripsi"),
                        rs.getInt("harga"),
                        rs.getBoolean("tersedia"),
                        rs.getInt("id_cabang")
                );
                menuList.add(menu);
            }

            menuTable.setItems(menuList);

            if (menuList.isEmpty()) {
                new Alert(AlertType.INFORMATION, "Tidak ada menu yang cocok dengan filter.").show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void filterMenu() {
        loadMenu();
    }

    @FXML
    private void tambahKeKeranjang() {
        Menu selected = menuTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(AlertType.WARNING, "Pilih menu terlebih dahulu!").show();
            return;
        }

        try (Connection conn = DatabaseConnection.connect()) {
            PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM keranjang WHERE id_pengguna = ? AND id_menu = ?");
            checkStmt.setInt(1, Session.getInstance().getIdPengguna());
            checkStmt.setInt(2, selected.getId());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                new Alert(AlertType.WARNING, "Menu sudah ada di keranjang!").show();
                return;
            }

            PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO keranjang (id_pengguna, id_menu, jumlah) VALUES (?, ?, ?)");
            insertStmt.setInt(1, Session.getInstance().getIdPengguna());
            insertStmt.setInt(2, selected.getId());
            insertStmt.setInt(3, 1);
            insertStmt.executeUpdate();

            new Alert(AlertType.INFORMATION, "Ditambahkan ke keranjang!").show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void lihatKeranjang() {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT k.jumlah, m.nama, m.harga FROM keranjang k JOIN menu m ON k.id_menu = m.id_menu WHERE k.id_pengguna = ?")) {
            stmt.setInt(1, Session.getInstance().getIdPengguna());
            ResultSet rs = stmt.executeQuery();

            StringBuilder sb = new StringBuilder("Keranjang:\n");
            int total = 0;

            while (rs.next()) {
                String nama = rs.getString("nama");
                int harga = rs.getInt("harga");
                int jumlah = rs.getInt("jumlah");
                total += harga * jumlah;
                sb.append(nama).append(" x").append(jumlah).append(" = ").append(harga * jumlah).append("\n");
            }

            sb.append("Total: ").append(total);
            new Alert(AlertType.INFORMATION, sb.toString()).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void lihatHistori() {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM pemesanan WHERE id_pengguna = ? ORDER BY tanggal DESC")) {
            stmt.setInt(1, Session.getInstance().getIdPengguna());
            ResultSet rs = stmt.executeQuery();

            StringBuilder sb = new StringBuilder("Histori Pemesanan:\n");
            while (rs.next()) {
                sb.append("ID: ").append(rs.getInt("id_pemesanan"))
                        .append(" - Status: ").append(rs.getString("status"))
                        .append(" - Tanggal: ").append(rs.getDate("tanggal"))
                        .append("\n");
            }

            new Alert(AlertType.INFORMATION, sb.toString()).show();

        } catch (Exception e) {
            e.printStackTrace();
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
        }

    }
}
