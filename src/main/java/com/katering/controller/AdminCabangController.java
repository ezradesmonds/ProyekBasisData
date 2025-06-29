package com.katering.controller;

import com.katering.database.DatabaseConnection;
import com.katering.model.Menu;
import com.katering.model.Pesanan;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Optional;

public class AdminCabangController {

    // --- Variabel untuk Tabel Pesanan ---
    @FXML private TableView<Pesanan> pesananTable;
    @FXML private TableColumn<Pesanan, Integer> idCol;
    @FXML private TableColumn<Pesanan, String> namaCol;
    @FXML private TableColumn<Pesanan, String> statusCol;

    // --- Variabel untuk Tabel Menu (YANG BARU DITAMBAHKAN) ---
    @FXML private TableView<Menu> menuTable;
    @FXML private TableColumn<Menu, Integer> idMenuCol;
    @FXML private TableColumn<Menu, String> namaMenuCol;
    @FXML private TableColumn<Menu, String> kategoriMenuCol;
    @FXML private TableColumn<Menu, Integer> hargaMenuCol;
    @FXML private TableColumn<Menu, Integer> stokMenuCol;
    @FXML private TableColumn<Menu, Boolean> tersediaMenuCol;

    @FXML private Label cabangTitleLabel;

    private final ObservableList<Pesanan> pesananList = FXCollections.observableArrayList();
    private final ObservableList<Menu> menuList = FXCollections.observableArrayList(); // <-- Deklarasi yang hilang

