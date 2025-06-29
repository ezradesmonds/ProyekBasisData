package com.katering.model;

public class DetailPemesanan {
    private int idPemesanan;
    private int idMenu;
    private int jumlah;
    private int subtotal; // Added

    public DetailPemesanan(int idPemesanan, int idMenu, int jumlah, int subtotal) {
        this.idPemesanan = idPemesanan;
        this.idMenu = idMenu;
        this.jumlah = jumlah;
        this.subtotal = subtotal; // Initialize subtotal
    }

    public int getIdPemesanan() { return idPemesanan; }
    public int getIdMenu() { return idMenu; }
    public int getJumlah() { return jumlah; }
    public int getSubtotal() { return subtotal; } // Getter for subtotal

    public void setIdPemesanan(int idPemesanan) { this.idPemesanan = idPemesanan; }
    public void setIdMenu(int idMenu) { this.idMenu = idMenu; }
    public void setJumlah(int jumlah) { this.jumlah = jumlah; }
    public void setSubtotal(int subtotal) { this.subtotal = subtotal; } // Setter for subtotal
}