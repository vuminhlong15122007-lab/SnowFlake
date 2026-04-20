package com.javfxtutorial.hethongdaugia.client.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TimeLeft {       // Class tái sd — truyền vào Label và end là chạy .
    private Timeline timeline;   // dong ho dem nguoc
    private final Label label;
    private final LocalDateTime end;
    private Runnable onFinished;     // Hd se chay khi thoi gian ket thuc

    public TimeLeft(Label label, LocalDateTime end) {
        this.label = label;
        this.end = end;
    }

    public void start() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        timeline.setCycleCount(Timeline.INDEFINITE);  //tgian dong ho dem nguoc chua xd
        tick();
        timeline.play();
    }

    public void stop() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    public void tick() {
        long second = LocalDateTime.now().until(end, ChronoUnit.SECONDS);

        long hours = second / 3600;
        long minute = (second % 3600) / 60;
        long secs = second % 60;
        label.setText(String.format("%02d:%02d:%02d", hours, minute, secs));

        if (second <= 0) {
            label.setText("00:00:00");
            label.setStyle("-fx-text-fill: #888888; -fx-font-weight: bold;");
            stop();
            if (onFinished != null) onFinished.run();    //neeus co hd can sau khi het gio, thi no se chay
        } else {
            label.setStyle("-fx-text-fill: #d9534f; -fx-font-weight: bold;");
        }
    }

    public void setOnFinished(Runnable onFinished) {   //CAN Chuyeen them  hdong sau khi het gio
        this.onFinished = onFinished;
    }

}