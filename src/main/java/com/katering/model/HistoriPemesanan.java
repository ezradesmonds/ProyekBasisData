package com.katering.model;

import java.time.LocalDateTime;

public class HistoriPemesanan {
    private int id;
    private String menu;
    private int jumlah;
    private int total;
    private LocalDateTime waktu;

    public HistoriPemesanan(int id, String menu, int jumlah, int total, LocalDateTime waktu) {
        this.id = id;
        this.menu = menu;
        this.jumlah = jumlah;
        this.total = total;
        this.waktu = waktu;
    }

    public int getId() { return id; }
    public String getMenu() { return menu; }
    public int getJumlah() { return jumlah; }
    public int getTotal() { return total; }
    public LocalDateTime getWaktu() { return waktu; }

    public void setId(int id) { this.id = id; }
    public void setMenu(String menu) { this.menu = menu; }
    public void setJumlah(int jumlah) { this.jumlah = jumlah; }
    public void setTotal(int total) { this.total = total; }
    public void setWaktu(LocalDateTime waktu) { this.waktu = waktu; }

    @Override
    public String toString() {
        // Ini adalah format yang akan ditampilkan di ChoiceDialog
        return "ID Pesanan: " + id + " - Tanggal: " + waktu.toLocalDate() + " - Total: " + total;
    }
}