package com.katering.model;

public class User {
    private int id;
    private String nama;
    private String username;
    private String role;

    public User(int id, String nama, String username, String role) {
        this.id = id;
        this.nama = nama;
        this.username = username;
        this.role = role;
    }

    public int getId() { return id; }
    public String getNama() { return nama; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}