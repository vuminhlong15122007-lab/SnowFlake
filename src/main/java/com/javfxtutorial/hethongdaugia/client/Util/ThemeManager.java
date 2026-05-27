package com.javfxtutorial.hethongdaugia.client.Util;

import java.net.URL;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Window;

public final class ThemeManager {
  private static final String GLOBAL_CSS = "/com/javfxtutorial/hethongdaugia/view/css/global.css";
  private static final String DARK_CSS = "/com/javfxtutorial/hethongdaugia/view/css/darkmode.css";

  private static final String THEME_PROPERTY = "snowfox.theme";
  private static final String THEME_ENV = "SNOWFOX_THEME";
  private static final String SWITCHER_ID = "themeModeSwitcher";
  private static final String SWITCHER_SHELL_ID = "themeModeSwitcherShell";
  private static ColorMode currentMode = resolveInitialMode();

  private ThemeManager() {}

  public static void apply(Scene scene) {
    if (scene == null) {
      return;
    }
    applyStyles(scene.getStylesheets());
    if (scene.getRoot() instanceof Parent root) {
      apply(root);
    }
    installThemeSwitcher(scene);
  }

  public static void apply(Parent root) {
    if (root == null) {
      return;
    }
    applyStyles(root.getStylesheets());
  }

  private static void applyStyles(List<String> stylesheets) {
    addIfMissing(stylesheets, GLOBAL_CSS);
    removeThemeStyles(stylesheets);
    String themeCss = currentMode.stylesheet();
    if (themeCss != null) {
      addIfMissing(stylesheets, themeCss);
    }
  }

  private static void removeThemeStyles(List<String> stylesheets) {
    removeIfPresent(stylesheets, DARK_CSS);
  }

  private static void removeIfPresent(List<String> stylesheets, String resourcePath) {
    String stylesheet = css(resourcePath);
    if (stylesheet != null) {
      stylesheets.remove(stylesheet);
    }
    String resourceSuffix = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
    stylesheets.removeIf(
        existing -> existing != null && existing.replace('\\', '/').endsWith(resourceSuffix));
  }

  private static void addIfMissing(List<String> stylesheets, String resourcePath) {
    String stylesheet = css(resourcePath);
    if (stylesheet != null && !stylesheets.contains(stylesheet)) {
      stylesheets.add(stylesheet);
    }
  }

  private static String css(String resourcePath) {
    URL resource = ThemeManager.class.getResource(resourcePath);
    return resource == null ? null : resource.toExternalForm();
  }

  private static ColorMode resolveInitialMode() {
    String theme = System.getProperty(THEME_PROPERTY);
    if (theme == null || theme.isBlank()) {
      theme = System.getenv(THEME_ENV);
    }
    return ColorMode.from(theme);
  }

  private static void installThemeSwitcher(Scene scene) {
    if (scene == null
        || !(scene.getRoot() instanceof Parent root)
        || !isApplicationScreenRoot(root)) {
      return;
    }

    Button existing = findSwitcher(root);
    if (existing != null) {
      updateSwitcher(existing);
      return;
    }

    Button switcher = createSwitcher();
    if (root instanceof StackPane stackPane) {
      addSwitcherOverlay(stackPane, switcher);
    } else if (root instanceof BorderPane borderPane) {
      StackPane wrapper = new StackPane(borderPane);
      wrapper.setPrefSize(borderPane.getPrefWidth(), borderPane.getPrefHeight());
      scene.setRoot(wrapper);
      addSwitcherOverlay(wrapper, switcher);
    }
  }

  private static void addSwitcherOverlay(StackPane stackPane, Button switcher) {
    HBox shell = new HBox(switcher);
    shell.setId(SWITCHER_SHELL_ID);
    shell.getStyleClass().add("theme-switcher-shell");
    shell.setAlignment(Pos.BOTTOM_RIGHT);
    shell.setPickOnBounds(false);

    shell.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    shell.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

    StackPane.setAlignment(shell, Pos.BOTTOM_RIGHT);
    StackPane.setMargin(shell, new Insets(0, 18, 18, 0));
    stackPane.getChildren().add(shell);
  }

