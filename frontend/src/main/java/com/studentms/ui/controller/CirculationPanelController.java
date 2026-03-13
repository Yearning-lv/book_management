package com.studentms.ui.controller;

import com.google.gson.JsonObject;
import com.studentms.ui.api.ApiClient;
import com.studentms.ui.api.ApiSession;
import com.studentms.ui.model.NamedOption;
import com.studentms.ui.util.DateValueHelper;
import com.studentms.ui.util.JsonHelper;
import com.studentms.ui.util.StatusTextHelper;
import com.studentms.ui.util.TableViewHelper;
import com.studentms.ui.util.UiHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.Map;

public class CirculationPanelController {
  private final ApiClient apiClient = new ApiClient();
  private final ObservableList<Map<String, Object>> borrowItems = FXCollections.observableArrayList();
  private final ObservableList<Map<String, Object>> reservationItems = FXCollections.observableArrayList();

  @FXML
  private TextField borrowKeywordField;

  @FXML
  private ComboBox<String> borrowStatusBox;

  @FXML
  private TableView<Map<String, Object>> borrowTable;

  @FXML
  private TableColumn<Map<String, Object>, String> borrowTitleColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> borrowReaderColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> borrowBarcodeColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> borrowStatusColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> borrowDateColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> dueDateColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> fineAmountColumn;

  @FXML
  private VBox borrowActionPane;

  @FXML
  private ComboBox<NamedOption> readerBox;

  @FXML
  private ComboBox<NamedOption> copyBox;

  @FXML
  private DatePicker borrowDatePicker;

  @FXML
  private TextArea borrowRemarkArea;

  @FXML
  private Button borrowButton;

  @FXML
  private Button renewButton;

  @FXML
  private Button returnButton;

  @FXML
  private ComboBox<String> reservationStatusBox;

  @FXML
  private TableView<Map<String, Object>> reservationTable;

  @FXML
  private TableColumn<Map<String, Object>, String> reserveTitleColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> reserveReaderColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> queueColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> reserveStatusColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> reserveDateColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> pickupDeadlineColumn;

  @FXML
  private VBox reservationActionPane;

  @FXML
  private VBox reservationReaderPane;

  @FXML
  private ComboBox<NamedOption> reserveReaderBox;

  @FXML
  private ComboBox<NamedOption> reserveBookBox;

  @FXML
  private TextArea reserveNoteArea;

  @FXML
  private Button reserveButton;

  @FXML
  private Button cancelReservationButton;

  @FXML
  private void initialize() {
    initOptions();
    initTables();
    applyRoleMode();
    if (!isReaderMode()) {
      loadReaderOptions();
      loadCopyOptions();
    }
    loadBookOptions();
    loadBorrows();
    loadReservations();
  }

  @FXML
  private void handleBorrowSearch() {
    loadBorrows();
  }

  @FXML
  private void handleBorrowRefresh() {
    borrowKeywordField.clear();
    borrowStatusBox.getSelectionModel().selectFirst();
    loadBorrows();
  }

  @FXML
  private void handleBorrowBook() {
    if (readerBox.getValue() == null || copyBox.getValue() == null) {
      UiHelper.showWarning("提示", "请选择读者和副本");
      return;
    }
    JsonObject data = new JsonObject();
    data.addProperty("readerId", readerBox.getValue().getValue());
    data.addProperty("copyId", copyBox.getValue().getValue());
    addText(data, "borrowDate", DateValueHelper.formatDate(borrowDatePicker.getValue()));
    addText(data, "remark", borrowRemarkArea.getText());
    runRequest("/api/circulation/borrowBook", data, "借阅登记成功", true);
  }

