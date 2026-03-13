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

import java.util.Map;

public class BookPanelController {
  private final ApiClient apiClient = new ApiClient();
  private final ObservableList<Map<String, Object>> bookItems = FXCollections.observableArrayList();
  private Integer currentBookId;

  @FXML
  private TextField keywordField;

  @FXML
  private ComboBox<NamedOption> categoryFilterBox;

  @FXML
  private ComboBox<String> shelfStatusFilterBox;

  @FXML
  private TableView<Map<String, Object>> bookTable;

  @FXML
  private TableColumn<Map<String, Object>, String> isbnColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> titleColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> authorColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> categoryColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> availableColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> totalColumn;

  @FXML
  private TableColumn<Map<String, Object>, String> statusColumn;

  @FXML
  private TextField isbnField;

  @FXML
  private TextField titleField;

  @FXML
  private TextField authorField;

  @FXML
  private ComboBox<NamedOption> categoryBox;

  @FXML
  private ComboBox<NamedOption> publisherBox;

  @FXML
  private DatePicker publishDatePicker;

  @FXML
  private TextField priceField;

  @FXML
  private ComboBox<String> shelfStatusBox;

  @FXML
  private TextField keywordsField;

  @FXML
  private TextArea summaryArea;

  @FXML
  private Button saveButton;

  @FXML
  private Button deleteButton;

  @FXML
  private Button reserveButton;

