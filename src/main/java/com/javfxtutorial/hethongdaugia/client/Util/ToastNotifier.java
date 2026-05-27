package com.javfxtutorial.hethongdaugia.client.Util;

import com.javfxtutorial.hethongdaugia.client.controller.NotificationToastController;
import com.javfxtutorial.hethongdaugia.client.controller.NotificationToastController.ToastType;

public final class ToastNotifier {
  private final NotificationToastController controller;

  private ToastNotifier(NotificationToastController controller) {
    this.controller = controller;
  }

  public static ToastNotifier of(NotificationToastController controller) {
    return new ToastNotifier(controller);
  }

  public void success(String message) {
    show(message, ToastType.SUCCESS);
  }

  public void error(String message) {
    show(message, ToastType.ERROR);
  }

  public void warning(String message) {
    show(message, ToastType.WARNING);
  }

  public void info(String message) {
    show(message, ToastType.INFO);
  }

  public void show(String message, ToastType type) {
    if (controller != null) {
      controller.show(message, type);
    }
  }
}
