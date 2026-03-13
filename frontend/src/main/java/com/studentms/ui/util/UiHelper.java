package com.studentms.ui.util;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public final class UiHelper {
  private UiHelper() {
  }

  public static <T> void runAsync(Callable<T> action, Consumer<T> onSuccess, Consumer<Throwable> onError) {
    Task<T> task = new Task<>() {
      @Override
      protected T call() throws Exception {
        return action.call();
      }
    };
    task.setOnSucceeded(event -> onSuccess.accept(task.getValue()));
    task.setOnFailed(event -> onError.accept(task.getException()));
    Thread thread = new Thread(task);
    thread.setDaemon(true);
    thread.start();
  }

  public static void showInfo(String title, String content) {
    showAlert(Alert.AlertType.INFORMATION, title, content);
  }

  public static void showWarning(String title, String content) {
    showAlert(Alert.AlertType.WARNING, title, content);
  }

  public static void showError(String title, String content) {
    showAlert(Alert.AlertType.ERROR, title, content);
  }

  public static String safeText(Object value) {
    return value == null ? "" : String.valueOf(value);
  }

  private static void showAlert(Alert.AlertType type, String title, String content) {
    Platform.runLater(() -> {
      Alert alert = new Alert(type);
      alert.setTitle(title);
      alert.setHeaderText(null);
      alert.setContentText(content);
      alert.showAndWait();
    });
  }
}
