package com.vehicle.vis.vehicleidentificationsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApplication extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        stage.setTitle("Vehicle Identification System");
        stage.setScene(scene);
        stage.setMinWidth(1024);
        stage.setMinHeight(768);
        stage.show();
    }

    public static void changeScene(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource(fxml));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(MainApplication.class.getResource("/css/style.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}