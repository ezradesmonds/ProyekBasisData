package com.katering.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/proyekBDAKHIRrev1";
    private static final String USER = "postgres";
    private static final String PASS = "default"; // ganti sesuai config

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}