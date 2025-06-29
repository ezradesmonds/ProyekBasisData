package com.katering.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDate;

public class Promo {
    private IntegerProperty idPromo;
    private StringProperty namaPromo;
    private StringProperty deskripsi; // Changed to StringProperty
    private IntegerProperty diskon;
    private LocalDate tglMulai;
    private LocalDate tglSelesai;

    public Promo(int idPromo, String namaPromo, String deskripsi, int diskon, LocalDate tglMulai, LocalDate tglSelesai) {
        this.idPromo = new SimpleIntegerProperty(idPromo);
        this.namaPromo = new SimpleStringProperty(namaPromo);
        this.deskripsi = new SimpleStringProperty(deskripsi); // Initialized as SimpleStringProperty
        this.diskon = new SimpleIntegerProperty(diskon);
        this.tglMulai = tglMulai;
        this.tglSelesai = tglSelesai;
    }

    public int getIdPromo() { return idPromo.get(); }
    public IntegerProperty idPromoProperty() { return idPromo; }
    public void setIdPromo(int idPromo) { this.idPromo.set(idPromo); }

    public String getNamaPromo() { return namaPromo.get(); }
    public StringProperty namaPromoProperty() { return namaPromo; }
    public void setNamaPromo(String namaPromo) { this.namaPromo.set(namaPromo); }

    public String getDeskripsi() { return deskripsi.get(); } // Returns String
    public StringProperty deskripsiProperty() { return deskripsi; } // New property method
    public void setDeskripsi(String deskripsi) { this.deskripsi.set(deskripsi); } // Uses .set()

    public int getDiskon() { return diskon.get(); }
    public IntegerProperty diskonProperty() { return diskon; }
    public void setDiskon(int diskon) { this.diskon.set(diskon); }

    public LocalDate getTglMulai() { return tglMulai; }
    public void setTglMulai(LocalDate tglMulai) { this.tglMulai = tglMulai; }

    public LocalDate getTglSelesai() { return tglSelesai; }
    public void setTglSelesai(LocalDate tglSelesai) { this.tglSelesai = tglSelesai; }
}