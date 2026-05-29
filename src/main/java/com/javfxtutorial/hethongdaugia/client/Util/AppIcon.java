package com.javfxtutorial.hethongdaugia.client.Util;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AppIcon {
  private static final Logger log = LoggerFactory.getLogger(AppIcon.class);
  private static final String DEFAULT_ICON_PATH =
      "/com/javfxtutorial/hethongdaugia/assets/TaskBar.png";
  private static final Map<String, Image> ICON_CACHE = new ConcurrentHashMap<>();

  private AppIcon() {}

  public static void apply(Stage stage) {
    apply(stage, DEFAULT_ICON_PATH);
  }

  public static void apply(Stage stage, String resourcePath) {
    if (stage == null) {
      return;
    }
    Image icon = loadIcon(resourcePath);
    if (icon != null) {
      stage.getIcons().setAll(icon);
    }
  }

  private static Image loadIcon(String resourcePath) {
    if (resourcePath == null || resourcePath.isBlank()) {
      return null;
    }
    URL resource = AppIcon.class.getResource(resourcePath);
    if (resource == null) {
      log.warn("Khong tim thay app icon tai: {}", resourcePath);
      return null;
    }
    return ICON_CACHE.computeIfAbsent(resource.toExternalForm(), Image::new);
  }
}
