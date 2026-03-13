package com.studentms.ui.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.studentms.ui.api.ApiClient;
import com.studentms.ui.api.ApiSession;
import com.studentms.ui.util.JsonHelper;
import com.studentms.ui.util.UiHelper;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

import java.util.List;
import java.util.Map;

public class StatPanelController {
  private static final int CATEGORY_LABEL_LIMIT = 6;
  private static final int TOP_BOOK_LABEL_LIMIT = 10;

  private final ApiClient apiClient = new ApiClient();

  @FXML
  private Label bookCountLabel;

  @FXML
  private Label copyCountLabel;

  @FXML
  private Label readerCountLabel;

  @FXML
  private Label activeBorrowLabel;

  @FXML
  private Label overdueLabel;

  @FXML
  private Label reservationLabel;

  @FXML
  private Label availableCopyLabel;

  @FXML
  private Label summaryLabel;

  @FXML
  private BarChart<String, Number> categoryChart;

  @FXML
  private CategoryAxis categoryXAxis;

  @FXML
  private LineChart<String, Number> trendChart;

  @FXML
  private CategoryAxis trendXAxis;

  @FXML
  private BarChart<String, Number> topBooksChart;

  @FXML
  private CategoryAxis topBooksXAxis;

  @FXML
  private void initialize() {
    configureCharts();
    loadStats();
  }

  @FXML
  private void handleRefresh() {
    loadStats();
  }

  private void loadStats() {
    UiHelper.runAsync(() -> {
      JsonObject overview = JsonHelper.object(post("/api/stat/overview"), "data");
      JsonArray categoryItems = JsonHelper.array(JsonHelper.object(post("/api/stat/category"), "data"), "items");
      JsonArray trendItems = JsonHelper.array(JsonHelper.object(post("/api/stat/trend"), "data"), "items");
      JsonArray topItems = JsonHelper.array(JsonHelper.object(post("/api/stat/topBooks"), "data"), "items");
      return List.of(overview, categoryItems, trendItems, topItems);
    }, result -> {
      JsonObject overview = (JsonObject) result.get(0);
      JsonArray categoryItems = (JsonArray) result.get(1);
      JsonArray trendItems = (JsonArray) result.get(2);
      JsonArray topItems = (JsonArray) result.get(3);
      fillOverview(overview);
      fillCategoryChart(categoryItems);
      fillTrendChart(trendItems);
      fillTopBooksChart(topItems);
    }, ex -> UiHelper.showError("统计加载失败", ex == null ? "未知错误" : ex.getMessage()));
  }

  private void fillOverview(JsonObject overview) {
    bookCountLabel.setText(JsonHelper.getString(overview, "bookCount"));
    copyCountLabel.setText(JsonHelper.getString(overview, "copyCount"));
    readerCountLabel.setText(JsonHelper.getString(overview, "readerCount"));
    activeBorrowLabel.setText(JsonHelper.getString(overview, "activeBorrowCount"));
    overdueLabel.setText(JsonHelper.getString(overview, "overdueCount"));
    reservationLabel.setText(JsonHelper.getString(overview, "reservationCount"));
    availableCopyLabel.setText(JsonHelper.getString(overview, "availableCopyCount"));
    summaryLabel.setText("当前系统共有 " + JsonHelper.getString(overview, "bookCount") + " 种图书、"
        + JsonHelper.getString(overview, "copyCount") + " 个实体副本，"
        + JsonHelper.getString(overview, "activeBorrowCount") + " 条在途借阅记录。");
  }

