package com.vehicle.vis.vehicleidentificationsystem.utils;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class AnimationUtils {

    // Fade in animation
    public static void fadeIn(Node node, double durationSeconds) {
        FadeTransition fade = new FadeTransition(Duration.seconds(durationSeconds), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    // Fade out animation
    public static void fadeOut(Node node, double durationSeconds) {
        FadeTransition fade = new FadeTransition(Duration.seconds(durationSeconds), node);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.play();
    }

    // Continuous fade in/out for button (Pulse effect)
    public static void applyPulseAnimation(Node node) {
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), node);
        fadeIn.setFromValue(0.5);
        fadeIn.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), node);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0.5);

        SequentialTransition pulse = new SequentialTransition(node, fadeOut, fadeIn);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();
    }

    // Shake animation for error feedback
    public static void shakeField(Node node) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), node);
        shake.setFromX(0);
        shake.setToX(10);
        shake.setAutoReverse(true);
        shake.setCycleCount(6);
        shake.play();
    }

    // Slide in from left
    public static void slideInFromLeft(Node node, double durationSeconds) {
        TranslateTransition slide = new TranslateTransition(Duration.seconds(durationSeconds), node);
        slide.setFromX(-node.getBoundsInParent().getWidth());
        slide.setToX(0);
        slide.play();
    }

    // Scale animation on hover
    public static void addHoverScaleEffect(Node node) {
        node.setOnMouseEntered(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), node);
            scale.setToX(1.05);
            scale.setToY(1.05);
            scale.play();

            DropShadow shadow = new DropShadow();
            shadow.setRadius(15);
            shadow.setColor(Color.rgb(102, 126, 234, 0.5));
            node.setEffect(shadow);
        });

        node.setOnMouseExited(e -> {
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), node);
            scale.setToX(1);
            scale.setToY(1);
            scale.play();
            node.setEffect(null);
        });
    }

    // Rotate animation
    public static void rotate(Node node, double durationSeconds, int cycles) {
        RotateTransition rotate = new RotateTransition(Duration.seconds(durationSeconds), node);
        rotate.setByAngle(360);
        rotate.setCycleCount(cycles);
        rotate.play();
    }
}