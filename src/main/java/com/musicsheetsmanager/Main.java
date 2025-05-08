package com.musicsheetsmanager;

import com.musicsheetsmanager.model.UserRepository;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    private static UserRepository userRepository = new UserRepository();

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the main application view

        URL mainViewUrl = getClass().getResource("/com/musicsheetsmanager/fxml/MainView.fxml");
        FXMLLoader loader = new FXMLLoader(mainViewUrl);

        Parent root = loader.load();

        // Set up the scene
        Scene scene = new Scene(root, 800, 600);
        URL cssUrl = getClass().getResource("/com/musicsheetsmanager/css/styles.css");
        scene.getStylesheets().add(cssUrl.toExternalForm());

        // Configure and show the stage
        primaryStage.setTitle("Dash App - JavaFX Version");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Get the application-wide user repository
     */
    public static UserRepository getUserRepository() {
        return userRepository;
    }

    public static void main(String[] args) {
        launch(args);
    }
}