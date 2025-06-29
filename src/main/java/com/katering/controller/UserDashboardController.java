package com.katering.controller;

import com.katering.database.DatabaseConnection;
import com.katering.model.DetailPemesanan;
import com.katering.model.HistoriPemesanan;
import com.katering.model.Menu;
import com.katering.util.Session;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class UserDashboardController {

    @FXML private ComboBox<String> cabangComboBox;
    @FXML private ComboBox<String> kategoriComboBox;
    @FXML private TilePane menuTilePane;

    private final ObservableList<Menu> masterMenuList = FXCollections.observableArrayList();
    private final ObservableList<DetailPemesanan> keranjangList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadCabang();
        cabangComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadKategori(newVal);
                loadMenuData(newVal);
            }
        });
        kategoriComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            filterMenuByKategori(newVal);
        });
        logAktivitas("User logged in.");
    }

    // -- BAGIAN LOGIKA TAMPILAN KARTU MENU BARU --

    private Node createMenuCard(Menu menu) {
        VBox card = new VBox(10);
        card.getStyleClass().add("menu-card");
        card.setUserData(menu);

        Label namaLabel = new Label(menu.getNama());
        namaLabel.getStyleClass().add("menu-card-title");

        Label hargaLabel = new Label("Rp " + menu.getHarga());
        hargaLabel.getStyleClass().add("menu-card-details");

        Label stokLabel = new Label("Stok: " + menu.getStok());
        stokLabel.getStyleClass().add("menu-card-details");

        Button addButton = new Button("Tambah");
        addButton.getStyleClass().add("add-to-cart-button");
        addButton.setOnAction(e -> tambahKeKeranjang(menu));

        card.getChildren().addAll(namaLabel, hargaLabel, stokLabel, addButton);

        FadeTransition ft = new FadeTransition(Duration.millis(600), card);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();

        return card;
    }

    private void populateMenuDisplay(ObservableList<Menu> menusToShow) {
        menuTilePane.getChildren().clear();
        for (Menu menu : menusToShow) {
            if (menu.isTersedia() && menu.getStok() > 0) {
                menuTilePane.getChildren().add(createMenuCard(menu));
            }
        }
    }

    private void loadMenuData(String namaCabang) {
        masterMenuList.clear();
        String sql = "SELECT m.id_menu, m.nama_menu, m.deskripsi, m.harga, m.kategori, m.stok, m.tersedia, m.id_cabang FROM Menu m JOIN Cabang c ON m.id_cabang = c.id_cabang WHERE c.nama_cabang = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, namaCabang);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                masterMenuList.add(new Menu(
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
            populateMenuDisplay(masterMenuList);
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Gagal memuat menu: " + e.getMessage()).show();
        }
    }

    private void filterMenuByKategori(String kategori) {
        if (kategori == null || kategori.isEmpty() || kategori.equalsIgnoreCase("Semua")) {
            populateMenuDisplay(masterMenuList);
            return;
        }
        ObservableList<Menu> filteredList = FXCollections.observableArrayList();
        for (Menu menu : masterMenuList) {
            if (menu.getKategori().equalsIgnoreCase(kategori)) {
                filteredList.add(menu);
            }
        }
        populateMenuDisplay(filteredList);
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
        ObservableList<String> kategoriList = FXCollections.observableArrayList("Semua");
        kategoriComboBox.getSelectionModel().clearSelection();
        kategoriComboBox.setItems(null);
        String sql = "SELECT DISTINCT m.kategori FROM Menu m JOIN Cabang c ON m.id_cabang = c.id_cabang WHERE c.nama_cabang = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, namaCabang);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    kategoriList.add(rs.getString("kategori"));
                }
                kategoriComboBox.setItems(kategoriList);
                kategoriComboBox.getSelectionModel().selectFirst();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Gagal memuat data kategori: " + e.getMessage()).show();
        }
    }

    // --- BAGIAN FUNGSI-FUNGSI UTAMA (LOGIKA LENGKAP) ---

    private void tambahKeKeranjang(Menu selectedMenu) {
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
                        keranjangList.add(new DetailPemesanan(0, selectedMenu.getId(), quantity, subtotal));
                    }
                    new Alert(AlertType.INFORMATION, quantity + "x " + selectedMenu.getNama() + " ditambahkan ke keranjang.").show();
                } else {
                    new Alert(AlertType.WARNING, "Jumlah tidak valid atau melebihi stok (" + selectedMenu.getStok() + ").").show();
                }
            } catch (NumberFormatException e) {
                new Alert(AlertType.ERROR, "Jumlah harus berupa angka.").show();
            }
        });
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
            grid.addRow(row++, new Label(namaMenu), new Label(String.valueOf(item.getJumlah())), new Label(String.valueOf(item.getSubtotal())));
            totalHargaKeranjang += item.getSubtotal();
        }
        grid.addRow(row, new Label(""), new Label("Total:"), new Label(String.valueOf(totalHargaKeranjang)));
        alert.getDialogPane().setContent(grid);
        alert.showAndWait();
    }

    @FXML
    private void checkout() {
        if (keranjangList.isEmpty()) {
            new Alert(AlertType.INFORMATION, "Keranjang Anda kosong.").show();
            return;
        }

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Checkout");
        alert.setHeaderText("Detail Pesanan Anda:");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.addRow(0, new Label("Nama Menu"), new Label("Jumlah"), new Label("Subtotal"));

        int row = 1;
        int totalKeseluruhan = 0;
        for (DetailPemesanan item : keranjangList) {
            String namaMenu = getNamaMenuById(item.getIdMenu());
            grid.addRow(row++, new Label(namaMenu), new Label(String.valueOf(item.getJumlah())), new Label(String.valueOf(item.getSubtotal())));
            totalKeseluruhan += item.getSubtotal();
        }

        String appliedPromoName = "Tidak ada";
        int discountAmount = 0;
        int finalTotalHarga = totalKeseluruhan;

        try {
            int idCabangSaatIni = getIdCabangByName(cabangComboBox.getValue());
            LocalDate currentDate = LocalDate.now();
            String sql = "SELECT nama_promo, diskon, target_kategori FROM Promo WHERE (tgl_mulai <= ? AND tgl_selesai >= ?) AND ((target_cabang_id IS NULL AND target_kategori IS NULL) OR (target_cabang_id = ?) OR (target_kategori IS NOT NULL))";
            try (Connection conn = DatabaseConnection.connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDate(1, Date.valueOf(currentDate));
                stmt.setDate(2, Date.valueOf(currentDate));
                stmt.setInt(3, idCabangSaatIni);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String promoNama = rs.getString("nama_promo");
                    int diskonPercentage = rs.getInt("diskon");
                    String targetKategori = rs.getString("target_kategori");
                    int currentPromoDiscount = 0;
                    if (targetKategori != null) {
                        int subtotalKategori = 0;
                        for (DetailPemesanan item : keranjangList) {
                            if (getKategoriMenuById(item.getIdMenu()).equalsIgnoreCase(targetKategori)) {
                                subtotalKategori += item.getSubtotal();
                            }
                        }
                        currentPromoDiscount = (subtotalKategori * diskonPercentage) / 100;
                    } else {
                        currentPromoDiscount = (totalKeseluruhan * diskonPercentage) / 100;
                    }
                    if (currentPromoDiscount > discountAmount) {
                        discountAmount = currentPromoDiscount;
                        appliedPromoName = promoNama;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Gagal memeriksa promo: " + e.getMessage()).show();
            return;
        }

        finalTotalHarga -= discountAmount;
        grid.addRow(row++, new Label(""), new Label("Diskon Promo:"), new Label("- " + discountAmount + " (" + appliedPromoName + ")"));
        grid.addRow(row, new Label(""), new Label("Total Pembayaran:"), new Label(String.valueOf(finalTotalHarga)));
        alert.getDialogPane().setContent(grid);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            processCheckout(finalTotalHarga);
            logAktivitas("User checked out. Total: " + finalTotalHarga + ". Promo: " + appliedPromoName);
        } else {
            new Alert(AlertType.INFORMATION, "Checkout dibatalkan.").show();
        }
    }

    @FXML
    private void lihatRiwayatPemesanan() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Riwayat Pemesanan");
        alert.setHeaderText("Detail Riwayat Pemesanan Anda:");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.addRow(0, new Label("ID Pesanan"), new Label("Menu"), new Label("Jumlah"), new Label("Total"), new Label("Waktu"));

        int row = 1;
        String sql = "SELECT p.id_pemesanan, m.nama_menu, dp.jumlah, dp.subtotal, p.tgl_pesan FROM Pemesanan p JOIN Detil_pesanan dp ON p.id_pemesanan = dp.id_pemesanan JOIN Menu m ON dp.id_menu = m.id_menu WHERE p.id_pengguna = ? ORDER BY p.tgl_pesan DESC";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Session.getInstance().getIdPengguna());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                HistoriPemesanan histori = new HistoriPemesanan(rs.getInt("id_pemesanan"), rs.getString("nama_menu"), rs.getInt("jumlah"), rs.getInt("subtotal"), rs.getTimestamp("tgl_pesan").toLocalDateTime());
                grid.addRow(row++, new Label(String.valueOf(histori.getId())), new Label(histori.getMenu()), new Label(String.valueOf(histori.getJumlah())), new Label(String.valueOf(histori.getTotal())), new Label(histori.getWaktu().toString()));
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
        ObservableList<HistoriPemesanan> userOrders = FXCollections.observableArrayList();
        String sql = "SELECT p.id_pemesanan, p.tgl_pesan, p.total_harga FROM Pemesanan p WHERE p.id_pengguna = ? AND p.rating IS NULL ORDER BY p.tgl_pesan DESC";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Session.getInstance().getIdPengguna());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                userOrders.add(new HistoriPemesanan(rs.getInt("id_pemesanan"), "Pesanan pada " + rs.getTimestamp("tgl_pesan").toLocalDateTime().toLocalDate(), 0, rs.getInt("total_harga"), rs.getTimestamp("tgl_pesan").toLocalDateTime()));
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
            selectedOrderResult.ifPresent(selectedOrder -> {
                Dialog<Pair<Integer, String>> ratingCommentDialog = new Dialog<>();
                ratingCommentDialog.setTitle("Beri Rating & Komentar");
                ratingCommentDialog.setHeaderText("Rating untuk Pesanan ID: " + selectedOrder.getId());
                ButtonType submitButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
                ratingCommentDialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);

                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));
                Spinner<Integer> ratingSpinner = new Spinner<>(1, 5, 5);
                TextArea commentTextArea = new TextArea();
                commentTextArea.setPromptText("Tulis komentar Anda di sini (opsional)...");
                grid.add(new Label("Rating (1-5):"), 0, 0);
                grid.add(ratingSpinner, 1, 0);
                grid.add(new Label("Komentar:"), 0, 1);
                grid.add(commentTextArea, 1, 1);
                ratingCommentDialog.getDialogPane().setContent(grid);

                ratingCommentDialog.setResultConverter(dialogButton -> {
                    if (dialogButton == submitButtonType) {
                        return new Pair<>(ratingSpinner.getValue(), commentTextArea.getText());
                    }
                    return null;
                });

                Optional<Pair<Integer, String>> result = ratingCommentDialog.showAndWait();
                result.ifPresent(pair -> updatePemesananWithRating(selectedOrder.getId(), pair.getKey(), pair.getValue()));
            });
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Gagal memuat pesanan untuk rating: " + e.getMessage()).show();
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
        String sql = "SELECT p.id_pemesanan, sp.status, jp.waktu_kirim FROM Pemesanan p JOIN Status_pesanan sp ON p.id_status = sp.id_status LEFT JOIN Jadwal_pengiriman jp ON p.id_pemesanan = jp.id_pengiriman WHERE p.id_pengguna = ? ORDER BY p.tgl_pesan DESC";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Session.getInstance().getIdPengguna());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int idPemesanan = rs.getInt("id_pemesanan");
                String status = rs.getString("status");
                Timestamp waktuKirimTimestamp = rs.getTimestamp("waktu_kirim");
                String waktuKirim = (waktuKirimTimestamp != null) ? waktuKirimTimestamp.toLocalDateTime().toString() : "Belum Dijadwalkan";
                grid.addRow(row++, new Label(String.valueOf(idPemesanan)), new Label(status), new Label(waktuKirim));
            }
            if (row == 1) {
                grid.addRow(row, new Label("Tidak ada pesanan yang sedang berlangsung."));
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
        String sql = "SELECT aktivitas, timestamp FROM Log_aktivitas WHERE id_pengguna = ? ORDER BY timestamp DESC";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Session.getInstance().getIdPengguna());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDateTime timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
                String aktivitas = rs.getString("aktivitas");
                grid.addRow(row++, new Label(timestamp.toString()), new Label(aktivitas));
            }
            if (row == 1) {
                grid.addRow(row, new Label("Belum ada log aktivitas."));
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
        alert.setHeaderText("Daftar Promo yang Berlaku untuk Cabang " + cabangComboBox.getValue() + ":");
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setPadding(new Insets(10));
        grid.addRow(0, new Label("Nama Promo"), new Label("Deskripsi"), new Label("Diskon"), new Label("Target"));
        int row = 1;
        try {
            int idCabangSaatIni = getIdCabangByName(cabangComboBox.getValue());
            LocalDate currentDate = LocalDate.now();
            String sql = "SELECT nama_promo, deskripsi, diskon, target_kategori FROM Promo WHERE (tgl_mulai <= ? AND tgl_selesai >= ?) AND ((target_cabang_id IS NULL AND target_kategori IS NULL) OR (target_cabang_id = ?) OR (target_kategori IS NOT NULL)) ORDER BY id_promo";
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
                    int diskon = rs.getInt("diskon");
                    String diskonText = diskon + "%";
                    String targetKategori = rs.getString("target_kategori");
                    String targetText = (targetKategori != null) ? "Kategori: " + targetKategori : "Semua Menu";
                    grid.addRow(row++, new Label(namaPromo), new Label(deskripsi), new Label(diskonText), new Label(targetText));
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

    private void updatePemesananWithRating(int idPemesanan, int rating, String komentar) {
        String sql = "UPDATE Pemesanan SET rating = ?, komentar = ?, tgl_review = ? WHERE id_pemesanan = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, rating);
            stmt.setString(2, komentar.isEmpty() ? null : komentar);
            stmt.setDate(3, Date.valueOf(LocalDate.now()));
            stmt.setInt(4, idPemesanan);
            if (stmt.executeUpdate() > 0) {
                new Alert(AlertType.INFORMATION, "Rating dan komentar berhasil disimpan.").show();
            } else {
                new Alert(AlertType.WARNING, "Gagal menyimpan rating, pesanan tidak ditemukan.").show();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Gagal menyimpan rating: " + e.getMessage()).show();
        }
    }

    private void logAktivitas(String activity) {
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO Log_aktivitas (id_pengguna, aktivitas, timestamp) VALUES (?, ?, ?)")) {
            stmt.setInt(1, Session.getInstance().getIdPengguna());
            stmt.setString(2, activity);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        logAktivitas("User logged out.");
        Session.getInstance().clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/views/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) menuTilePane.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(AlertType.ERROR, "Gagal logout: " + e.getMessage()).show();
        }
    }

    private void processCheckout(int totalHarga) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.connect();
            conn.setAutoCommit(false);

            String insertPemesananSQL = "INSERT INTO Pemesanan (id_pengguna, id_status, id_cabang, tgl_pesan, total_harga) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmtPemesanan = conn.prepareStatement(insertPemesananSQL, Statement.RETURN_GENERATED_KEYS)) {
                stmtPemesanan.setInt(1, Session.getInstance().getIdPengguna());
                stmtPemesanan.setInt(2, 1);
                stmtPemesanan.setInt(3, getIdCabangByName(cabangComboBox.getValue()));
                stmtPemesanan.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                stmtPemesanan.setInt(5, totalHarga);
                stmtPemesanan.executeUpdate();

                try (ResultSet rsPemesanan = stmtPemesanan.getGeneratedKeys()) {
                    int idPemesanan = -1;
                    if (rsPemesanan.next()) {
                        idPemesanan = rsPemesanan.getInt(1);
                    } else {
                        throw new SQLException("Gagal mendapatkan ID pemesanan.");
                    }

                    String insertDetailSQL = "INSERT INTO Detil_pesanan (id_pemesanan, id_menu, jumlah, subtotal) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement stmtDetail = conn.prepareStatement(insertDetailSQL)) {
                        for (DetailPemesanan item : keranjangList) {
                            stmtDetail.setInt(1, idPemesanan);
                            stmtDetail.setInt(2, item.getIdMenu());
                            stmtDetail.setInt(3, item.getJumlah());
                            stmtDetail.setInt(4, item.getSubtotal());
                            stmtDetail.addBatch();
                        }
                        stmtDetail.executeBatch();
                    }

                    String updateStokSQL = "UPDATE Menu SET stok = stok - ? WHERE id_menu = ?";
                    try (PreparedStatement stmtUpdateStok = conn.prepareStatement(updateStokSQL)) {
                        for (DetailPemesanan item : keranjangList) {
                            stmtUpdateStok.setInt(1, item.getJumlah());
                            stmtUpdateStok.setInt(2, item.getIdMenu());
                            stmtUpdateStok.addBatch();
                        }
                        stmtUpdateStok.executeBatch();
                    }

                    conn.commit();
                    new Alert(AlertType.INFORMATION, "Pemesanan berhasil! ID Pemesanan Anda: " + idPemesanan).show();
                    keranjangList.clear();
                    loadMenuData(cabangComboBox.getValue());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            new Alert(AlertType.ERROR, "Gagal memproses checkout: " + e.getMessage()).show();
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // --- METHOD HELPER YANG HILANG ---
    // Tambahkan tiga method di bawah ini ke dalam kelas Anda

    /**
     * Method helper untuk mendapatkan nama menu berdasarkan ID-nya.
     * @param idMenu ID dari menu yang dicari.
     * @return Nama menu sebagai String.
     */
    private String getNamaMenuById(int idMenu) {
        for (Menu menu : masterMenuList) {
            if (menu.getId() == idMenu) {
                return menu.getNama();
            }
        }
        return "Menu Tidak Ditemukan";
    }

    /**
     * Method helper untuk mendapatkan harga menu berdasarkan ID-nya.
     * @param idMenu ID dari menu yang dicari.
     * @return Harga menu sebagai integer.
     */
    private int getHargaMenuById(int idMenu) {
        for (Menu menu : masterMenuList) {
            if (menu.getId() == idMenu) {
                return menu.getHarga();
            }
        }
        return 0; // Mengembalikan 0 jika tidak ditemukan
    }

    /**
     * Method helper untuk mendapatkan kategori menu berdasarkan ID-nya.
     * @param idMenu ID dari menu yang dicari.
     * @return Kategori menu sebagai String.
     */
    private String getKategoriMenuById(int idMenu) {
        for (Menu menu : masterMenuList) {
            if (menu.getId() == idMenu) {
                return menu.getKategori();
            }
        }
        return ""; // Mengembalikan string kosong jika tidak ditemukan
    }

    private int getIdCabangByName(String namaCabang) throws SQLException {
        String sql = "SELECT id_cabang FROM Cabang WHERE nama_cabang = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, namaCabang);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_cabang");
                }
            }
        }
        throw new SQLException("ID Cabang tidak ditemukan untuk nama: " + namaCabang);
    }

    private static class Pair<K, V> {
        private final K key;
        private final V value;
        public Pair(K key, V value) { this.key = key; this.value = value; }
        public K getKey() { return key; }
        public V getValue() { return value; }
    }
}