  @FXML
  private void handleRenew() {
    Map<String, Object> selected = borrowTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      UiHelper.showWarning("提示", "请先选择一条借阅记录");
      return;
    }
    JsonObject data = new JsonObject();
    data.addProperty("recordId", String.valueOf(selected.get("recordId")));
    runRequest("/api/circulation/renewBorrow", data, "续借成功", true);
  }

  @FXML
  private void handleReturn() {
    Map<String, Object> selected = borrowTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      UiHelper.showWarning("提示", "请先选择一条借阅记录");
      return;
    }
    JsonObject data = new JsonObject();
    data.addProperty("recordId", String.valueOf(selected.get("recordId")));
    runRequest("/api/circulation/returnBook", data, "归还登记成功", true);
  }

  @FXML
  private void handleReserveSearch() {
    loadReservations();
  }

  @FXML
  private void handleReserveRefresh() {
    reservationStatusBox.getSelectionModel().selectFirst();
    loadReservations();
  }

  @FXML
  private void handleReserveBook() {
    if (reserveBookBox.getValue() == null) {
      UiHelper.showWarning("提示", "请先选择一本图书");
      return;
    }
    if (!isReaderMode() && reserveReaderBox.getValue() == null) {
      UiHelper.showWarning("提示", "请先选择预约读者");
      return;
    }
    JsonObject data = new JsonObject();
    data.addProperty("bookId", reserveBookBox.getValue().getValue());
    if (!isReaderMode() && reserveReaderBox.getValue() != null) {
      data.addProperty("readerId", reserveReaderBox.getValue().getValue());
    }
    addText(data, "note", reserveNoteArea.getText());
    runRequest("/api/circulation/reserveBook", data, "预约提交成功", false);
  }

  @FXML
  private void handleCancelReservation() {
    Map<String, Object> selected = reservationTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      UiHelper.showWarning("提示", "请先选择一条预约记录");
      return;
    }
    JsonObject data = new JsonObject();
    data.addProperty("reservationId", String.valueOf(selected.get("reservationId")));
    runRequest("/api/circulation/cancelReservation", data, "预约取消成功", false);
  }

  private void initOptions() {
    borrowStatusBox.setItems(FXCollections.observableArrayList("全部状态", "BORROWED", "OVERDUE", "RETURNED"));
    borrowStatusBox.getSelectionModel().selectFirst();
    StatusTextHelper.applyTo(borrowStatusBox);

    reservationStatusBox.setItems(FXCollections.observableArrayList("全部状态", "WAITING", "READY", "FULFILLED", "CANCELED"));
    reservationStatusBox.getSelectionModel().selectFirst();
    StatusTextHelper.applyTo(reservationStatusBox);
  }

  private void initTables() {
    TableViewHelper.bindTextColumn(borrowTitleColumn, "title");
    TableViewHelper.bindTextColumn(borrowReaderColumn, "readerName");
    TableViewHelper.bindTextColumn(borrowBarcodeColumn, "barcode");
    TableViewHelper.bindTextColumn(borrowStatusColumn, "status", StatusTextHelper.mapper());
    TableViewHelper.bindTextColumn(borrowDateColumn, "borrowDate");
    TableViewHelper.bindTextColumn(dueDateColumn, "dueDate");
    TableViewHelper.bindTextColumn(fineAmountColumn, "fineAmount");
    borrowTable.setItems(borrowItems);

    TableViewHelper.bindTextColumn(reserveTitleColumn, "title");
    TableViewHelper.bindTextColumn(reserveReaderColumn, "readerName");
    TableViewHelper.bindTextColumn(queueColumn, "queueNo", value -> value == null || value.isBlank() ? "" : "第" + value + "位");
    TableViewHelper.bindTextColumn(reserveStatusColumn, "status", StatusTextHelper.mapper());
    TableViewHelper.bindTextColumn(reserveDateColumn, "reserveDate");
    TableViewHelper.bindTextColumn(pickupDeadlineColumn, "pickupDeadline");
    reservationTable.setItems(reservationItems);
  }

  private void applyRoleMode() {
    boolean reader = isReaderMode();
    borrowActionPane.setManaged(!reader);
    borrowActionPane.setVisible(!reader);
    borrowButton.setManaged(!reader);
    borrowButton.setVisible(!reader);
    returnButton.setManaged(!reader);
    returnButton.setVisible(!reader);
    reservationActionPane.setManaged(true);
    reservationActionPane.setVisible(true);
    if (reservationReaderPane != null) {
      reservationReaderPane.setManaged(!reader);
      reservationReaderPane.setVisible(!reader);
    }
  }

  private void loadReaderOptions() {
    JsonObject data = new JsonObject();
    data.addProperty("roleCode", "2");
    UiHelper.runAsync(() -> JsonHelper.toList(JsonHelper.array(post("/api/user/getUserList", data), "data")), items -> {
      ObservableList<NamedOption> options = FXCollections.observableArrayList();
      for (Map<String, Object> item : items) {
        options.add(new NamedOption(String.valueOf(item.get("userId")),
            UiHelper.safeText(item.get("name")) + " / " + UiHelper.safeText(item.get("account")), item));
      }
      readerBox.setItems(options);
      if (reserveReaderBox != null) {
        reserveReaderBox.setItems(FXCollections.observableArrayList(options));
      }
      if (!options.isEmpty()) {
        readerBox.getSelectionModel().selectFirst();
        if (reserveReaderBox != null) {
          reserveReaderBox.getSelectionModel().selectFirst();
        }
      }
    }, ex -> UiHelper.showError("读者加载失败", ex == null ? "未知错误" : ex.getMessage()));
  }

  private void loadCopyOptions() {
    JsonObject data = new JsonObject();
    UiHelper.runAsync(() -> JsonHelper.toList(JsonHelper.array(post("/api/catalog/getCopyList", data), "data")), items -> {
      ObservableList<NamedOption> options = FXCollections.observableArrayList();
      for (Map<String, Object> item : items) {
        String status = UiHelper.safeText(item.get("status"));
        if (!"AVAILABLE".equals(status) && !"RESERVED".equals(status)) {
          continue;
        }
        String label = UiHelper.safeText(item.get("barcode")) + " / " + UiHelper.safeText(item.get("title")) + " / " + StatusTextHelper.toDisplay(status);
        options.add(new NamedOption(String.valueOf(item.get("copyId")), label, item));
      }
      copyBox.setItems(options);
      if (!options.isEmpty()) {
        copyBox.getSelectionModel().selectFirst();
      }
    }, ex -> UiHelper.showError("副本选项加载失败", ex == null ? "未知错误" : ex.getMessage()));
  }

  private void loadBookOptions() {
    UiHelper.runAsync(() -> JsonHelper.toList(JsonHelper.array(post("/api/catalog/getBookList", new JsonObject()), "data")), items -> {
      ObservableList<NamedOption> options = FXCollections.observableArrayList();
      for (Map<String, Object> item : items) {
        String label = UiHelper.safeText(item.get("title")) + " / 可借 " + UiHelper.safeText(item.get("availableCopies"));
        options.add(new NamedOption(String.valueOf(item.get("bookId")), label, item));
      }
      reserveBookBox.setItems(options);
      if (!options.isEmpty()) {
        reserveBookBox.getSelectionModel().selectFirst();
      }
    }, ex -> UiHelper.showError("图书选项加载失败", ex == null ? "未知错误" : ex.getMessage()));
  }

  private void loadBorrows() {
    JsonObject data = new JsonObject();
    addText(data, "keyword", borrowKeywordField.getText());
    if (borrowStatusBox.getValue() != null && !"全部状态".equals(borrowStatusBox.getValue())) {
      data.addProperty("status", borrowStatusBox.getValue());
    }
    UiHelper.runAsync(() -> JsonHelper.toList(JsonHelper.array(post("/api/circulation/getBorrowList", data), "data")), items -> {
      borrowItems.setAll(items);
      if (!borrowItems.isEmpty()) {
        borrowTable.getSelectionModel().selectFirst();
      }
    }, ex -> UiHelper.showError("借阅记录加载失败", ex == null ? "未知错误" : ex.getMessage()));
  }

  private void loadReservations() {
    JsonObject data = new JsonObject();
    if (reservationStatusBox.getValue() != null && !"全部状态".equals(reservationStatusBox.getValue())) {
      data.addProperty("status", reservationStatusBox.getValue());
    }
    UiHelper.runAsync(() -> JsonHelper.toList(JsonHelper.array(post("/api/circulation/getReservationList", data), "data")), items -> {
      reservationItems.setAll(items);
      if (!reservationItems.isEmpty()) {
        reservationTable.getSelectionModel().selectFirst();
      }
    }, ex -> UiHelper.showError("预约记录加载失败", ex == null ? "未知错误" : ex.getMessage()));
  }

  private void runRequest(String path, JsonObject data, String successMessage, boolean reloadCopyOptions) {
    UiHelper.runAsync(() -> post(path, data), ignored -> {
      UiHelper.showInfo("操作成功", successMessage);
      if (reloadCopyOptions && !isReaderMode()) {
        loadCopyOptions();
      }
      loadBorrows();
      loadReservations();
    }, ex -> UiHelper.showError("操作失败", ex == null ? "未知错误" : ex.getMessage()));
  }

  private JsonObject post(String path, JsonObject data) throws Exception {
    JsonObject payload = new JsonObject();
    payload.add("data", data == null ? new JsonObject() : data);
    JsonObject response = apiClient.post(path, payload, ApiSession.getToken());
    if (response.get("code").getAsInt() != 0) {
      throw new IllegalStateException(JsonHelper.getString(response, "msg"));
    }
    return response;
  }

  private void addText(JsonObject target, String key, String value) {
    if (value != null && !value.isBlank()) {
      target.addProperty(key, value.trim());
    }
  }

  private boolean isReaderMode() {
    return "READER".equalsIgnoreCase(ApiSession.getRole());
  }
}
