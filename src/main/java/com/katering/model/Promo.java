package com.katering.model;

import java.time.LocalDate;

public class Promo {
    private int idPromo;
    private String namaPromo;
    private String deskripsi;
    private int diskon;
    private LocalDate tglMulai;
    private LocalDate tglSelesai;
    private Integer targetCabangId;
    private String targetKategori;
    private String namaCabang; // Untuk tampilan

    public Promo(int idPromo, String namaPromo, String deskripsi, int diskon, LocalDate tglMulai, LocalDate tglSelesai, Integer targetCabangId, String targetKategori, String namaCabang) {
        this.idPromo = idPromo;
        this.namaPromo = namaPromo;
        this.deskripsi = deskripsi;
        this.diskon = diskon;
        this.tglMulai = tglMulai;
        this.tglSelesai = tglSelesai;
        this.targetCabangId = targetCabangId;
        this.targetKategori = targetKategori;
        this.namaCabang = namaCabang;
    }

    // --- GETTERS ---
    public int getIdPromo() { return idPromo; }
    public String getNamaPromo() { return namaPromo; }
    public String getDeskripsi() { return deskripsi; }
    public int getDiskon() { return diskon; }
    public LocalDate getTglMulai() { return tglMulai; }
    public LocalDate getTglSelesai() { return tglSelesai; }
    public Integer getTargetCabangId() { return targetCabangId; }
    public String getTargetKategori() { return targetKategori; }

    // Properti buatan untuk ditampilkan di tabel
    public String getTarget() {
        if (targetCabangId != null && targetCabangId > 0) {
            return "Cabang: " + namaCabang;
        }
        if (targetKategori != null && !targetKategori.isEmpty()) {
            return "Kategori: " + targetKategori;
        }
        return "Semua";
    }
}