  @FXML
  private void initialize() {
    initStaticOptions();
    initTable();
    loadLookups();
    clearForm();
    bookTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> populateForm(newValue));
    applyRoleMode();
    loadBooks();
  }

  @FXML
  private void handleSearch() {
    loadBooks();
  }

  @FXML
  private void handleResetFilter() {
    keywordField.clear();
    categoryFilterBox.getSelectionModel().selectFirst();
    shelfStatusFilterBox.getSelectionModel().selectFirst();
    loadBooks();
  }

  @FXML
  private void handleNew() {
    clearForm();
    bookTable.getSelectionModel().clearSelection();
  }

  @FXML
  private void handleSave() {
    if (categoryBox.getValue() == null || isbnField.getText().isBlank() || titleField.getText().isBlank() || authorField.getText().isBlank()) {
      UiHelper.showWarning("提示", "ISBN、书名、作者和分类不能为空");
      return;
    }
    JsonObject data = new JsonObject();
    addInt(data, "bookId", currentBookId);
    addText(data, "isbn", isbnField.getText());
    addText(data, "title", titleField.getText());
    addText(data, "author", authorField.getText());
    addText(data, "categoryId", categoryBox.getValue().getValue());
    addText(data, "publisherId", publisherBox.getValue() == null ? null : publisherBox.getValue().getValue());
    addText(data, "publishDate", DateValueHelper.formatDate(publishDatePicker.getValue()));
    addText(data, "price", priceField.getText());
    addText(data, "shelfStatus", shelfStatusBox.getValue());
    addText(data, "keywords", keywordsField.getText());
    addText(data, "summary", summaryArea.getText());
    runRequest("/api/catalog/saveBook", data, "图书保存成功", true);
  }

  @FXML
  private void handleDelete() {
    if (currentBookId == null) {
      UiHelper.showWarning("提示", "请先选择一本图书");
      return;
    }
    JsonObject data = new JsonObject();
    addInt(data, "bookId", currentBookId);
    runRequest("/api/catalog/deleteBook", data, "图书删除成功", true);
  }

  @FXML
  private void handleReserve() {
    if (currentBookId == null) {
      UiHelper.showWarning("提示", "请先选择一本图书");
      return;
    }
    JsonObject data = new JsonObject();
    addInt(data, "bookId", currentBookId);
    runRequest("/api/circulation/reserveBook", data, "预约申请已提交", false);
  }

  private void initStaticOptions() {
    categoryFilterBox.setItems(FXCollections.observableArrayList(new NamedOption("", "全部分类", null)));
    categoryFilterBox.getSelectionModel().selectFirst();

    shelfStatusFilterBox.setItems(FXCollections.observableArrayList("全部状态", "ON_SHELF", "OFF_SHELF"));
    shelfStatusFilterBox.getSelectionModel().selectFirst();
    StatusTextHelper.applyTo(shelfStatusFilterBox);

    shelfStatusBox.setItems(FXCollections.observableArrayList("ON_SHELF", "OFF_SHELF"));
    shelfStatusBox.getSelectionModel().selectFirst();
    StatusTextHelper.applyTo(shelfStatusBox);
  }

  private void initTable() {
    TableViewHelper.bindTextColumn(isbnColumn, "isbn");
    TableViewHelper.bindTextColumn(titleColumn, "title");
    TableViewHelper.bindTextColumn(authorColumn, "author");
    TableViewHelper.bindTextColumn(categoryColumn, "categoryName");
    TableViewHelper.bindTextColumn(availableColumn, "availableCopies");
    TableViewHelper.bindTextColumn(totalColumn, "totalCopies");
    TableViewHelper.bindTextColumn(statusColumn, "shelfStatus", StatusTextHelper.mapper());
    bookTable.setItems(bookItems);
  }

  private void applyRoleMode() {
    boolean readerMode = "READER".equalsIgnoreCase(ApiSession.getRole());
    setEditable(!readerMode);
    reserveButton.setVisible(readerMode);
    reserveButton.setManaged(readerMode);
  }

  private void setEditable(boolean editable) {
    isbnField.setDisable(!editable);
    titleField.setDisable(!editable);
    authorField.setDisable(!editable);
    categoryBox.setDisable(!editable);
    publisherBox.setDisable(!editable);
    publishDatePicker.setDisable(!editable);
    priceField.setDisable(!editable);
    shelfStatusBox.setDisable(!editable);
    keywordsField.setDisable(!editable);
    summaryArea.setDisable(!editable);
    saveButton.setVisible(editable);
    saveButton.setManaged(editable);
    deleteButton.setVisible(editable);
    deleteButton.setManaged(editable);
  }

  private void loadLookups() {
    UiHelper.runAsync(() -> JsonHelper.toList(JsonHelper.array(post("/api/catalog/getCategoryOptions", new JsonObject()), "data")), items -> {
      ObservableList<NamedOption> options = FXCollections.observableArrayList(new NamedOption("", "全部分类", null));
      for (Map<String, Object> item : items) {
        options.add(new NamedOption(String.valueOf(item.get("categoryId")), UiHelper.safeText(item.get("categoryName")), item));
      }
      categoryFilterBox.setItems(options);
      if (!options.isEmpty()) {
        categoryFilterBox.getSelectionModel().selectFirst();
      }
      ObservableList<NamedOption> formOptions = FXCollections.observableArrayList();
      for (NamedOption option : options) {
        if (!option.getValue().isBlank()) {
          formOptions.add(option);
        }
      }
      categoryBox.setItems(formOptions);
      if (!formOptions.isEmpty()) {
        categoryBox.getSelectionModel().selectFirst();
      }
    }, ex -> UiHelper.showError("分类加载失败", ex == null ? "未知错误" : ex.getMessage()));

    UiHelper.runAsync(() -> JsonHelper.toList(JsonHelper.array(post("/api/catalog/getPublisherOptions", new JsonObject()), "data")), items -> {
      ObservableList<NamedOption> options = FXCollections.observableArrayList();
      for (Map<String, Object> item : items) {
        options.add(new NamedOption(String.valueOf(item.get("publisherId")), UiHelper.safeText(item.get("publisherName")), item));
      }
      publisherBox.setItems(options);
    }, ex -> UiHelper.showError("出版社加载失败", ex == null ? "未知错误" : ex.getMessage()));
  }

  private void loadBooks() {
    JsonObject data = new JsonObject();
    addText(data, "keyword", keywordField.getText());
    NamedOption category = categoryFilterBox.getValue();
    if (category != null && category.getValue() != null && !category.getValue().isBlank()) {
      data.addProperty("categoryId", category.getValue());
    }
    String status = shelfStatusFilterBox.getValue();
    if (status != null && !"全部状态".equals(status)) {
      data.addProperty("shelfStatus", status);
    }
    UiHelper.runAsync(() -> JsonHelper.toList(JsonHelper.array(post("/api/catalog/getBookList", data), "data")), items -> {
      bookItems.setAll(items);
      if (!bookItems.isEmpty()) {
        bookTable.getSelectionModel().selectFirst();
      }
    }, ex -> UiHelper.showError("图书加载失败", ex == null ? "未知错误" : ex.getMessage()));
  }

  private void populateForm(Map<String, Object> book) {
    if (book == null) {
      clearForm();
      return;
    }
    currentBookId = intValue(book.get("bookId"));
    isbnField.setText(UiHelper.safeText(book.get("isbn")));
    titleField.setText(UiHelper.safeText(book.get("title")));
    authorField.setText(UiHelper.safeText(book.get("author")));
    selectOption(categoryBox, UiHelper.safeText(book.get("categoryId")));
    selectOption(publisherBox, UiHelper.safeText(book.get("publisherId")));
    publishDatePicker.setValue(DateValueHelper.parseDate(UiHelper.safeText(book.get("publishDate"))));
    priceField.setText(UiHelper.safeText(book.get("price")));
    shelfStatusBox.setValue(UiHelper.safeText(book.get("shelfStatus")));
    keywordsField.setText(UiHelper.safeText(book.get("keywords")));
    summaryArea.setText(UiHelper.safeText(book.get("summary")));
  }

  private void clearForm() {
    currentBookId = null;
    isbnField.clear();
    titleField.clear();
    authorField.clear();
    priceField.clear();
    keywordsField.clear();
    summaryArea.clear();
    publishDatePicker.setValue(null);
    if (!categoryBox.getItems().isEmpty()) {
      categoryBox.getSelectionModel().selectFirst();
    }
    if (!publisherBox.getItems().isEmpty()) {
      publisherBox.getSelectionModel().selectFirst();
    }
    shelfStatusBox.getSelectionModel().selectFirst();
  }

  private void runRequest(String path, JsonObject data, String successMessage, boolean reload) {
    UiHelper.runAsync(() -> post(path, data), ignored -> {
      UiHelper.showInfo("操作成功", successMessage);
      if (reload) {
        loadBooks();
      }
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

  private void selectOption(ComboBox<NamedOption> comboBox, String value) {
    if (value == null || value.isBlank()) {
      return;
    }
    for (NamedOption option : comboBox.getItems()) {
      if (value.equals(option.getValue())) {
        comboBox.setValue(option);
        return;
      }
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
