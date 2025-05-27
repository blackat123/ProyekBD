package com.example.proyekbasisdata;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class HelloApplication extends Application {
    private static HelloApplication applicationInstance;
    private int userId;
    private Stage primaryStage;
    public static HelloApplication getApplicationInstance() {
        return applicationInstance;
    }
    public Stage getPrimaryStage () { return primaryStage; }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public int getUserId() {
        return this.userId;
    }

    @Override
    public void start(Stage stage) throws IOException {
        HelloApplication.applicationInstance = this;

        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-page.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        primaryStage = stage;
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}