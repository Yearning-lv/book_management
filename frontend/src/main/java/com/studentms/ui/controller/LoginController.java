package com.studentms.ui.controller;

import com.google.gson.JsonObject;
import com.studentms.ui.MainApp;
import com.studentms.ui.api.ApiClient;
import com.studentms.ui.api.ApiSession;
import com.studentms.ui.util.UiHelper;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginController {

  private final ApiClient apiClient = new ApiClient();

  @FXML
  private TextField usernameField;

  @FXML
  private PasswordField passwordField;

  @FXML
  private Button loginButton;

  @FXML
  private RadioButton adminRole;

  @FXML
  private RadioButton staffRole;

  @FXML
  private RadioButton readerRole;

  @FXML
  private VBox brandBox;

  @FXML
  private VBox formBox;

  @FXML
  private void initialize() {
    if (brandBox == null || formBox == null) {
      return;
    }

    brandBox.setOpacity(0);
    formBox.setOpacity(0);

    FadeTransition brandFade = new FadeTransition(Duration.millis(500), brandBox);
    brandFade.setFromValue(0);
    brandFade.setToValue(1);

    TranslateTransition brandMove = new TranslateTransition(Duration.millis(500), brandBox);
    brandMove.setFromY(12);
    brandMove.setToY(0);

    FadeTransition formFade = new FadeTransition(Duration.millis(520), formBox);
    formFade.setFromValue(0);
    formFade.setToValue(1);

    TranslateTransition formMove = new TranslateTransition(Duration.millis(520), formBox);
    formMove.setFromY(12);
    formMove.setToY(0);

    ParallelTransition brandIn = new ParallelTransition(brandFade, brandMove);
    ParallelTransition formIn = new ParallelTransition(formFade, formMove);
    formIn.setDelay(Duration.millis(120));

    SequentialTransition sequence = new SequentialTransition(brandIn, formIn);
    sequence.play();
  }

  @FXML
  private void handleLogin() {
    String username = usernameField.getText();
    String password = passwordField.getText();
    String role = adminRole.isSelected() ? "管理员" : staffRole.isSelected() ? "工作人员" : "读者";

    if (username == null || username.isBlank() || password == null || password.isBlank()) {
      UiHelper.showWarning("提示", "请输入账号和密码");
      return;
    }

    if (loginButton != null) {
      loginButton.setDisable(true);
      loginButton.setText("登录中...");
    }

    UiHelper.runAsync(() -> {
        JsonObject payload = new JsonObject();
        JsonObject data = new JsonObject();
        data.addProperty("username", username);
        data.addProperty("password", password);
        data.addProperty("role", role);
        payload.add("data", data);

        JsonObject response = apiClient.post("/api/auth/login", payload, null);
        int code = response.get("code").getAsInt();
        if (code != 0) {
          String msg = response.has("msg") ? response.get("msg").getAsString() : "登录失败";
          throw new IllegalStateException(msg);
        }
        JsonObject respData = response.getAsJsonObject("data");
        ApiSession.setToken(respData.get("token").getAsString());
        ApiSession.setUserId(respData.get("userId").getAsInt());
        ApiSession.setAccount(respData.get("account").getAsString());
        ApiSession.setName(respData.get("name").getAsString());
        ApiSession.setRole(respData.get("role").getAsString());
        return true;
      }, ignored -> {
      if (loginButton != null) {
        loginButton.setDisable(false);
        loginButton.setText("登录系统");
      }
      switchToMainView();
    }, ex -> {
      if (loginButton != null) {
        loginButton.setDisable(false);
        loginButton.setText("登录系统");
      }
      String msg = ex == null ? "登录失败" : ex.getMessage();
      UiHelper.showError("登录失败", msg);
    });
  }

  private void switchToMainView() {
    try {
      Stage stage = (Stage) usernameField.getScene().getWindow();
      MainApp.switchToMain(stage, ApiSession.getRole());
    } catch (Exception ex) {
      UiHelper.showError("界面加载失败", ex.getMessage());
    }
  }
}
