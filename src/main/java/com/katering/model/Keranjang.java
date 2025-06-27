package com.katering.model;

public class Keranjang {
    private int idPengguna;
    private int idMenu;
    private int jumlah;

    public Keranjang(int idPengguna, int idMenu, int jumlah) {
        this.idPengguna = idPengguna;
        this.idMenu = idMenu;
        this.jumlah = jumlah;
    }

    public int getIdPengguna() { return idPengguna; }
    public int getIdMenu() { return idMenu; }
    public int getJumlah() { return jumlah; }

    public void setIdPengguna(int idPengguna) { this.idPengguna = idPengguna; }
    public void setIdMenu(int idMenu) { this.idMenu = idMenu; }
    public void setJumlah(int jumlah) { this.jumlah = jumlah; }
}