    @FXML
    public void initialize() {
        // Mengatur judul dashboard
        if (Session.getInstance() != null) {
            cabangTitleLabel.setText("Dashboard Admin - " + getNamaCabangById(Session.getInstance().getIdCabang()));
        }

        // Setup untuk tabel pesanan
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        namaCol.setCellValueFactory(new PropertyValueFactory<>("namaPelanggan"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Setup untuk tabel menu (YANG BARU DITAMBAHKAN)
        idMenuCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        namaMenuCol.setCellValueFactory(new PropertyValueFactory<>("nama"));
        kategoriMenuCol.setCellValueFactory(new PropertyValueFactory<>("kategori"));
        hargaMenuCol.setCellValueFactory(new PropertyValueFactory<>("harga"));
        stokMenuCol.setCellValueFactory(new PropertyValueFactory<>("stok"));
        tersediaMenuCol.setCellValueFactory(new PropertyValueFactory<>("tersedia"));

        // Memuat data saat inisialisasi
        loadPesanan();
        loadMenu();
    }

    // --- METHOD UNTUK DATA ---

    private void loadPesanan() {
        pesananList.clear();
        String sql = "SELECT p.id_pemesanan, u.nama, s.status FROM Pemesanan p JOIN Pengguna u ON p.id_pengguna = u.id_pengguna JOIN Status_pesanan s ON p.id_status = s.id_status WHERE p.id_cabang = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Session.getInstance().getIdCabang());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                pesananList.add(new Pesanan(
                        rs.getInt("id_pemesanan"),
                        rs.getString("nama"),
                        rs.getString("status")
                ));
            }
            pesananTable.setItems(pesananList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMenu() {
        menuList.clear();
        String sql = "SELECT id_menu, nama_menu, deskripsi, harga, kategori, stok, tersedia, id_cabang FROM Menu WHERE id_cabang = ? ORDER BY nama_menu";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Session.getInstance().getIdCabang());
            ResultSet rs = stmt.executeQuery();
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
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Gagal memuat menu: " + e.getMessage()).show();
        }
    }

    // --- METHOD UNTUK TOMBOL AKSI ---

    @FXML
    private void prosesPesanan() {
        Pesanan selected = pesananTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            String sql = "UPDATE Pemesanan SET id_status = (SELECT id_status FROM Status_pesanan WHERE status = 'Processed') WHERE id_pemesanan = ?";
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, selected.getId());
                stmt.executeUpdate();
                loadPesanan();
                new Alert(AlertType.INFORMATION, "Pesanan #" + selected.getId() + " telah diproses dan statusnya menjadi 'Processed'.").show();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            new Alert(AlertType.WARNING, "Pilih pesanan yang ingin diproses.").show();
        }
    }

    @FXML
    private void updateStatusPesanan() {
        Pesanan selectedPesanan = pesananTable.getSelectionModel().getSelectedItem();
        if (selectedPesanan != null) {
            ChoiceDialog<String> dialog = new ChoiceDialog<>("Processed", "Pending", "Processed", "Shipped", "Delivered", "Cancelled");
            dialog.setTitle("Update Status Pesanan");
            dialog.setHeaderText("Pilih status baru untuk pesanan ID: " + selectedPesanan.getId());
            dialog.setContentText("Status:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(newStatus -> {
                String sql = "UPDATE Pemesanan SET id_status = (SELECT id_status FROM Status_pesanan WHERE status = ?) WHERE id_pemesanan = ?";
                try (Connection conn = DatabaseConnection.connect();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, newStatus);
                    stmt.setInt(2, selectedPesanan.getId());
                    if (stmt.executeUpdate() > 0) {
                        new Alert(AlertType.INFORMATION, "Status pesanan berhasil diperbarui!").show();
                        loadPesanan();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        } else {
            new Alert(AlertType.WARNING, "Pilih pesanan yang ingin diperbarui statusnya.").show();
        }
    }

    @FXML
    private void tambahMenu() {
        Dialog<Menu> dialog = new Dialog<>();
        dialog.setTitle("Tambah Menu Baru");
        ButtonType tambahButtonType = new ButtonType("Tambah", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(tambahButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        TextField namaMenu = new TextField();
        namaMenu.setPromptText("Nama Menu");
        TextArea deskripsi = new TextArea();
        deskripsi.setPromptText("Deskripsi");
        TextField harga = new TextField();
        harga.setPromptText("Harga");
        TextField kategori = new TextField();
        kategori.setPromptText("Kategori");
        TextField stok = new TextField();
        stok.setPromptText("Stok");
        grid.add(new Label("Nama Menu:"), 0, 0); grid.add(namaMenu, 1, 0);
        grid.add(new Label("Deskripsi:"), 0, 1); grid.add(deskripsi, 1, 1);
        grid.add(new Label("Harga:"), 0, 2); grid.add(harga, 1, 2);
        grid.add(new Label("Kategori:"), 0, 3); grid.add(kategori, 1, 3);
        grid.add(new Label("Stok:"), 0, 4); grid.add(stok, 1, 4);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == tambahButtonType) {
                try {
                    return new Menu(0, namaMenu.getText(), deskripsi.getText(), Integer.parseInt(harga.getText()), kategori.getText(), Integer.parseInt(stok.getText()), true, Session.getInstance().getIdCabang());
                } catch (NumberFormatException e) {
                    new Alert(AlertType.ERROR, "Harga dan Stok harus angka.").show();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newMenu -> {
            String sql = "INSERT INTO Menu(nama_menu, deskripsi, harga, kategori, stok, tersedia, id_cabang) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newMenu.getNama());
                stmt.setString(2, newMenu.getDeskripsi());
                stmt.setInt(3, newMenu.getHarga());
                stmt.setString(4, newMenu.getKategori());
                stmt.setInt(5, newMenu.getStok());
                stmt.setBoolean(6, newMenu.isTersedia());
                stmt.setInt(7, newMenu.getIdCabang());
                stmt.executeUpdate();
                new Alert(AlertType.INFORMATION, "Menu berhasil ditambahkan!").show();
                loadMenu();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void editMenu() {
        Menu selectedMenu = menuTable.getSelectionModel().getSelectedItem();
        if (selectedMenu == null) {
            new Alert(AlertType.WARNING, "Pilih menu yang ingin diedit.").show();
            return;
        }

        Dialog<Menu> dialog = new Dialog<>();
        dialog.setTitle("Edit Menu");
        ButtonType simpanButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(simpanButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        TextField namaMenu = new TextField(selectedMenu.getNama());
        TextArea deskripsi = new TextArea(selectedMenu.getDeskripsi());
        TextField harga = new TextField(String.valueOf(selectedMenu.getHarga()));
        TextField kategori = new TextField(selectedMenu.getKategori());
        TextField stok = new TextField(String.valueOf(selectedMenu.getStok()));
        CheckBox tersedia = new CheckBox("Tersedia");
        tersedia.setSelected(selectedMenu.isTersedia());
        grid.add(new Label("Nama Menu:"), 0, 0); grid.add(namaMenu, 1, 0);
        grid.add(new Label("Deskripsi:"), 0, 1); grid.add(deskripsi, 1, 1);
        grid.add(new Label("Harga:"), 0, 2); grid.add(harga, 1, 2);
        grid.add(new Label("Kategori:"), 0, 3); grid.add(kategori, 1, 3);
        grid.add(new Label("Stok:"), 0, 4); grid.add(stok, 1, 4);
        grid.add(tersedia, 1, 5);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == simpanButtonType) {
                try {
                    // Update objek yang sudah ada, jangan buat baru
                    selectedMenu.setNama(namaMenu.getText());
                    selectedMenu.setDeskripsi(deskripsi.getText());
                    selectedMenu.setHarga(Integer.parseInt(harga.getText()));
                    selectedMenu.setKategori(kategori.getText());
                    selectedMenu.setStok(Integer.parseInt(stok.getText()));
                    selectedMenu.setTersedia(tersedia.isSelected());
                    return selectedMenu;
                } catch (NumberFormatException e) {
                    new Alert(AlertType.ERROR, "Harga dan Stok harus angka.").show();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedMenu -> {
            String sql = "UPDATE Menu SET nama_menu = ?, deskripsi = ?, harga = ?, kategori = ?, stok = ?, tersedia = ? WHERE id_menu = ? AND id_cabang = ?";
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, updatedMenu.getNama());
                stmt.setString(2, updatedMenu.getDeskripsi());
                stmt.setInt(3, updatedMenu.getHarga());
                stmt.setString(4, updatedMenu.getKategori());
                stmt.setInt(5, updatedMenu.getStok());
                stmt.setBoolean(6, updatedMenu.isTersedia());
                stmt.setInt(7, updatedMenu.getId());
                stmt.setInt(8, Session.getInstance().getIdCabang());
                if (stmt.executeUpdate() > 0) {
                    new Alert(AlertType.INFORMATION, "Menu berhasil diperbarui!").show();
                    loadMenu();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void hapusMenu() {
        Menu selectedMenu = menuTable.getSelectionModel().getSelectedItem();
        if (selectedMenu == null) {
            new Alert(AlertType.WARNING, "Pilih menu yang ingin dihapus.").show();
            return;
        }

        Alert confirmDialog = new Alert(AlertType.CONFIRMATION, "Anda yakin ingin menghapus menu: " + selectedMenu.getNama() + "?", ButtonType.YES, ButtonType.NO);
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                String sql = "DELETE FROM Menu WHERE id_menu = ? AND id_cabang = ?";
                try (Connection conn = DatabaseConnection.connect();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, selectedMenu.getId());
                    stmt.setInt(2, Session.getInstance().getIdCabang());
                    if (stmt.executeUpdate() > 0) {
                        new Alert(AlertType.INFORMATION, "Menu berhasil dihapus!").show();
                        loadMenu();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void jadwalPengiriman() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/JadwalPengiriman.fxml"));
            VBox page = loader.load();
            Stage popupStage = new Stage();
            popupStage.setTitle("Jadwalkan Pengiriman");
            popupStage.initModality(Modality.WINDOW_MODAL);
            popupStage.initOwner(pesananTable.getScene().getWindow());
            Scene scene = new Scene(page);
            popupStage.setScene(scene);
            popupStage.showAndWait();
            loadPesanan();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Gagal membuka form penjadwalan.").show();
        }
    }

    @FXML
    private void handleLogout() {
        // --- KODE YANG DIPERBAIKI ---
        if (Session.getInstance() != null) {
            Session.getInstance().clear();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/login.fxml"));

            // 1. Muat FXML untuk mendapatkan Parent node (misal: AnchorPane)
            Parent root = loader.load();

            // 2. Buat Scene BARU dari Parent node tersebut
            Scene scene = new Scene(root);

            // 3. Dapatkan Stage saat ini dari komponen mana pun yang ada di scene
            Stage stage = (Stage) pesananTable.getScene().getWindow();

            // 4. Set Scene yang BARU ke Stage
            stage.setScene(scene);

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
        // --- AKHIR DARI KODE YANG DIPERBAIKI ---
    }

    private String getNamaCabangById(int idCabang) {
        String sql = "SELECT nama_cabang FROM Cabang WHERE id_cabang = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCabang);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nama_cabang");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Tidak Ditemukan";
    }
}