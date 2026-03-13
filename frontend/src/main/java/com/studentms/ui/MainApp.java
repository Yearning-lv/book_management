package com.studentms.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainApp extends Application {
  private static final double LOGIN_WIDTH = 1080;
  private static final double LOGIN_HEIGHT = 680;
  private static final double MAIN_WIDTH = 1460;
  private static final double MAIN_HEIGHT = 880;

  @Override
  public void start(Stage stage) {
    try {
      Scene scene = buildScene("/fxml/login-view.fxml", LOGIN_WIDTH, LOGIN_HEIGHT);
      stage.setScene(scene);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to load UI resources", ex);
    }
    stage.setTitle("图书管理系统");
    stage.setMinWidth(980);
    stage.setMinHeight(620);
    try (var iconStream = Objects.requireNonNull(
        MainApp.class.getResourceAsStream("/images/app-mark.png"),
        "Missing icon: /images/app-mark.png"
    )) {
      stage.getIcons().setAll(new Image(iconStream));
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to load app icon", ex);
    }
    stage.show();
  }

  public static void switchToLogin(Stage stage) throws IOException {
    stage.setScene(buildScene("/fxml/login-view.fxml", LOGIN_WIDTH, LOGIN_HEIGHT));
    stage.setMinWidth(980);
    stage.setMinHeight(620);
    stage.centerOnScreen();
  }

  public static void switchToMain(Stage stage, String role) throws IOException {
    stage.setScene(buildScene("/fxml/main-view.fxml", MAIN_WIDTH, MAIN_HEIGHT));
    stage.setMinWidth(1280);
    stage.setMinHeight(820);
    stage.centerOnScreen();
  }

  public static Scene buildScene(String fxmlPath, double width, double height) throws IOException {
    var fxmlUrl = Objects.requireNonNull(
        MainApp.class.getResource(fxmlPath),
        "Missing FXML: " + fxmlPath
    );
    var cssUrl = Objects.requireNonNull(
        MainApp.class.getResource("/styles/app.css"),
        "Missing CSS: /styles/app.css"
    );
    FXMLLoader loader = new FXMLLoader(fxmlUrl);
    Scene scene = new Scene(loader.load(), width, height);
    scene.getStylesheets().add(cssUrl.toExternalForm());
    return scene;
  }

  public static void main(String[] args) {
    launch(args);
  }
}
