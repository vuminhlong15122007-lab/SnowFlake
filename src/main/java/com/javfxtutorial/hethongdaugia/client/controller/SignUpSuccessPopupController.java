package com.javfxtutorial.hethongdaugia.client.controller;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.Random;

public class SignUpSuccessPopupController {
    @FXML
    private StackPane rootPane;
    @FXML
    private Pane confettiLayer;

    private final Random random = new Random();
    private final String[] colors = {
            "#66ccff", "#b3c6ff", "#f4b8ff", "#ffcf5c", "#9be7c7", "#ff8cc6"
    };
    private int runningConfettiAnimations;
    private boolean burstFinished;

    @FXML
    private void initialize() {
        Platform.runLater(this::playConfettiBurst);
    }

    private void playConfettiBurst() {
        Timeline burstTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, event -> createConfettiWave()),
                new KeyFrame(Duration.millis(420), event -> createConfettiWave()),
                new KeyFrame(Duration.millis(840), event -> createConfettiWave())
        );
        burstTimeline.setOnFinished(event -> {
            burstFinished = true;
            closePopupIfConfettiFinished();
        });
        burstTimeline.play();
    }

    private void createConfettiWave() {
        for (int i = 0; i < 26; i++) {
            Node piece = createConfettiPiece();
            confettiLayer.getChildren().add(piece);
            animateConfetti(piece);
        }
    }

    private Node createConfettiPiece() {
        Color color = Color.web(colors[random.nextInt(colors.length)]);
        if (random.nextBoolean()) {
            Rectangle rectangle = new Rectangle(6 + random.nextInt(8), 10 + random.nextInt(12), color);
            rectangle.setArcHeight(3);
            rectangle.setArcWidth(3);
            return rectangle;
        }
        return new Circle(3 + random.nextInt(4), color);
    }

    private void animateConfetti(Node piece) {
        double startX = 230 + randomOffset(42);
        double startY = 90 + randomOffset(18);
        double endX = randomOffset(210);
        double endY = 120 + random.nextDouble() * 170;

        piece.setLayoutX(startX);
        piece.setLayoutY(startY);
        piece.setOpacity(0.95);

        TranslateTransition move = new TranslateTransition(Duration.millis(1450 + random.nextInt(850)), piece);
        move.setByX(endX);
        move.setByY(endY);

        RotateTransition spin = new RotateTransition(Duration.millis(900 + random.nextInt(900)), piece);
        spin.setByAngle(random.nextBoolean() ? 420 : -420);

        FadeTransition fade = new FadeTransition(Duration.millis(950), piece);
        fade.setDelay(Duration.millis(900));
        fade.setFromValue(0.95);
        fade.setToValue(0.0);

        ParallelTransition animation = new ParallelTransition(move, spin, fade);
        runningConfettiAnimations++;
        animation.setOnFinished(event -> {
            confettiLayer.getChildren().remove(piece);
            runningConfettiAnimations--;
            closePopupIfConfettiFinished();
        });
        animation.play();
    }

    private double randomOffset(double maxAbsValue) {
        return (random.nextDouble() * 2 - 1) * maxAbsValue;
    }

    private void closePopupIfConfettiFinished() {
        if (!burstFinished || runningConfettiAnimations > 0 || rootPane.getScene() == null) {
            return;
        }
        Window window = rootPane.getScene().getWindow();
        if (window != null) {
            window.hide();
        }
    }
}
