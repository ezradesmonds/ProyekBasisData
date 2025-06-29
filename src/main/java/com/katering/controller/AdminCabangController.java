package com.katering.controller;

import com.katering.database.DatabaseConnection;
import com.katering.model.Pesanan;
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
import java.sql.*;
import java.util.Optional;

public class AdminCabangController {

    @FXML private TableView<Pesanan> pesananTable;
    @FXML private TableColumn<Pesanan, Integer> idCol;
    @FXML private TableColumn<Pesanan, String> namaCol;
    @FXML private TableColumn<Pesanan, String> statusCol;

    @FXML private TableView<Menu> menuTable;
    @FXML private TableColumn<Menu, Integer> menuIdCol;
    @FXML private TableColumn<Menu, String> menuNamaCol;
    @FXML private TableColumn<Menu, String> menuDeskripsiCol;
    @FXML private TableColumn<Menu, Integer> menuHargaCol;
    @FXML private TableColumn<Menu, String> menuKategoriCol;
    @FXML private TableColumn<Menu, Integer> menuStokCol;
    @FXML private TableColumn<Menu, Boolean> menuTersediaCol;

    private ObservableList<Pesanan> pesananList = FXCollections.observableArrayList();
    private ObservableList<Menu> menuList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleIntegerProperty(cell.getValue().getId()).asObject());
        namaCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNamaPelanggan()));
        statusCol.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getStatus()));
        loadPesanan();

        menuIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        menuNamaCol.setCellValueFactory(new PropertyValueFactory<>("nama"));
        menuDeskripsiCol.setCellValueFactory(new PropertyValueFactory<>("deskripsi"));
        menuHargaCol.setCellValueFactory(new PropertyValueFactory<>("harga"));
        menuKategoriCol.setCellValueFactory(new PropertyValueFactory<>("kategori"));
        menuStokCol.setCellValueFactory(new PropertyValueFactory<>("stok"));
        menuTersediaCol.setCellValueFactory(new PropertyValueFactory<>("tersedia"));
        loadMenu();
    }

    private void loadPesanan() {
        pesananList.clear();
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT p.id_pemesanan, u.nama, sp.status " +
                             "FROM Pemesanan p JOIN Pengguna u ON p.id_pengguna = u.id_pengguna " +
                             "JOIN Status_pesanan sp ON p.id_status = sp.id_status " +
                             "WHERE p.id_cabang = ? ORDER BY p.tgl_pesan DESC")) {
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
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Gagal memuat pesanan: " + e.getMessage()).show();
        }
    }

    private void loadMenu() {
        menuList.clear();
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id_menu, nama_menu, deskripsi, harga, kategori, stok, tersedia, id_cabang " +
                             "FROM Menu WHERE id_cabang = ? ORDER BY nama_menu")) {
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

    @FXML
    private void updateStatusPesanan() {
        Pesanan selectedPesanan = pesananTable.getSelectionModel().getSelectedItem();
        if (selectedPesanan != null) {
            ChoiceDialog<String> dialog = new ChoiceDialog<>("Pending", "Processed", "Shipped", "Delivered", "Cancelled");
            dialog.setTitle("Update Status Pesanan");
            dialog.setHeaderText("Pilih status baru untuk pesanan ID: " + selectedPesanan.getId());
            dialog.setContentText("Status:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(newStatus -> {
                try (Connection conn = DatabaseConnection.connect();
                     PreparedStatement stmt = conn.prepareStatement(
                             "UPDATE Pemesanan SET id_status = (SELECT id_status FROM Status_pesanan WHERE status = ?) WHERE id_pemesanan = ? AND id_cabang = ?")) {
                    stmt.setString(1, newStatus);
                    stmt.setInt(2, selectedPesanan.getId());
                    stmt.setInt(3, Session.getInstance().getIdCabang());
                    int updatedRows = stmt.executeUpdate();
                    if (updatedRows > 0) {
                        new Alert(AlertType.INFORMATION, "Status pesanan berhasil diperbarui!").show();
                        loadPesanan();
                    } else {
                        new Alert(AlertType.WARNING, "Gagal memperbarui status pesanan. Pastikan pesanan milik cabang ini.").show();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    new Alert(AlertType.ERROR, "Gagal memperbarui status pesanan: " + e.getMessage()).show();
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
        dialog.setHeaderText("Masukkan detail menu baru");

        ButtonType tambahButtonType = new ButtonType("Tambah", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(tambahButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField namaMenu = new TextField();
        namaMenu.setPromptText("Nama Menu");
        TextArea deskripsi = new TextArea();
        deskripsi.setPromptText("Deskripsi");
        deskripsi.setWrapText(true);
        TextField harga = new TextField();
        harga.setPromptText("Harga");
        TextField kategori = new TextField();
        kategori.setPromptText("Kategori");
        TextField stok = new TextField();
        stok.setPromptText("Stok");
        CheckBox tersedia = new CheckBox("Tersedia");
        tersedia.setSelected(true);

        grid.add(new Label("Nama Menu:"), 0, 0);
        grid.add(namaMenu, 1, 0);
        grid.add(new Label("Deskripsi:"), 0, 1);
        grid.add(deskripsi, 1, 1);
        grid.add(new Label("Harga:"), 0, 2);
        grid.add(harga, 1, 2);
        grid.add(new Label("Kategori:"), 0, 3);
        grid.add(kategori, 1, 3);
        grid.add(new Label("Stok:"), 0, 4);
        grid.add(stok, 1, 4);
        grid.add(tersedia, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == tambahButtonType) {
                try {
                    return new Menu(
                            0,
                            namaMenu.getText(),
                            deskripsi.getText(),
                            Integer.parseInt(harga.getText()),
                            kategori.getText(),
                            Integer.parseInt(stok.getText()),
                            tersedia.isSelected(),
                            Session.getInstance().getIdCabang()
                    );
                } catch (NumberFormatException e) {
                    new Alert(AlertType.ERROR, "Harga dan Stok harus berupa angka.").show();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newMenu -> {
            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO Menu(nama_menu, deskripsi, harga, kategori, stok, tersedia, id_cabang) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
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
                new Alert(AlertType.ERROR, "Gagal menambahkan menu: " + e.getMessage()).show();
            }
        });
    }

    @FXML
    private void editMenu() {
        Menu selectedMenu = menuTable.getSelectionModel().getSelectedItem();
        if (selectedMenu != null) {
            Dialog<Menu> dialog = new Dialog<>();
            dialog.setTitle("Edit Menu");
            dialog.setHeaderText("Edit detail menu: " + selectedMenu.getNama());

            ButtonType simpanButtonType = new ButtonType("Simpan", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(simpanButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

            TextField namaMenu = new TextField(selectedMenu.getNama());
            namaMenu.setPromptText("Nama Menu");
            TextArea deskripsi = new TextArea(selectedMenu.getDeskripsi());
            deskripsi.setPromptText("Deskripsi");
            deskripsi.setWrapText(true);
            TextField harga = new TextField(String.valueOf(selectedMenu.getHarga()));
            harga.setPromptText("Harga");

            String currentKategori = selectedMenu.getKategori();
            int currentStok = selectedMenu.getStok();

            TextField kategori = new TextField(currentKategori);
            kategori.setPromptText("Kategori");
            TextField stok = new TextField(String.valueOf(currentStok));
            stok.setPromptText("Stok");
            CheckBox tersedia = new CheckBox("Tersedia");
            tersedia.setSelected(selectedMenu.isTersedia());

            grid.add(new Label("Nama Menu:"), 0, 0);
            grid.add(namaMenu, 1, 0);
            grid.add(new Label("Deskripsi:"), 0, 1);
            grid.add(deskripsi, 1, 1);
            grid.add(new Label("Harga:"), 0, 2);
            grid.add(harga, 1, 2);
            grid.add(new Label("Kategori:"), 0, 3);
            grid.add(kategori, 1, 3);
            grid.add(new Label("Stok:"), 0, 4);
            grid.add(stok, 1, 4);
            grid.add(tersedia, 1, 5);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == simpanButtonType) {
                    try {
                        selectedMenu.setNama(namaMenu.getText());
                        selectedMenu.setDeskripsi(deskripsi.getText());
                        selectedMenu.setHarga(Integer.parseInt(harga.getText()));
                        selectedMenu.setKategori(kategori.getText());
                        selectedMenu.setStok(Integer.parseInt(stok.getText()));
                        selectedMenu.setTersedia(tersedia.isSelected());
                        return selectedMenu;
                    } catch (NumberFormatException e) {
                        new Alert(AlertType.ERROR, "Harga dan Stok harus berupa angka.").show();
                        return null;
                    }
                }
                return null;
            });

            dialog.showAndWait().ifPresent(updatedMenu -> {
                try (Connection conn = DatabaseConnection.connect();
                     PreparedStatement stmt = conn.prepareStatement(
                             "UPDATE Menu SET nama_menu = ?, deskripsi = ?, harga = ?, kategori = ?, stok = ?, tersedia = ? WHERE id_menu = ? AND id_cabang = ?")) {
                    stmt.setString(1, updatedMenu.getNama());
                    stmt.setString(2, updatedMenu.getDeskripsi());
                    stmt.setInt(3, updatedMenu.getHarga());
                    stmt.setString(4, updatedMenu.getKategori());
                    stmt.setInt(5, updatedMenu.getStok());
                    stmt.setBoolean(6, updatedMenu.isTersedia());
                    stmt.setInt(7, updatedMenu.getId());
                    stmt.setInt(8, Session.getInstance().getIdCabang());
                    int updatedRows = stmt.executeUpdate();
                    if (updatedRows > 0) {
                        new Alert(AlertType.INFORMATION, "Menu berhasil diperbarui!").show();
                        loadMenu();
                    } else {
                        new Alert(AlertType.WARNING, "Menu tidak ditemukan atau bukan milik cabang ini.").show();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    new Alert(AlertType.ERROR, "Gagal memperbarui menu: " + e.getMessage()).show();
                }
            });
        } else {
            new Alert(AlertType.WARNING, "Pilih menu yang ingin diedit.").show();
        }
    }


    @FXML
    private void hapusMenu() {
        Menu selectedMenu = menuTable.getSelectionModel().getSelectedItem();
        if (selectedMenu != null) {
            Alert confirmDialog = new Alert(AlertType.CONFIRMATION);
            confirmDialog.setTitle("Konfirmasi Hapus Menu");
            confirmDialog.setHeaderText("Anda yakin ingin menghapus menu ini?");
            confirmDialog.setContentText("Menu: " + selectedMenu.getNama() + " (ID: " + selectedMenu.getId() + ")");

            Optional<ButtonType> result = confirmDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try (Connection conn = DatabaseConnection.connect();
                     PreparedStatement stmt = conn.prepareStatement("DELETE FROM Menu WHERE id_menu = ? AND id_cabang = ?")) {
                    stmt.setInt(1, selectedMenu.getId());
                    stmt.setInt(2, Session.getInstance().getIdCabang());
                    int deleted = stmt.executeUpdate();
                    if (deleted > 0) {
                        new Alert(AlertType.INFORMATION, "Menu berhasil dihapus!").show();
                        loadMenu();
                    } else {
                        new Alert(AlertType.WARNING, "Menu tidak ditemukan atau bukan milik cabang ini.").show();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    new Alert(AlertType.ERROR, "Gagal menghapus menu: " + e.getMessage()).show();
                }
            }
        } else {
            new Alert(AlertType.WARNING, "Pilih menu yang ingin dihapus.").show();
        }
    }


    @FXML
    private void jadwalPengiriman() {
        new Alert(AlertType.INFORMATION, "Fitur penjadwalan pengiriman belum diimplementasikan.").show();
    }

    @FXML
    private void handleLogout() {
        Session.getInstance().clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) pesananTable.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Gagal logout: " + e.getMessage()).show();
        }
    }
}
