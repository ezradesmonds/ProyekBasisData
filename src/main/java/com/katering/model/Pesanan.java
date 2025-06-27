package com.katering.model;

public class Pesanan {
    private int id;
    private String namaPelanggan;
    private String status;

    public Pesanan(int id, String namaPelanggan, String status) {
        this.id = id;
        this.namaPelanggan = namaPelanggan;
        this.status = status;
    }

    public int getId() { return id; }
    public String getNamaPelanggan() { return namaPelanggan; }
    public String getStatus() { return status; }
}