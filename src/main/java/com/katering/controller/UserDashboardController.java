package com.katering.controller;

import com.katering.database.DatabaseConnection;
import com.katering.model.DetailPemesanan;
import com.katering.model.HistoriPemesanan;
import com.katering.model.Menu;
import com.katering.util.Session;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
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
    private ObservableList<DetailPemesanan> keranjangList = FXCollections.observableArrayList();

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

        // Log user login activity
        logAktivitas("User logged in.");
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
                        int subtotal = quantity * selectedMenu.getHarga();
                        // Check if the item already exists in the cart
                        boolean found = false;
                        for (DetailPemesanan item : keranjangList) {
                            if (item.getIdMenu() == selectedMenu.getId()) {
                                item.setJumlah(item.getJumlah() + quantity);
                                item.setSubtotal(item.getSubtotal() + subtotal);
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            keranjangList.add(new DetailPemesanan(0, selectedMenu.getId(), quantity, subtotal)); // idPemesanan will be set later
                        }
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
        if (keranjangList.isEmpty()) {
            new Alert(AlertType.INFORMATION, "Keranjang Anda kosong.").show();
            return;
        }

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Isi Keranjang");
        alert.setHeaderText("Daftar Item di Keranjang Anda:");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);

        grid.addRow(0, new Label("Nama Menu"), new Label("Jumlah"), new Label("Subtotal"));

        int row = 1;
        int totalHargaKeranjang = 0;
        for (DetailPemesanan item : keranjangList) {
            String namaMenu = getNamaMenuById(item.getIdMenu());
            grid.addRow(row++,
                    new Label(namaMenu),
                    new Label(String.valueOf(item.getJumlah())),
                    new Label(String.valueOf(item.getSubtotal()))
            );
            totalHargaKeranjang += item.getSubtotal();
        }

        grid.addRow(row, new Label(""), new Label("Total:"), new Label(String.valueOf(totalHargaKeranjang)));

        alert.getDialogPane().setContent(grid);
        alert.showAndWait();
    }

    private String getNamaMenuById(int idMenu) {
        for (Menu menu : menuList) { // Assuming menuList contains all relevant menus
            if (menu.getId() == idMenu) {
                return menu.getNama();
            }
        }
        return "Menu Tidak Ditemukan";
    }

    @FXML
    private void checkout() {
        if (keranjangList.isEmpty()) {
            new Alert(AlertType.INFORMATION, "Keranjang Anda kosong. Tidak ada yang bisa di-checkout.").show();
            return;
        }

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Checkout");
        alert.setHeaderText("Detail Pesanan Anda:");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);

        grid.addRow(0, new Label("ID Menu"), new Label("Nama Menu"), new Label("Jumlah"), new Label("Subtotal"));

        int row = 1;
        int totalKeseluruhan = 0;

        for (DetailPemesanan item : keranjangList) {
            String namaMenu = getNamaMenuById(item.getIdMenu());
            int hargaMenu = getHargaMenuById(item.getIdMenu());
            int subtotal = item.getJumlah() * hargaMenu;
            item.setSubtotal(subtotal);
            totalKeseluruhan += subtotal;

            grid.addRow(row++,
                    new Label(String.valueOf(item.getIdMenu())),
                    new Label(namaMenu),
                    new Label(String.valueOf(item.getJumlah())),
                    new Label(String.valueOf(item.getSubtotal()))
            );
        }

        // --- LOGIKA DISKON YANG DIPERBAIKI ---
        String appliedPromoName = "Tidak ada";
        int discountAmount = 0;
        int finalTotalHarga = totalKeseluruhan;

        try {
            int idCabangSaatIni = getIdCabangByName(cabangComboBox.getValue());
            LocalDate currentDate = LocalDate.now();

            // Query baru yang sesuai dengan struktur tabel Promo
            String sql = "SELECT nama_promo, diskon, target_kategori FROM Promo " +
                    "WHERE (tgl_mulai <= ? AND tgl_selesai >= ?) " +
                    "AND (" +
                    "   (target_cabang_id IS NULL AND target_kategori IS NULL) OR " +
                    "   (target_cabang_id = ?) OR " +
                    "   (target_kategori IS NOT NULL)" +
                    ")";

            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setDate(1, Date.valueOf(currentDate));
                stmt.setDate(2, Date.valueOf(currentDate));
                stmt.setInt(3, idCabangSaatIni);

                ResultSet rs = stmt.executeQuery();

                // Cari diskon terbaik yang bisa diaplikasikan
                while (rs.next()) {
                    String promoNama = rs.getString("nama_promo");
                    int diskonPercentage = rs.getInt("diskon");
                    String targetKategori = rs.getString("target_kategori");
                    int currentPromoDiscount = 0;

                    if (targetKategori != null) {
                        // Jika promo berbasis kategori, hitung subtotal untuk kategori tersebut
                        int subtotalKategori = 0;
                        for (DetailPemesanan item : keranjangList) {
                            if (getKategoriMenuById(item.getIdMenu()).equalsIgnoreCase(targetKategori)) {
                                subtotalKategori += item.getSubtotal();
                            }
                        }
                        currentPromoDiscount = (subtotalKategori * diskonPercentage) / 100;
                    } else {
                        // Jika promo umum (berlaku untuk semua atau cabang ini)
                        currentPromoDiscount = (totalKeseluruhan * diskonPercentage) / 100;
                    }

                    // Ambil diskon terbesar
                    if (currentPromoDiscount > discountAmount) {
                        discountAmount = currentPromoDiscount;
                        appliedPromoName = promoNama;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Gagal memeriksa promo: " + e.getMessage()).show();
            // Return agar tidak melanjutkan checkout jika ada error promo
            return;
        }

        finalTotalHarga -= discountAmount;

        grid.addRow(row++, new Label(""), new Label(""), new Label("Diskon Promo:"), new Label("- " + discountAmount + " (" + appliedPromoName + ")"));
        grid.addRow(row, new Label(""), new Label(""), new Label("Total Pembayaran Akhir:"), new Label(String.valueOf(finalTotalHarga)));
        alert.getDialogPane().setContent(grid);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            processCheckout(finalTotalHarga);
            logAktivitas("User checked out a new order. Order total: " + finalTotalHarga + ". Promo applied: " + appliedPromoName);
        } else {
            new Alert(AlertType.INFORMATION, "Checkout dibatalkan.").show();
        }
    }

    // Tambahkan method helper baru ini di dalam UserDashboardController.java
    private String getKategoriMenuById(int idMenu) {
        for (Menu menu : menuList) { // Asumsi menuList sudah ter-load dengan benar
            if (menu.getId() == idMenu) {
                return menu.getKategori();
            }
        }
        return ""; // Return string kosong jika tidak ketemu
    }

    private int getHargaMenuById(int idMenu) {
        for (Menu menu : menuList) { // Assuming menuList contains all relevant menus
            if (menu.getId() == idMenu) {
                return menu.getHarga();
            }
        }
        return 0; // Should not happen if menu data is consistent
    }

    private void processCheckout(int totalHarga) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.connect();
            conn.setAutoCommit(false); // Start transaction

            // 1. Insert into Pemesanan
            String insertPemesananSQL = "INSERT INTO Pemesanan (id_pengguna, id_status, id_cabang, tgl_pesan, total_harga) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmtPemesanan = conn.prepareStatement(insertPemesananSQL, Statement.RETURN_GENERATED_KEYS);
            stmtPemesanan.setInt(1, Session.getInstance().getIdPengguna());
            stmtPemesanan.setInt(2, 1); // Status 'Pending' (assuming id_status 1 is Pending)
            // Get id_cabang from the selected cabangComboBox
            int idCabang = getIdCabangByName(cabangComboBox.getValue());
            stmtPemesanan.setInt(3, idCabang);
            stmtPemesanan.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmtPemesanan.setInt(5, totalHarga); // Menggunakan totalHarga yang sudah diproses diskon
            stmtPemesanan.executeUpdate();

            ResultSet rsPemesanan = stmtPemesanan.getGeneratedKeys();
            int idPemesanan = -1;
            if (rsPemesanan.next()) {
                idPemesanan = rsPemesanan.getInt(1);
            } else {
                throw new SQLException("Gagal mendapatkan ID pemesanan yang dihasilkan.");
            }

            // 2. Insert into Detil_pesanan
            String insertDetailSQL = "INSERT INTO Detil_pesanan (id_pemesanan, id_menu, jumlah, subtotal) VALUES (?, ?, ?, ?)";
            PreparedStatement stmtDetail = conn.prepareStatement(insertDetailSQL);
            for (DetailPemesanan item : keranjangList) {
                stmtDetail.setInt(1, idPemesanan);
                stmtDetail.setInt(2, item.getIdMenu());
                stmtDetail.setInt(3, item.getJumlah());
                stmtDetail.setInt(4, item.getSubtotal());
                stmtDetail.addBatch(); // Add to batch for efficient insertion
            }
            stmtDetail.executeBatch(); // Execute all batched inserts

            // 3. Update stok di tabel Menu
            String updateStokSQL = "UPDATE Menu SET stok = stok - ? WHERE id_menu = ?";
            PreparedStatement stmtUpdateStok = conn.prepareStatement(updateStokSQL);
            for (DetailPemesanan item : keranjangList) {
                stmtUpdateStok.setInt(1, item.getJumlah());
                stmtUpdateStok.setInt(2, item.getIdMenu());
                stmtUpdateStok.addBatch();
            }
            stmtUpdateStok.executeBatch();

            conn.commit(); // Commit the transaction
            new Alert(AlertType.INFORMATION, "Pemesanan berhasil! ID Pemesanan Anda: " + idPemesanan).show();
            keranjangList.clear(); // Clear the cart after successful checkout
            loadMenuByCabang(cabangComboBox.getValue()); // Reload menu to reflect updated stock

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback transaction on error
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            new Alert(AlertType.ERROR, "Gagal memproses checkout: " + e.getMessage()).show();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int getIdCabangByName(String namaCabang) throws SQLException {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT id_cabang FROM Cabang WHERE nama_cabang = ?")) {
            stmt.setString(1, namaCabang);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_cabang");
                }
            }
        }
        throw new SQLException("ID Cabang tidak ditemukan untuk nama: " + namaCabang);
    }

    @FXML
    private void lihatRiwayatPemesanan() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Riwayat Pemesanan");
        alert.setHeaderText("Detail Riwayat Pemesanan Anda:");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.addRow(0, new Label("ID Pemesanan"), new Label("Menu"), new Label("Jumlah"), new Label("Total"), new Label("Waktu"));

        int row = 1;
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT p.id_pemesanan, m.nama_menu, dp.jumlah, dp.subtotal, p.tgl_pesan " +
                             "FROM Pemesanan p " +
                             "JOIN Detil_pesanan dp ON p.id_pemesanan = dp.id_pemesanan " +
                             "JOIN Menu m ON dp.id_menu = m.id_menu " +
                             "WHERE p.id_pengguna = ? ORDER BY p.tgl_pesan DESC")) {
            stmt.setInt(1, Session.getInstance().getIdPengguna());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                HistoriPemesanan histori = new HistoriPemesanan(
                        rs.getInt("id_pemesanan"),
                        rs.getString("nama_menu"),
                        rs.getInt("jumlah"),
                        rs.getInt("subtotal"),
                        rs.getTimestamp("tgl_pesan").toLocalDateTime()
                );
                grid.addRow(row++,
                        new Label(String.valueOf(histori.getId())),
                        new Label(histori.getMenu()),
                        new Label(String.valueOf(histori.getJumlah())),
                        new Label(String.valueOf(histori.getTotal())),
                        new Label(histori.getWaktu().toString())
                );
            }

            if (row == 1) {
                grid.addRow(row, new Label("Tidak ada riwayat pemesanan."));
            }

            alert.getDialogPane().setContent(grid);
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Gagal memuat riwayat pemesanan: " + e.getMessage()).show();
        }
    }

    @FXML
    private void beriRatingDanKomentar() {
        // Step 1: Show list of orders for the user to choose
        ObservableList<HistoriPemesanan> userOrders = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT p.id_pemesanan, p.tgl_pesan, p.total_harga, p.rating, p.komentar " +
                             "FROM Pemesanan p " +
                             "WHERE p.id_pengguna = ? AND p.rating IS NULL ORDER BY p.tgl_pesan DESC")) { // Only show orders not yet rated
            stmt.setInt(1, Session.getInstance().getIdPengguna());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                userOrders.add(new HistoriPemesanan(
                        rs.getInt("id_pemesanan"),
                        "Multiple items", // Placeholder
                        0, // Placeholder
                        rs.getInt("total_harga"),
                        rs.getTimestamp("tgl_pesan").toLocalDateTime()
                ));
            }

            if (userOrders.isEmpty()) {
                new Alert(AlertType.INFORMATION, "Tidak ada pesanan yang belum diberi rating.").show();
                return;
            }

            ChoiceDialog<HistoriPemesanan> orderSelectionDialog = new ChoiceDialog<>(userOrders.get(0), userOrders);
            orderSelectionDialog.setTitle("Beri Rating & Komentar");
            orderSelectionDialog.setHeaderText("Pilih pesanan yang ingin Anda beri rating:");
            orderSelectionDialog.setContentText("Pesanan:");

            Optional<HistoriPemesanan> selectedOrderResult = orderSelectionDialog.showAndWait();

            if (selectedOrderResult.isPresent()) {
                HistoriPemesanan selectedOrder = selectedOrderResult.get();

                // Step 2: Show rating and comment input
                Dialog<Pair<Integer, String>> ratingCommentDialog = new Dialog<>();
                ratingCommentDialog.setTitle("Beri Rating & Komentar");
                ratingCommentDialog.setHeaderText("Rating untuk Pesanan ID: " + selectedOrder.getId());

                // Set the button types
                ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
                ratingCommentDialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));

                Label ratingLabel = new Label("Rating (1-5):");
                Spinner<Integer> ratingSpinner = new Spinner<>(1, 5, 5); // Default to 5
                ratingSpinner.setEditable(true);

                Label commentLabel = new Label("Komentar:");
                TextArea commentTextArea = new TextArea();
                commentTextArea.setWrapText(true);
                commentTextArea.setPromptText("Tulis komentar Anda di sini (opsional)...");

                grid.add(ratingLabel, 0, 0);
                grid.add(ratingSpinner, 1, 0);
                grid.add(commentLabel, 0, 1);
                grid.add(commentTextArea, 1, 1);

                ratingCommentDialog.getDialogPane().setContent(grid);

                // Convert the result to a rating and comment pair when the submit button is clicked
                ratingCommentDialog.setResultConverter(dialogButton -> {
                    if (dialogButton == submitButtonType) {
                        return new Pair<>(ratingSpinner.getValue(), commentTextArea.getText());
                    }
                    return null;
                });

                Optional<Pair<Integer, String>> result = ratingCommentDialog.showAndWait();

                result.ifPresent(pair -> {
                    int rating = pair.getKey();
                    String komentar = pair.getValue();
                    updatePemesananWithRating(selectedOrder.getId(), rating, komentar);
                });

            } else {
                new Alert(AlertType.INFORMATION, "Pemberian rating dibatalkan.").show();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Gagal memuat pesanan untuk rating: " + e.getMessage()).show();
        }
    }

    private void updatePemesananWithRating(int idPemesanan, int rating, String komentar) {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE Pemesanan SET rating = ?, komentar = ?, tgl_review = ? WHERE id_pemesanan = ?")) {
            stmt.setInt(1, rating);
            stmt.setString(2, komentar.isEmpty() ? null : komentar); // Set to NULL if comment is empty
            stmt.setDate(3, Date.valueOf(LocalDate.now())); // Current date for tgl_review
            stmt.setInt(4, idPemesanan);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                new Alert(AlertType.INFORMATION, "Rating dan komentar berhasil disimpan untuk Pesanan ID: " + idPemesanan).show();
            } else {
                new Alert(AlertType.WARNING, "Gagal menyimpan rating dan komentar. Pesanan mungkin tidak ditemukan.").show();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Gagal menyimpan rating dan komentar: " + e.getMessage()).show();
        }
    }


    @FXML
    private void pantauStatus() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Status Pesanan & Pengiriman");
        alert.setHeaderText("Status Pesanan Anda:");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.addRow(0, new Label("ID Pesanan"), new Label("Status"), new Label("Waktu Kirim"));

        int row = 1;
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT p.id_pemesanan, sp.status, jp.waktu_kirim " +
                             "FROM Pemesanan p " +
                             "JOIN Status_pesanan sp ON p.id_status = sp.id_status " +
                             "LEFT JOIN Jadwal_pengiriman jp ON p.id_pemesanan = jp.id_pengiriman " +
                             "WHERE p.id_pengguna = ? ORDER BY p.tgl_pesan DESC")) {
            stmt.setInt(1, Session.getInstance().getIdPengguna());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int idPemesanan = rs.getInt("id_pemesanan");
                String status = rs.getString("status");
                Timestamp waktuKirimTimestamp = rs.getTimestamp("waktu_kirim");
                String waktuKirim = (waktuKirimTimestamp != null) ? waktuKirimTimestamp.toLocalDateTime().toString() : "Belum Dijadwalkan";

                grid.addRow(row++,
                        new Label(String.valueOf(idPemesanan)),
                        new Label(status),
                        new Label(waktuKirim)
                );
            }

            if (row == 1) { // No orders found
                grid.addRow(row, new Label("Tidak ada pesanan yang sedang berlangsung atau riwayat pengiriman."));
            }

            alert.getDialogPane().setContent(grid);
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Gagal memuat status pesanan: " + e.getMessage()).show();
        }
    }

    @FXML
    private void lihatLogAktivitasSaya() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Log Aktivitas Saya");
        alert.setHeaderText("Riwayat Aktivitas Anda:");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.addRow(0, new Label("Waktu"), new Label("Aktivitas"));

        int row = 1;
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT aktivitas, timestamp FROM Log_aktivitas WHERE id_pengguna = ? ORDER BY timestamp DESC")) {
            stmt.setInt(1, Session.getInstance().getIdPengguna());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
                String aktivitas = rs.getString("aktivitas");

                grid.addRow(row++,
                        new Label(timestamp.toString()),
                        new Label(aktivitas)
                );
            }

            if (row == 1) { // No logs found
                grid.addRow(row, new Label("Belum ada log aktivitas untuk akun ini."));
            }

            alert.getDialogPane().setContent(grid);
            alert.getDialogPane().setPrefSize(600, 400);
            alert.showAndWait();

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Gagal memuat log aktivitas: " + e.getMessage()).show();
        }
    }

    @FXML
    private void checkPromo() {
        if (cabangComboBox.getValue() == null) {
            new Alert(AlertType.WARNING, "Silakan pilih cabang terlebih dahulu.").show();
            return;
        }

        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Promo Aktif");
        alert.setHeaderText("Daftar Promo yang Sedang Berlaku untuk Cabang " + cabangComboBox.getValue() + ":");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setPadding(new Insets(10));

        // --- PERUBAHAN 1: Tambahkan header kolom "Diskon" ---
        grid.addRow(0, new Label("Nama Promo"), new Label("Deskripsi"), new Label("Diskon"), new Label("Target"));

        int row = 1;
        try {
            int idCabangSaatIni = getIdCabangByName(cabangComboBox.getValue());
            LocalDate currentDate = LocalDate.now();

            String sql = "SELECT nama_promo, deskripsi, diskon, target_kategori " +
                    "FROM Promo " +
                    "WHERE (tgl_mulai <= ? AND tgl_selesai >= ?) " +
                    "AND (" +
                    "   (target_cabang_id IS NULL AND target_kategori IS NULL) OR " +
                    "   (target_cabang_id = ?) OR " +
                    "   (target_kategori IS NOT NULL)" +
                    ") ORDER BY id_promo";

            try (Connection conn = DatabaseConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setDate(1, Date.valueOf(currentDate));
                stmt.setDate(2, Date.valueOf(currentDate));
                stmt.setInt(3, idCabangSaatIni);

                ResultSet rs = stmt.executeQuery();
                boolean promoFound = false;
                while (rs.next()) {
                    promoFound = true;
                    String namaPromo = rs.getString("nama_promo");
                    String deskripsi = rs.getString("deskripsi");
                    String targetKategori = rs.getString("target_kategori");

                    // --- PERUBAHAN 2: Ambil nilai diskon dan format teksnya ---
                    int diskon = rs.getInt("diskon");
                    String diskonText = diskon + "%";

                    String targetText;
                    if (targetKategori != null) {
                        targetText = "Kategori: " + targetKategori;
                    } else {
                        targetText = "Semua Menu";
                    }

                    // --- PERUBAHAN 3: Tambahkan Label diskon ke dalam baris ---
                    grid.addRow(row++,
                            new Label(namaPromo),
                            new Label(deskripsi),
                            new Label(diskonText), // <-- Kolom baru ditambahkan di sini
                            new Label(targetText)
                    );
                }

                if (!promoFound) {
                    grid.addRow(row, new Label("Tidak ada promo aktif untuk cabang ini saat ini."));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Gagal memuat promo: " + e.getMessage()).show();
        }

        alert.getDialogPane().setContent(grid);
        alert.getDialogPane().setPrefSize(700, 300);
        alert.showAndWait();
    }

    private void logAktivitas(String activity) {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO Log_aktivitas (id_pengguna, aktivitas, timestamp) VALUES (?, ?, ?)")) {
            stmt.setInt(1, Session.getInstance().getIdPengguna());
            stmt.setString(2, activity);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Gagal mencatat aktivitas: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        logAktivitas("User logged out."); // Log logout activity
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

    private static class Pair<K, V> {
        private final K key;
        private final V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }
}