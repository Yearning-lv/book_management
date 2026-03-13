package com.studentms.ui.controller;

import com.studentms.ui.MainApp;
import com.studentms.ui.api.ApiClient;
import com.studentms.ui.api.ApiSession;
import com.studentms.ui.util.TextCleaner;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainController {
  private final ApiClient apiClient = new ApiClient();

  @FXML
  private Label userLabel;

  @FXML
  private Button logoutButton;

  @FXML
  private StackPane contentPane;

  @FXML
  private Button userMenuButton;

  @FXML
  private Button bookMenuButton;

  @FXML
  private Button copyMenuButton;

  @FXML
  private Button circulationMenuButton;

  @FXML
  private Button statMenuButton;

  @FXML
  private Label roleHintLabel;

  private final Map<String, Parent> viewCache = new HashMap<>();

  @FXML
  private void initialize() {
    if (userLabel != null) {
      userLabel.setText(buildUserBadgeText());
    }
    if (roleHintLabel != null) {
      roleHintLabel.setText(buildRoleHint());
    }
    applyRoleLayout();
    String role = ApiSession.getRole();
    if ("ADMIN".equalsIgnoreCase(role)) {
      showUserPanel();
    } else {
      showBookPanel();
    }
  }

  @FXML
  private void showUserPanel() {
    loadView("user", "/fxml/user-panel.fxml", userMenuButton);
  }

  @FXML
  private void showBookPanel() {
    loadView("book", "/fxml/book-panel.fxml", bookMenuButton);
  }

  @FXML
  private void showCopyPanel() {
    loadView("copy", "/fxml/copy-panel.fxml", copyMenuButton);
  }

  @FXML
  private void showCirculationPanel() {
    loadView("circulation", "/fxml/circulation-panel.fxml", circulationMenuButton);
  }

  @FXML
  private void showStatPanel() {
    loadView("stat", "/fxml/stat-panel.fxml", statMenuButton);
  }

  @FXML
  private void handleLogout() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("退出登录");
    alert.setHeaderText(null);
    alert.setContentText("确定要退出当前账号吗？");
    if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
      return;
    }

    if (logoutButton != null) {
      logoutButton.setDisable(true);
      logoutButton.setText("退出中...");
    }

    String token = ApiSession.getToken();
    Thread thread = new Thread(() -> {
      try {
        if (token != null && !token.isBlank()) {
          apiClient.post("/api/auth/logout", new com.google.gson.JsonObject(), token);
        }
      } catch (Exception ignored) {
        // Best effort logout: we still clear local session below.
      } finally {
        ApiSession.clear();
      }

      javafx.application.Platform.runLater(() -> {
        try {
          Stage stage = (Stage) contentPane.getScene().getWindow();
          MainApp.switchToLogin(stage);
        } catch (Exception ex) {
          if (logoutButton != null) {
            logoutButton.setDisable(false);
            logoutButton.setText("退出登录");
          }
          loadPlaceholder("返回登录页失败: " + describeLoadFailure(ex));
        }
      });
    });
    thread.setDaemon(true);
    thread.start();
  }

  private void loadView(String key, String fxml, Button activeButton) {
    try {
      Parent view = viewCache.get(key);
      if (view == null) {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxml));
        view = loader.load();
        viewCache.put(key, view);
      }
      contentPane.getChildren().setAll(view);
      playContentAnimation(view);
      setActiveMenu(activeButton);
    } catch (Exception ex) {
      loadPlaceholder("界面加载失败: " + describeLoadFailure(ex));
    }
  }

  private void playContentAnimation(Parent view) {
    view.setOpacity(0);
    view.setTranslateY(8);

    FadeTransition fade = new FadeTransition(Duration.millis(220), view);
    fade.setFromValue(0);
    fade.setToValue(1);

    TranslateTransition move = new TranslateTransition(Duration.millis(220), view);
    move.setFromY(8);
    move.setToY(0);

    new ParallelTransition(fade, move).play();
  }

  private void setActiveMenu(Button activeButton) {
    for (Button button : getMenuButtons()) {
      if (button == null) {
        continue;
      }
      button.getStyleClass().remove("menu-button-active");
    }
    if (activeButton != null && !activeButton.getStyleClass().contains("menu-button-active")) {
      activeButton.getStyleClass().add("menu-button-active");
    }
  }

  private Button[] getMenuButtons() {
    return new Button[]{
        userMenuButton,
        bookMenuButton,
        copyMenuButton,
        circulationMenuButton,
        statMenuButton
    };
  }

  private void loadPlaceholder(String message) {
    Label label = new Label(message);
    label.getStyleClass().add("main-subtitle");
    contentPane.getChildren().setAll(label);
  }

  private String describeLoadFailure(Throwable ex) {
    Throwable current = ex;
    while (current.getCause() != null) {
      current = current.getCause();
    }
    String message = safe(current.getMessage());
    return message.isBlank() ? ex.getClass().getSimpleName() : message;
  }

  private String buildUserBadgeText() {
    String name = safe(ApiSession.getName());
    String account = safe(ApiSession.getAccount());
    String roleLabel = toRoleLabel(ApiSession.getRole());

    String displayName = name;
    if (displayName.isBlank() || TextCleaner.looksCorrupted(displayName)) {
      displayName = account;
    }
    if (displayName.isBlank()) {
      displayName = "用户";
    }
    displayName = trimDisplayName(displayName);

    if (roleLabel.isBlank()) {
      return "欢迎，" + displayName;
    }
    return "欢迎，" + displayName + "（" + roleLabel + "）";
  }

  private String toRoleLabel(String role) {
    String value = safe(role).toUpperCase(Locale.ROOT);
    return switch (value) {
      case "ADMIN", "0", "管理员" -> "管理员";
      case "STAFF", "1", "工作人员" -> "工作人员";
      case "READER", "2", "读者" -> "读者";
      default -> safe(role);
    };
  }

  private String buildRoleHint() {
    return switch (safe(ApiSession.getRole()).toUpperCase(Locale.ROOT)) {
      case "ADMIN" -> "管理员负责账号、权限与全局配置";
      case "STAFF" -> "工作人员负责图书、馆藏与借阅业务";
      default -> "读者可检索图书、预约并查看个人借阅";
    };
  }

  private void applyRoleLayout() {
    String role = safe(ApiSession.getRole()).toUpperCase(Locale.ROOT);
    if ("ADMIN".equals(role)) {
      setVisible(userMenuButton, true);
      setVisible(bookMenuButton, false);
      setVisible(copyMenuButton, false);
      setVisible(circulationMenuButton, false);
      setVisible(statMenuButton, true);
      return;
    }
    if ("STAFF".equals(role)) {
      setVisible(userMenuButton, false);
      setVisible(bookMenuButton, true);
      setVisible(copyMenuButton, true);
      setVisible(circulationMenuButton, true);
      setVisible(statMenuButton, true);
      return;
    }
    setVisible(userMenuButton, false);
    setVisible(bookMenuButton, true);
    setVisible(copyMenuButton, false);
    setVisible(circulationMenuButton, true);
    setVisible(statMenuButton, true);
  }

  private void setVisible(Button button, boolean visible) {
    if (button == null) {
      return;
    }
    button.setVisible(visible);
    button.setManaged(visible);
  }

  private String trimDisplayName(String value) {
    String text = safe(value).trim();
    return text.length() > 12 ? text.substring(0, 12) + "..." : text;
  }

  private String safe(String value) {
    return value == null ? "" : value.trim();
  }
}
