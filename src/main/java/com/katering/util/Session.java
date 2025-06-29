package com.katering.util;

public class Session {
    private static Session instance;
    private int idPengguna;
    private String nama;
    private String role;
    private Integer idCabang; // hanya untuk admin cabang

    private Session(int idPengguna, String nama, String role, Integer idCabang) {
        this.idPengguna = idPengguna;
        this.nama = nama;
        this.role = role;
        this.idCabang = idCabang;
    }

    public static void createSession(int idPengguna, String nama, String role, Integer idCabang) {
        instance = new Session(idPengguna, nama, role, idCabang);
    }

    public static Session getInstance() {
        return instance;
    }

    public int getIdPengguna() {
        return idPengguna;
    }

    public String getNama() {
        return nama;
    }

    public String getRole() {
        return role;
    }

    public Integer getIdCabang() {
        return idCabang;
    }

    public void clear() {
        instance = null;
    }
}