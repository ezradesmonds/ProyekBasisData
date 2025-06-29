// Menu.java
package com.katering.model;

import javafx.beans.property.*;

public class Menu {
    private IntegerProperty id;
    private StringProperty nama_menu;
    private StringProperty deskripsi;
    private IntegerProperty harga;
    private StringProperty kategori;
    private IntegerProperty stok;
    private BooleanProperty tersedia;
    private IntegerProperty idCabang;

    public Menu(int id, String nama, String deskripsi, int harga, String kategori, int stok, boolean tersedia, int idCabang) {
        this.id = new SimpleIntegerProperty(id);
        this.nama_menu = new SimpleStringProperty(nama);
        this.deskripsi = new SimpleStringProperty(deskripsi);
        this.harga = new SimpleIntegerProperty(harga);
        this.kategori = new SimpleStringProperty(kategori);
        this.stok = new SimpleIntegerProperty(stok);
        this.tersedia = new SimpleBooleanProperty(tersedia);
        this.idCabang = new SimpleIntegerProperty(idCabang);
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public void setId(int id) { this.id.set(id); }

    public String getNama() { return nama_menu.get(); }
    public StringProperty namaProperty() { return nama_menu; }
    public void setNama(String nama) { this.nama_menu.set(nama); }

    public String getDeskripsi() { return deskripsi.get(); }
    public StringProperty deskripsiProperty() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi.set(deskripsi); }

    public int getHarga() { return harga.get(); }
    public IntegerProperty hargaProperty() { return harga; }
    public void setHarga(int harga) { this.harga.set(harga); }

    public String getKategori() { return kategori.get(); }
    public StringProperty kategoriProperty() { return kategori; }
    public void setKategori(String kategori) { this.kategori.set(kategori); }

    public int getStok() { return stok.get(); }
    public IntegerProperty stokProperty() { return stok; }
    public void setStok(int stok) { this.stok.set(stok); }

    public boolean isTersedia() { return tersedia.get(); }
    public BooleanProperty tersediaProperty() { return tersedia; }
    public void setTersedia(boolean tersedia) { this.tersedia.set(tersedia); }

    public int getIdCabang() { return idCabang.get(); }
    public IntegerProperty idCabangProperty() { return idCabang; }
    public void setIdCabang(int idCabang) { this.idCabang.set(idCabang); }
}