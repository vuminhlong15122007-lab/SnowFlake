package com.javfxtutorial.hethongdaugia.client.controller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class NotificationToastController {

  private static final Duration FADE_DURATION = Duration.millis(120);

  public enum ToastType {
    SUCCESS("sf-toast-success", "✓", Duration.seconds(1)),
    ERROR("sf-toast-error", "×", Duration.seconds(1.5)),
    WARNING("sf-toast-warning", "!", Duration.seconds(1.5)),
    INFO("sf-toast-info", "i", Duration.seconds(1));

    private final String styleClass;
    private final String iconText;
    private final Duration displayDuration;

    ToastType(String styleClass, String iconText, Duration displayDuration) {
      this.styleClass = styleClass;
      this.iconText = iconText;
      this.displayDuration = displayDuration;
    }
  }

  @FXML private StackPane root;
  @FXML private Label notificationLabel;
  @FXML private Label statusIcon;
  @FXML private Rectangle progressBar;

  private FadeTransition fadeTransition;
  private Timeline progressTimeline;

  public void showSuccess(String message) {
    show(message, ToastType.SUCCESS);
  }

  public void showError(String message) {
    show(message, ToastType.ERROR);
  }

  public void showWarning(String message) {
    show(message, ToastType.WARNING);
  }

  public void showInfo(String message) {
    show(message, ToastType.INFO);
  }

  public void show(String message, ToastType type) {
    stopCurrentAnimation();

    notificationLabel.setText(message == null ? "" : message);
    statusIcon.setText(type.iconText);
    root.getStyleClass()
        .removeAll(
            ToastType.SUCCESS.styleClass,
            ToastType.ERROR.styleClass,
            ToastType.WARNING.styleClass,
            ToastType.INFO.styleClass);
    root.getStyleClass().add(type.styleClass);
    root.applyCss();

    double fromOpacity = root.isVisible() ? root.getOpacity() : 0;

    root.setVisible(true);
    root.toFront();

    fadeTransition = new FadeTransition(FADE_DURATION, root);
    fadeTransition.setFromValue(fromOpacity);
    fadeTransition.setToValue(1);
    fadeTransition.play();

    startProgressTimer(type.displayDuration);
  }

  @FXML
  public void hide() {
    if (root == null || !root.isVisible()) {
      return;
    }

    stopCurrentAnimation();

    fadeTransition = new FadeTransition(FADE_DURATION, root);
    fadeTransition.setFromValue(root.getOpacity());
    fadeTransition.setToValue(0);
    fadeTransition.setOnFinished(event -> root.setVisible(false));
    fadeTransition.play();
  }

  private void startProgressTimer(Duration displayDuration) {
    double progressWidth = root.getWidth() > 0 ? root.getWidth() : root.getPrefWidth();
    progressBar.setWidth(progressWidth);

    progressTimeline =
        new Timeline(
            new KeyFrame(
                Duration.ZERO,
                new KeyValue(progressBar.widthProperty(), progressWidth, Interpolator.LINEAR)),
            new KeyFrame(
                displayDuration,
                new KeyValue(progressBar.widthProperty(), 0, Interpolator.LINEAR)));
    progressTimeline.setOnFinished(event -> hide());
    progressTimeline.play();
  }

  private void stopCurrentAnimation() {
    if (progressTimeline != null) {
      progressTimeline.stop();
    }
    if (fadeTransition != null) {
      fadeTransition.stop();
    }
  }
}
