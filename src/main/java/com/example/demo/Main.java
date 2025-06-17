package com.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Student Management System - Login");
        stage.setScene(scene);
        stage.setMaximized(true); // fullscreen maximized window
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

