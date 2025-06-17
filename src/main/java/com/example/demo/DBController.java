package com.example.demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBController {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/student_management_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "@Uchiha007"; // ← replace with your real password

    /**
     * Establishes a connection to the MySQL database.
     * @return Connection object
     * @throws SQLException if database connection fails
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}
