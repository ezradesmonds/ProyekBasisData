package com.katering.model;

public class Cabang {
    private int idCabang;
    private String namaCabang;
    private String alamat;
    private String kota;

    // Constructor
    public Cabang(int idCabang, String namaCabang, String alamat, String kota) {
        this.idCabang = idCabang;
        this.namaCabang = namaCabang;
        this.alamat = alamat;
        this.kota = kota;
    }

    // Getters (diperlukan oleh controller)
    public int getIdCabang() {
        return idCabang;
    }

    public String getNamaCabang() {
        return namaCabang;
    }

    public String getAlamat() {
        return alamat;
    }

    public String getKota() {
        return kota;
    }

    // Setters (opsional, tapi baik untuk dimiliki)
    public void setIdCabang(int idCabang) {
        this.idCabang = idCabang;
    }

    public void setNamaCabang(String namaCabang) {
        this.namaCabang = namaCabang;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public void setKota(String kota) {
        this.kota = kota;
    }
}