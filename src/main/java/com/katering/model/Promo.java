package com.katering.model;

public class Promo {
    private int idPromo;
    private String namaPromo;
    private String deskripsi;
    private int diskon;

    public Promo(int idPromo, String namaPromo, String deskripsi, int diskon) {
        this.idPromo = idPromo;
        this.namaPromo = namaPromo;
        this.deskripsi = deskripsi;
        this.diskon = diskon;
    }

    public int getIdPromo() { return idPromo; }
    public String getNamaPromo() { return namaPromo; }
    public String getDeskripsi() { return deskripsi; }
    public int getDiskon() { return diskon; }

    public void setIdPromo(int idPromo) { this.idPromo = idPromo; }
    public void setNamaPromo(String namaPromo) { this.namaPromo = namaPromo; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    public void setDiskon(int diskon) { this.diskon = diskon; }
}
