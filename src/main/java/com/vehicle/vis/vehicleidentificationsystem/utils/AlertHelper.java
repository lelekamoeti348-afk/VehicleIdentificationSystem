package com.vehicle.vis.vehicleidentificationsystem.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.StageStyle;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AlertHelper {

    public static void showInfo(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            styleAlert(alert);
            alert.showAndWait();
        });
    }

    public static void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            styleAlert(alert);
            alert.showAndWait();
        });
    }

    public static void showWarning(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            styleAlert(alert);
            alert.showAndWait();
        });
    }

    public static void showConfirmation(String title, String content, Runnable onConfirm) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            styleAlert(alert);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                onConfirm.run();
            }
        });
    }

    public static boolean showConfirmationAndWait(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        styleAlert(alert);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void styleAlert(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                AlertHelper.class.getResource("/css/style.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("card");
        alert.initStyle(StageStyle.UNDECORATED);
    }
}