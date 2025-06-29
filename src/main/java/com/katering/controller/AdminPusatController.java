package com.katering.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;
import java.sql.*;
import com.katering.database.DatabaseConnection;
import com.katering.model.Promo; // Make sure this is the updated Promo model
import com.katering.util.Session;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import java.time.LocalDate;

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
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT c.nama_cabang, COUNT(p.id_pemesanan) as jumlah_pesanan, SUM(dp.subtotal) as total_pendapatan " +
                             "FROM Cabang c " +
                             "LEFT JOIN Pemesanan p ON c.id_cabang = p.id_cabang " +
                             "LEFT JOIN Detil_pesanan dp ON p.id_pemesanan = dp.id_pemesanan " +
                             "GROUP BY c.nama_cabang ORDER BY c.nama_cabang")) {
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
        private final javafx.beans.property.StringProperty cabang;
        private final javafx.beans.property.IntegerProperty jumlah;
        private final javafx.beans.property.IntegerProperty pendapatan;

        public Performance(String cabang, int jumlah, int pendapatan) {
            this.cabang = new javafx.beans.property.SimpleStringProperty(cabang);
            this.jumlah = new javafx.beans.property.SimpleIntegerProperty(jumlah);
            this.pendapatan = new javafx.beans.property.SimpleIntegerProperty(pendapatan);
        }

        public javafx.beans.property.StringProperty cabangProperty() { return cabang; }
        public javafx.beans.property.IntegerProperty jumlahProperty() { return jumlah; }
        public javafx.beans.property.IntegerProperty pendapatanProperty() { return pendapatan; }
    }


    @FXML
    private void lihatDaftarPengguna() {
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

    @FXML
    private void kelolaPromo() {
        new Alert(Alert.AlertType.INFORMATION, "Fitur kelola promo belum diimplementasikan sepenuhnya. Mengarahkan ke tampilan daftar promo.").show();
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT id_promo, nama_promo, deskripsi, diskon, tgl_mulai, tgl_selesai FROM Promo ORDER BY tgl_mulai DESC");
             ResultSet rs = stmt.executeQuery()) {

            StringBuilder sb = new StringBuilder("Daftar Promo:\n");
            sb.append(String.format("%-5s %-25s %-40s %-10s %-12s %-12s\n", "ID", "Nama Promo", "Deskripsi", "Diskon", "Mulai", "Selesai"));
            sb.append("---------------------------------------------------------------------------------------------------------------\n");

            while (rs.next()) {
                int id = rs.getInt("id_promo");
                String nama = rs.getString("nama_promo");
                String deskripsi = rs.getString("deskripsi");
                int diskon = rs.getInt("diskon");
                LocalDate tglMulai = rs.getDate("tgl_mulai").toLocalDate();
                LocalDate tglSelesai = rs.getDate("tgl_selesai").toLocalDate();
                sb.append(String.format("%-5d %-25s %-40s %-10d %-12s %-12s\n", id, nama, deskripsi != null ? deskripsi : "-", diskon, tglMulai.toString(), tglSelesai.toString()));
            }

            TextArea textArea = new TextArea(sb.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefHeight(400);
            textArea.setPrefWidth(700);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Daftar Promo");
            alert.setHeaderText("Berikut adalah daftar promo yang tersedia:");
            alert.getDialogPane().setContent(textArea);
            alert.setResizable(true);
            alert.showAndWait();

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Gagal memuat daftar promo: " + e.getMessage()).show();
        }
    }


    @FXML
    private void tambahPromo() {
        Dialog<Promo> dialog = new Dialog<>();
        dialog.setTitle("Tambah Promo Baru");
        dialog.setHeaderText("Masukkan detail promo baru");

        ButtonType tambahButtonType = new ButtonType("Tambah", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(tambahButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField namaPromo = new TextField();
        namaPromo.setPromptText("Nama Promo");
        TextArea deskripsi = new TextArea();
        deskripsi.setPromptText("Deskripsi");
        deskripsi.setWrapText(true);
        TextField diskon = new TextField();
        diskon.setPromptText("Diskon (%)");
        DatePicker tglMulai = new DatePicker();
        tglMulai.setPromptText("Tanggal Mulai");
        DatePicker tglSelesai = new DatePicker();
        tglSelesai.setPromptText("Tanggal Selesai");

        grid.add(new Label("Nama Promo:"), 0, 0);
        grid.add(namaPromo, 1, 0);
        grid.add(new Label("Deskripsi:"), 0, 1);
        grid.add(deskripsi, 1, 1);
        grid.add(new Label("Diskon (%):"), 0, 2);
        grid.add(diskon, 1, 2);
        grid.add(new Label("Tanggal Mulai:"), 0, 3);
        grid.add(tglMulai, 1, 3);
        grid.add(new Label("Tanggal Selesai:"), 0, 4);
        grid.add(tglSelesai, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == tambahButtonType) {
                try {
                    return new Promo(
                            0,
                            namaPromo.getText(),
                            deskripsi.getText(),
                            Integer.parseInt(diskon.getText()),
                            tglMulai.getValue(),
                            tglSelesai.getValue()
                    );
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "Diskon harus berupa angka.").show();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newPromo -> {
            if (newPromo.getTglMulai() == null || newPromo.getTglSelesai() == null) {
                new Alert(Alert.AlertType.ERROR, "Tanggal mulai dan tanggal selesai harus diisi.").show();
                return;
            }
            if (newPromo.getTglMulai().isAfter(newPromo.getTglSelesai())) {
                new Alert(Alert.AlertType.ERROR, "Tanggal mulai tidak boleh setelah tanggal selesai.").show();
                return;
            }

            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO Promo(nama_promo, deskripsi, diskon, tgl_mulai, tgl_selesai) VALUES (?, ?, ?, ?, ?)")) {
                stmt.setString(1, newPromo.getNamaPromo());
                stmt.setString(2, newPromo.getDeskripsi());
                stmt.setInt(3, newPromo.getDiskon());
                stmt.setDate(4, Date.valueOf(newPromo.getTglMulai()));
                stmt.setDate(5, Date.valueOf(newPromo.getTglSelesai()));
                stmt.executeUpdate();
                new Alert(Alert.AlertType.INFORMATION, "Promo berhasil ditambahkan!").show();
            } catch (SQLException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Gagal menambahkan promo: " + e.getMessage()).show();
            }
        });
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
        }
        catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Gagal logout: " + e.getMessage()).show();
        }
    }
}