  private void fillCategoryChart(JsonArray items) {
    categoryChart.getData().clear();
    XYChart.Series<String, Number> titleSeries = new XYChart.Series<>();
    titleSeries.setName("馆藏书目数");
    XYChart.Series<String, Number> availableSeries = new XYChart.Series<>();
    availableSeries.setName("当前可借副本");
    for (Map<String, Object> item : JsonHelper.toList(items)) {
      String categoryName = UiHelper.safeText(item.get("categoryName"));
      String axisLabel = shortenLabel(categoryName, CATEGORY_LABEL_LIMIT);
      Number titleCount = numberValue(item.get("titleCount"));
      Number availableCopies = numberValue(item.get("availableCopies"));
      titleSeries.getData().add(buildData(axisLabel, titleCount, categoryName + "：馆藏书目 " + titleCount));
      availableSeries.getData().add(buildData(axisLabel, availableCopies, categoryName + "：当前可借 " + availableCopies));
    }
    categoryChart.getData().add(titleSeries);
    categoryChart.getData().add(availableSeries);
  }

  private void fillTrendChart(JsonArray items) {
    trendChart.getData().clear();
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("近 6 个月借阅次数");
    for (Map<String, Object> item : JsonHelper.toList(items)) {
      String month = UiHelper.safeText(item.get("month"));
      Number count = numberValue(item.get("count"));
      series.getData().add(buildData(formatMonthLabel(month), count, month + "：借阅 " + count + " 次"));
    }
    trendChart.getData().add(series);
  }

  private void fillTopBooksChart(JsonArray items) {
    topBooksChart.getData().clear();
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    series.setName("热门图书借阅次数");
    int index = 1;
    for (Map<String, Object> item : JsonHelper.toList(items)) {
      String title = UiHelper.safeText(item.get("title"));
      Number count = numberValue(item.get("count"));
      String axisLabel = index + "." + shortenLabel(title, TOP_BOOK_LABEL_LIMIT);
      series.getData().add(buildData(axisLabel, count, title + "：借阅 " + count + " 次"));
      index++;
    }
    topBooksChart.getData().add(series);
  }

  private void configureCharts() {
    configureChart(categoryChart, categoryXAxis, -28, 360);
    configureChart(trendChart, trendXAxis, 0, 320);
    configureChart(topBooksChart, topBooksXAxis, -25, 360);
  }

  private void configureChart(XYChart<String, Number> chart, CategoryAxis axis, double rotation, double prefHeight) {
    chart.setAnimated(false);
    chart.setLegendVisible(true);
    chart.setPrefHeight(prefHeight);
    chart.setMinHeight(prefHeight);
    axis.setTickLabelRotation(rotation);
    axis.setTickLabelGap(10);
    axis.setGapStartAndEnd(false);
  }

  private XYChart.Data<String, Number> buildData(String axisLabel, Number value, String tooltipText) {
    XYChart.Data<String, Number> data = new XYChart.Data<>(axisLabel, value);
    data.nodeProperty().addListener((obs, oldNode, newNode) -> {
      if (newNode != null && tooltipText != null && !tooltipText.isBlank()) {
        Tooltip.install(newNode, new Tooltip(tooltipText));
      }
    });
    return data;
  }

  private String shortenLabel(String text, int limit) {
    String safeText = UiHelper.safeText(text).trim();
    if (safeText.length() <= limit) {
      return safeText;
    }
    return safeText.substring(0, limit) + "...";
  }

  private String formatMonthLabel(String month) {
    String safeMonth = UiHelper.safeText(month).trim();
    if (safeMonth.matches("\\d{4}-\\d{2}")) {
      return Integer.parseInt(safeMonth.substring(5)) + "月";
    }
    return safeMonth;
  }

  private Number numberValue(Object value) {
    if (value == null) {
      return 0;
    }
    try {
      return Double.parseDouble(String.valueOf(value));
    } catch (NumberFormatException ex) {
      return 0;
    }
  }

  private JsonObject post(String path) throws Exception {
    JsonObject payload = new JsonObject();
    payload.add("data", new JsonObject());
    JsonObject response = apiClient.post(path, payload, ApiSession.getToken());
    if (response.get("code").getAsInt() != 0) {
      throw new IllegalStateException(JsonHelper.getString(response, "msg"));
    }
    return response;
  }
}
