package com.example.demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBController {

    // Database connection parameters
    private static final String URL = "jdbc:mysql://localhost:3306/student_management_system"; // Ensure 'student_management_system' matches your MySQL database name
    private static final String USER = "root"; // Your MySQL username
    private static final String PASSWORD = "@Uchiha007"; // Your MySQL password

    /**
     * Establishes and returns a connection to the MySQL database.
     * This method is called by DAOs to get a new connection for each database operation,
     * ensuring proper resource management using try-with-resources in DAOs.
     *
     * @return A valid database Connection object.
     * @throws SQLException If a database access error occurs or the JDBC driver is not found.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // Register the JDBC driver. This is typically not explicitly needed for JDBC 4.0+
            // as drivers are automatically discovered, but it's good practice for clarity.
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // If the driver is not found, throw a SQLException to indicate the problem.
            throw new SQLException("MySQL JDBC Driver not found. Ensure 'mysql-connector-java' is in your classpath.", e);
        }
        // Establish the connection to the database
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * A simple main method for testing the database connection.
     * You can run this method directly to verify your database setup.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        System.out.println("Attempting to connect to the database...");
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("Database connection successful!");
                System.out.println("Connected to: " + conn.getMetaData().getURL());
            } else {
                System.err.println("Failed to get database connection.");
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

