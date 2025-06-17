package com.example.demo;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneLoader {

    private final Stage stage;

    public SceneLoader(Stage stage) {
        this.stage = stage;
    }

    /**
     * Loads the given FXML file and sets it as the current scene.
     * @param fxmlFileName the FXML file name located in the resources/com/example/demo/
     * @throws IOException if loading the FXML fails
     */
    public void loadScene(String fxmlFileName) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFileName));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
}
