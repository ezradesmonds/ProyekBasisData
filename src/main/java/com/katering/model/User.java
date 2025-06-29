package com.katering.model;

public class User {
    private int id;
    private String nama;
    private String username;
    private String role;
    private Integer idCabang;

    public User(int id, String nama, String username, String role, Integer idCabang) {
        this.id = id;
        this.nama = nama;
        this.username = username;
        this.role = role;
        this.idCabang = idCabang;
    }

    public int getId() { return id; }
    public String getNama() { return nama; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public Integer getIdCabang() { return idCabang; }
}