  private static boolean isApplicationScreenRoot(Parent root) {
    if (root instanceof BorderPane) {
      return true;
    }
    return root instanceof StackPane && root.prefWidth(-1) >= 900 && root.prefHeight(-1) >= 500;
  }

  private static Button createSwitcher() {
    Button switcher = new Button();
    switcher.setId(SWITCHER_ID);
    switcher.setMnemonicParsing(false);
    switcher.setFocusTraversable(false);
    switcher.setMinWidth(28);
    switcher.setPrefWidth(28);
    switcher.setMaxWidth(28);
    switcher.setMinHeight(28);
    switcher.setPrefHeight(28);
    switcher.setMaxHeight(28);
    switcher.getStyleClass().add("theme-switcher");
    switcher.setTooltip(new Tooltip("Đổi chế độ màu: Sáng / Tối"));
    switcher.setOnAction(
        _ -> {
          currentMode = currentMode.next();
          System.setProperty(THEME_PROPERTY, currentMode.id());
          applyOpenWindows();
        });
    updateSwitcher(switcher);
    return switcher;
  }

  private static void updateSwitcher(Button switcher) {
    switcher.setText("");
    switcher.setTooltip(new Tooltip("Color mode: " + currentMode.label()));
    switcher.getStyleClass().removeAll(ColorMode.styleClasses());
    switcher.getStyleClass().add(currentMode.styleClass());
  }

  private static void applyOpenWindows() {
    for (Window window : Window.getWindows()) {
      Scene scene = window.getScene();
      if (scene != null) {
        apply(scene);
      }
    }
  }

  private static void ensureFlexibleSpacer(HBox topBar) {
    for (Node child : topBar.getChildren()) {
      if (child instanceof Region && HBox.getHgrow(child) == Priority.ALWAYS) {
        return;
      }
    }
    for (int i = topBar.getChildren().size() - 1; i >= 0; i--) {
      Node child = topBar.getChildren().get(i);
      if (child instanceof Region region) {
        HBox.setHgrow(region, Priority.ALWAYS);
        region.setMaxWidth(Double.MAX_VALUE);
        return;
      }
    }
  }

  private static Button findSwitcher(Parent root) {
    Node node = findById(root, SWITCHER_ID);
    return node instanceof Button button ? button : null;
  }

  private static Node findById(Node node, String id) {
    if (id.equals(node.getId())) {
      return node;
    }
    if (node instanceof Parent parent) {
      for (Node child : parent.getChildrenUnmodifiable()) {
        Node match = findById(child, id);
        if (match != null) {
          return match;
        }
      }
    }
    return null;
  }

  private enum ColorMode {
    LIGHT("light", "Sáng", null, "theme-light"),
    DARK("dark", "Tối", DARK_CSS, "theme-dark");

    private final String id;
    private final String label;
    private final String stylesheet;
    private final String styleClass;

    ColorMode(String id, String label, String stylesheet, String styleClass) {
      this.id = id;
      this.label = label;
      this.stylesheet = stylesheet;
      this.styleClass = styleClass;
    }

    private String id() {
      return id;
    }

    private String label() {
      return label;
    }

    private String stylesheet() {
      return stylesheet;
    }

    private String styleClass() {
      return styleClass;
    }

    private ColorMode next() {
      ColorMode[] modes = values();
      return modes[(ordinal() + 1) % modes.length];
    }

    private static ColorMode from(String raw) {
      if (raw == null || raw.isBlank()) {
        return LIGHT;
      }
      if ("true".equalsIgnoreCase(raw)) {
        return DARK;
      }
      String normalized = raw.trim().toLowerCase();
      if (normalized.equals("darkmode")) {
        return DARK;
      }
      for (ColorMode mode : values()) {
        if (mode.id.equalsIgnoreCase(raw) || mode.label.equalsIgnoreCase(raw)) {
          return mode;
        }
      }
      return LIGHT;
    }

    private static List<String> styleClasses() {
      return List.of(LIGHT.styleClass, DARK.styleClass);
    }
  }
}
