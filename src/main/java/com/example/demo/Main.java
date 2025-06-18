package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane; // Assuming login.fxml uses AnchorPane as root
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the login view as the initial screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml")); // Path to your login FXML
            AnchorPane root = loader.load(); // Assuming login.fxml's root element is an AnchorPane
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Student Management System - Login");
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load the login screen: " + e.getMessage());
            e.printStackTrace();
            // In a real application, you might show an error dialog here
        }
    }

    public static void main(String[] args) {
        // This is the entry point for your JavaFX application
        launch(args);
    }
}
