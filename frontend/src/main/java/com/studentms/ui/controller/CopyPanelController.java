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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.Map;

public class CopyPanelController {
  private final ApiClient apiClient = new ApiClient();
  private final ObservableList<Map<String, Object>> copyItems = FXCollections.observableArrayList();
  private Integer currentCopyId;

  @FXML
  private TextField keywordField;

  @FXML
  private ComboBox<String> statusFilterBox;

  @FXML
  private TableView<Map<String, Object>> copyTable;

  @FXML
  private TableColumn<Map<String, Object>, String> barcodeColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> titleColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> locationColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> statusColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> conditionColumn;

  @FXML
  private ComboBox<NamedOption> bookBox;

  @FXML
  private TextField barcodeField;

  @FXML
  private TextField locationField;

  @FXML
  private ComboBox<String> conditionBox;

  @FXML
  private ComboBox<String> statusBox;

  @FXML
  private DatePicker purchaseDatePicker;

  @FXML
  private DatePicker inventoryDatePicker;

  @FXML
  private void initialize() {
    initOptions();
    initTable();
    loadBookOptions();
    clearForm();
    copyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> populateForm(newValue));
    loadCopies();
  }

  @FXML
  private void handleSearch() {
    loadCopies();
  }

  @FXML
  private void handleResetFilter() {
    keywordField.clear();
    statusFilterBox.getSelectionModel().selectFirst();
    loadCopies();
  }

  @FXML
  private void handleNew() {
    clearForm();
    copyTable.getSelectionModel().clearSelection();
  }

  @FXML
  private void handleSave() {
    if (bookBox.getValue() == null || barcodeField.getText().isBlank()) {
      UiHelper.showWarning("提示", "图书和条码不能为空");
      return;
    }
    JsonObject data = new JsonObject();
    addInt(data, "copyId", currentCopyId);
    addText(data, "bookId", bookBox.getValue().getValue());
    addText(data, "barcode", barcodeField.getText());
    addText(data, "locationCode", locationField.getText());
    addText(data, "conditionLevel", conditionBox.getValue());
    addText(data, "status", statusBox.getValue());
    addText(data, "purchaseDate", DateValueHelper.formatDate(purchaseDatePicker.getValue()));
    addText(data, "lastInventoryDate", DateValueHelper.formatDate(inventoryDatePicker.getValue()));
    runRequest("/api/catalog/saveCopy", data, "馆藏副本保存成功");
  }

  @FXML
  private void handleDelete() {
    if (currentCopyId == null) {
      UiHelper.showWarning("提示", "请先选择一条馆藏副本");
      return;
    }
    JsonObject data = new JsonObject();
    addInt(data, "copyId", currentCopyId);
    runRequest("/api/catalog/deleteCopy", data, "馆藏副本删除成功");
  }

  private void initOptions() {
    statusFilterBox.setItems(FXCollections.observableArrayList("全部状态", "AVAILABLE", "BORROWED", "RESERVED", "REPAIR", "LOST"));
    statusFilterBox.getSelectionModel().selectFirst();
    StatusTextHelper.applyTo(statusFilterBox);

    conditionBox.setItems(FXCollections.observableArrayList("GOOD", "NORMAL", "WORN"));
    conditionBox.getSelectionModel().selectFirst();
    StatusTextHelper.applyConditionTo(conditionBox);

    statusBox.setItems(FXCollections.observableArrayList("AVAILABLE", "BORROWED", "RESERVED", "REPAIR", "LOST"));
    statusBox.getSelectionModel().selectFirst();
    StatusTextHelper.applyTo(statusBox);
  }

  private void initTable() {
    TableViewHelper.bindTextColumn(barcodeColumn, "barcode");
    TableViewHelper.bindTextColumn(titleColumn, "title");
    TableViewHelper.bindTextColumn(locationColumn, "locationCode");
    TableViewHelper.bindTextColumn(statusColumn, "status", StatusTextHelper.mapper());
    TableViewHelper.bindTextColumn(conditionColumn, "conditionLevel", StatusTextHelper.conditionMapper());
    copyTable.setItems(copyItems);
  }

  private void loadBookOptions() {
    JsonObject data = new JsonObject();
    data.addProperty("pageSize", 300);
    UiHelper.runAsync(() -> JsonHelper.toList(JsonHelper.array(post("/api/catalog/getBookList", data), "data")), items -> {
      ObservableList<NamedOption> options = FXCollections.observableArrayList();
      for (Map<String, Object> item : items) {
        String label = UiHelper.safeText(item.get("title")) + " / " + UiHelper.safeText(item.get("isbn"));
        options.add(new NamedOption(String.valueOf(item.get("bookId")), label, item));
      }
      bookBox.setItems(options);
      if (!options.isEmpty()) {
        bookBox.getSelectionModel().selectFirst();
      }
    }, ex -> UiHelper.showError("图书选项加载失败", ex == null ? "未知错误" : ex.getMessage()));
  }

  private void loadCopies() {
    JsonObject data = new JsonObject();
    addText(data, "keyword", keywordField.getText());
    if (statusFilterBox.getValue() != null && !"全部状态".equals(statusFilterBox.getValue())) {
      data.addProperty("status", statusFilterBox.getValue());
    }
    UiHelper.runAsync(() -> JsonHelper.toList(JsonHelper.array(post("/api/catalog/getCopyList", data), "data")), items -> {
      copyItems.setAll(items);
      if (!copyItems.isEmpty()) {
        copyTable.getSelectionModel().selectFirst();
      }
    }, ex -> UiHelper.showError("馆藏加载失败", ex == null ? "未知错误" : ex.getMessage()));
  }

  private void populateForm(Map<String, Object> copy) {
    if (copy == null) {
      clearForm();
      return;
    }
    currentCopyId = intValue(copy.get("copyId"));
    selectBook(UiHelper.safeText(copy.get("bookId")));
    barcodeField.setText(UiHelper.safeText(copy.get("barcode")));
    locationField.setText(UiHelper.safeText(copy.get("locationCode")));
    conditionBox.setValue(UiHelper.safeText(copy.get("conditionLevel")));
    statusBox.setValue(UiHelper.safeText(copy.get("status")));
    purchaseDatePicker.setValue(DateValueHelper.parseDate(UiHelper.safeText(copy.get("purchaseDate"))));
    inventoryDatePicker.setValue(DateValueHelper.parseDate(UiHelper.safeText(copy.get("lastInventoryDate"))));
  }

  private void clearForm() {
    currentCopyId = null;
    if (!bookBox.getItems().isEmpty()) {
      bookBox.getSelectionModel().selectFirst();
    }
    barcodeField.clear();
    locationField.clear();
    conditionBox.getSelectionModel().selectFirst();
    statusBox.getSelectionModel().selectFirst();
    purchaseDatePicker.setValue(null);
    inventoryDatePicker.setValue(null);
  }

  private void selectBook(String bookId) {
    if (bookId == null || bookId.isBlank()) {
      return;
    }
    for (NamedOption option : bookBox.getItems()) {
      if (bookId.equals(option.getValue())) {
        bookBox.setValue(option);
        return;
      }
    }
  }

  private void runRequest(String path, JsonObject data, String successMessage) {
    UiHelper.runAsync(() -> post(path, data), ignored -> {
      UiHelper.showInfo("操作成功", successMessage);
      loadCopies();
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
