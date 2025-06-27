package com.katering.model;

import javafx.beans.property.*;

public class Menu {
    private IntegerProperty id;
    private StringProperty nama;
    private StringProperty deskripsi;
    private IntegerProperty harga;
    private BooleanProperty tersedia;
    private IntegerProperty idCabang;

    public Menu(int id, String nama, String deskripsi, int harga, boolean tersedia, int idCabang) {
        this.id = new SimpleIntegerProperty(id);
        this.nama = new SimpleStringProperty(nama);
        this.deskripsi = new SimpleStringProperty(deskripsi);
        this.harga = new SimpleIntegerProperty(harga);
        this.tersedia = new SimpleBooleanProperty(tersedia);
        this.idCabang = new SimpleIntegerProperty(idCabang);
    }

    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }

    public String getNama() { return nama.get(); }
    public StringProperty namaProperty() { return nama; }

    public String getDeskripsi() { return deskripsi.get(); }
    public StringProperty deskripsiProperty() { return deskripsi; }

    public int getHarga() { return harga.get(); }
    public IntegerProperty hargaProperty() { return harga; }

    public boolean isTersedia() { return tersedia.get(); }
    public BooleanProperty tersediaProperty() { return tersedia; }

    public int getIdCabang() { return idCabang.get(); }
    public IntegerProperty idCabangProperty() { return idCabang; }
}
