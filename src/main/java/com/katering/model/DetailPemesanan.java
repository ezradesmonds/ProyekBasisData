package com.katering.model;

public class DetailPemesanan {
    private int idPemesanan;
    private int idMenu;
    private int jumlah;

    public DetailPemesanan(int idPemesanan, int idMenu, int jumlah) {
        this.idPemesanan = idPemesanan;
        this.idMenu = idMenu;
        this.jumlah = jumlah;
    }

    public int getIdPemesanan() { return idPemesanan; }
    public int getIdMenu() { return idMenu; }
    public int getJumlah() { return jumlah; }

    public void setIdPemesanan(int idPemesanan) { this.idPemesanan = idPemesanan; }
    public void setIdMenu(int idMenu) { this.idMenu = idMenu; }
    public void setJumlah(int jumlah) { this.jumlah = jumlah; }
}