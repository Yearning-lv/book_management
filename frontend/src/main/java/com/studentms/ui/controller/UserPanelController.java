package com.studentms.ui.controller;

import com.google.gson.JsonObject;
import com.studentms.ui.api.ApiClient;
import com.studentms.ui.api.ApiSession;
import com.studentms.ui.model.NamedOption;
import com.studentms.ui.util.JsonHelper;
import com.studentms.ui.util.StatusTextHelper;
import com.studentms.ui.util.TableViewHelper;
import com.studentms.ui.util.UiHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.Map;

public class UserPanelController {
  private final ApiClient apiClient = new ApiClient();
  private final ObservableList<Map<String, Object>> userItems = FXCollections.observableArrayList();
  private Integer currentUserId;

  @FXML
  private TextField keywordField;

  @FXML
  private ComboBox<NamedOption> roleFilterBox;

  @FXML
  private TableView<Map<String, Object>> userTable;

  @FXML
  private TableColumn<Map<String, Object>, String> accountColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> nameColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> roleColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> statusColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> phoneColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> fineColumn;

  @FXML
  private TextField accountField;

  @FXML
  private TextField nameField;

  @FXML
  private TextField passwordField;

  @FXML
  private ComboBox<NamedOption> roleBox;

  @FXML
  private ComboBox<String> statusBox;

  @FXML
  private TextField departmentField;

  @FXML
  private TextField phoneField;

  @FXML
  private TextField emailField;

  @FXML
  private VBox readerSection;

  @FXML
  private TextField readerNoField;

  @FXML
  private TextField maxBorrowCountField;

  @FXML
  private ComboBox<String> cardStatusBox;

  @FXML
  private TextField gradeField;

  @FXML
  private TextArea noteArea;

  @FXML
  private Label currentBorrowLabel;

  @FXML
  private Label fineBalanceLabel;

  @FXML
  private TextField payAmountField;

  @FXML
  private Button deleteButton;

  @FXML
  private Button payFineButton;

  @FXML
  private void initialize() {
    initOptions();
    initTable();
    clearForm();
    userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> populateForm(newValue));
    roleBox.valueProperty().addListener((obs, oldValue, newValue) -> updateReaderSection());
    loadUsers();
  }

  @FXML
  private void handleSearch() {
    loadUsers();
  }

  @FXML
  private void handleResetFilter() {
    keywordField.clear();
    roleFilterBox.getSelectionModel().selectFirst();
    loadUsers();
  }

  @FXML
  private void handleNew() {
    clearForm();
    userTable.getSelectionModel().clearSelection();
  }

  @FXML
  private void handleSave() {
    if (accountField.getText().isBlank() || nameField.getText().isBlank() || roleBox.getValue() == null) {
      UiHelper.showWarning("提示", "账号、姓名和角色不能为空");
      return;
    }
    JsonObject data = new JsonObject();
    addInt(data, "userId", currentUserId);
    addText(data, "account", accountField.getText());
    addText(data, "name", nameField.getText());
    addText(data, "password", passwordField.getText());
    addText(data, "roleCode", roleBox.getValue() == null ? null : roleBox.getValue().getValue());
    addText(data, "status", statusBox.getValue());
    addText(data, "department", departmentField.getText());
    addText(data, "phone", phoneField.getText());
    addText(data, "email", emailField.getText());
    if (isReaderSelected()) {
      addText(data, "readerNo", readerNoField.getText());
      addText(data, "maxBorrowCount", maxBorrowCountField.getText());
      addText(data, "cardStatus", cardStatusBox.getValue());
      addText(data, "grade", gradeField.getText());
      addText(data, "note", noteArea.getText());
    }
    runRequest("/api/user/saveUser", data, "保存成功", true);
  }

  @FXML
  private void handleDelete() {
    Map<String, Object> selected = userTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      UiHelper.showWarning("提示", "请先选择一条账户记录");
      return;
    }
    if ("管理员".equals(UiHelper.safeText(selected.get("roleName")))) {
      UiHelper.showWarning("提示", "管理员账号不允许在此删除");
      return;
    }
    JsonObject data = new JsonObject();
    addInt(data, "userId", intValue(selected.get("userId")));
    runRequest("/api/user/deleteUser", data, "删除成功", true);
  }

  @FXML
  private void handlePayFine() {
    if (currentUserId == null || !isReaderSelected()) {
      UiHelper.showWarning("提示", "请选择读者账户后再执行罚金结算");
      return;
    }
    if (payAmountField.getText().isBlank()) {
      UiHelper.showWarning("提示", "请输入本次缴费金额");
      return;
    }
    JsonObject data = new JsonObject();
    addInt(data, "userId", currentUserId);
    addText(data, "amount", payAmountField.getText());
    runRequest("/api/user/payFine", data, "罚金结算成功", true);
  }

  private void initOptions() {
    roleFilterBox.setItems(FXCollections.observableArrayList(
        new NamedOption("", "全部角色", null),
        new NamedOption("1", "工作人员", null),
        new NamedOption("2", "读者", null)
    ));
    roleFilterBox.getSelectionModel().selectFirst();

    roleBox.setItems(FXCollections.observableArrayList(
        new NamedOption("1", "工作人员", null),
        new NamedOption("2", "读者", null)
    ));
    roleBox.getSelectionModel().selectFirst();

    statusBox.setItems(FXCollections.observableArrayList("ACTIVE", "FROZEN"));
    statusBox.getSelectionModel().selectFirst();
    StatusTextHelper.applyTo(statusBox);

    cardStatusBox.setItems(FXCollections.observableArrayList("NORMAL", "PAUSED"));
    cardStatusBox.getSelectionModel().selectFirst();
    StatusTextHelper.applyTo(cardStatusBox);
  }

  private void initTable() {
    TableViewHelper.bindTextColumn(accountColumn, "account");
    TableViewHelper.bindTextColumn(nameColumn, "name");
    TableViewHelper.bindTextColumn(roleColumn, "roleName");
    TableViewHelper.bindTextColumn(statusColumn, "status", StatusTextHelper.mapper());
    TableViewHelper.bindTextColumn(phoneColumn, "phone");
    TableViewHelper.bindTextColumn(fineColumn, "fineBalance");
    userTable.setItems(userItems);
  }

  private void loadUsers() {
    JsonObject data = new JsonObject();
    addText(data, "keyword", keywordField.getText());
    NamedOption roleFilter = roleFilterBox.getValue();
    if (roleFilter != null && roleFilter.getValue() != null && !roleFilter.getValue().isBlank()) {
      data.addProperty("roleCode", roleFilter.getValue());
    }
    UiHelper.runAsync(() -> {
      JsonObject response = post("/api/user/getUserList", data);
      return JsonHelper.toList(response.getAsJsonArray("data"));
    }, items -> {
      userItems.setAll(items);
      if (!userItems.isEmpty()) {
        userTable.getSelectionModel().selectFirst();
      }
    }, ex -> UiHelper.showError("账户加载失败", ex == null ? "未知错误" : ex.getMessage()));
  }

  private void populateForm(Map<String, Object> user) {
    if (user == null) {
      clearForm();
      return;
    }
    currentUserId = intValue(user.get("userId"));
    accountField.setText(UiHelper.safeText(user.get("account")));
    nameField.setText(UiHelper.safeText(user.get("name")));
    passwordField.clear();
    selectRole(UiHelper.safeText(user.get("roleCode")));
    statusBox.setValue(UiHelper.safeText(user.get("status")));
    departmentField.setText(UiHelper.safeText(user.get("department")));
    phoneField.setText(UiHelper.safeText(user.get("phone")));
    emailField.setText(UiHelper.safeText(user.get("email")));
    readerNoField.setText(UiHelper.safeText(user.get("readerNo")));
    maxBorrowCountField.setText(UiHelper.safeText(user.get("maxBorrowCount")));
    cardStatusBox.setValue(UiHelper.safeText(user.get("cardStatus")).isBlank() ? "NORMAL" : UiHelper.safeText(user.get("cardStatus")));
    gradeField.setText(UiHelper.safeText(user.get("grade")));
    noteArea.setText(UiHelper.safeText(user.get("note")));
    currentBorrowLabel.setText(UiHelper.safeText(user.get("currentBorrowCount")));
    fineBalanceLabel.setText(UiHelper.safeText(user.get("fineBalance")).isBlank() ? "0" : UiHelper.safeText(user.get("fineBalance")));
    payAmountField.clear();
    updateReaderSection();
  }

  private void clearForm() {
    currentUserId = null;
    accountField.clear();
    nameField.clear();
    passwordField.clear();
    roleBox.getSelectionModel().selectFirst();
    statusBox.getSelectionModel().selectFirst();
    departmentField.clear();
    phoneField.clear();
    emailField.clear();
    readerNoField.clear();
    maxBorrowCountField.setText("8");
    cardStatusBox.getSelectionModel().selectFirst();
    gradeField.clear();
    noteArea.clear();
    currentBorrowLabel.setText("0");
    fineBalanceLabel.setText("0");
    payAmountField.clear();
    updateReaderSection();
  }

  private void updateReaderSection() {
    boolean reader = isReaderSelected();
    readerSection.setDisable(!reader);
    payFineButton.setDisable(!reader || currentUserId == null);
  }

  private boolean isReaderSelected() {
    return roleBox.getValue() != null && "2".equals(roleBox.getValue().getValue());
  }

  private void selectRole(String roleCode) {
    for (NamedOption option : roleBox.getItems()) {
      if (option.getValue().equals(roleCode)) {
        roleBox.setValue(option);
        return;
      }
    }
    roleBox.getSelectionModel().selectFirst();
  }

  private void runRequest(String path, JsonObject data, String successMessage, boolean reload) {
    UiHelper.runAsync(() -> {
      JsonObject response = post(path, data);
      return JsonHelper.object(response, "data");
    }, result -> {
      UiHelper.showInfo("操作成功", successMessage);
      if (reload) {
        loadUsers();
      }
    }, ex -> UiHelper.showError("操作失败", ex == null ? "未知错误" : ex.getMessage()));
  }

  private JsonObject post(String path, JsonObject data) throws Exception {
    JsonObject payload = new JsonObject();
    payload.add("data", data == null ? new JsonObject() : data);
    JsonObject response = apiClient.post(path, payload, ApiSession.getToken());
    int code = response.get("code").getAsInt();
    if (code != 0) {
      throw new IllegalStateException(UiHelper.safeText(JsonHelper.getString(response, "msg")));
    }
    return response;
  }

  private void addText(JsonObject target, String key, String value) {
    if (value != null && !value.isBlank()) {
      target.addProperty(key, value.trim());
    }
  }

  private void addInt(JsonObject target, String key, Integer value) {
    if (value != null) {
      target.addProperty(key, value);
    }
  }

  private Integer intValue(Object value) {
    if (value == null) {
      return null;
    }
    try {
      return Integer.valueOf(String.valueOf(value));
    } catch (NumberFormatException ex) {
      return null;
    }
  }
}
