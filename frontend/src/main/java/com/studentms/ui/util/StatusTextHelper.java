package com.studentms.ui.util;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;

import java.util.Map;
import java.util.function.UnaryOperator;

public final class StatusTextHelper {
  private static final Map<String, String> LABELS = Map.ofEntries(
      Map.entry("ACTIVE", "启用"),
      Map.entry("FROZEN", "冻结"),
      Map.entry("NORMAL", "正常"),
      Map.entry("PAUSED", "暂停"),
      Map.entry("ON_SHELF", "已上架"),
      Map.entry("OFF_SHELF", "已下架"),
      Map.entry("AVAILABLE", "可借"),
      Map.entry("BORROWED", "借阅中"),
      Map.entry("RESERVED", "预约待取"),
      Map.entry("REPAIR", "维修中"),
      Map.entry("LOST", "已遗失"),
      Map.entry("OVERDUE", "已逾期"),
      Map.entry("RETURNED", "已归还"),
      Map.entry("WAITING", "排队中"),
      Map.entry("READY", "待取书"),
      Map.entry("FULFILLED", "已完成"),
      Map.entry("CANCELED", "已取消")
  );
  private static final Map<String, String> CONDITION_LABELS = Map.of(
      "GOOD", "良好",
      "NORMAL", "一般",
      "WORN", "磨损"
  );

  private StatusTextHelper() {
  }

  public static String toDisplay(String raw) {
    String value = raw == null ? "" : raw.trim();
    return LABELS.getOrDefault(value, value);
  }

  public static UnaryOperator<String> mapper() {
    return StatusTextHelper::toDisplay;
  }

  public static String toConditionDisplay(String raw) {
    String value = raw == null ? "" : raw.trim();
    return CONDITION_LABELS.getOrDefault(value, toDisplay(value));
  }

  public static UnaryOperator<String> conditionMapper() {
    return StatusTextHelper::toConditionDisplay;
  }

  public static void applyTo(ComboBox<String> comboBox) {
    comboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(String value) {
        return toDisplay(value);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    });
    comboBox.setCellFactory(listView -> createCell());
    comboBox.setButtonCell(createCell());
  }

  public static void applyConditionTo(ComboBox<String> comboBox) {
    comboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(String value) {
        return toConditionDisplay(value);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    });
    comboBox.setCellFactory(listView -> createConditionCell());
    comboBox.setButtonCell(createConditionCell());
  }

  private static ListCell<String> createCell() {
    return new ListCell<>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? "" : toDisplay(item));
      }
    };
  }

  private static ListCell<String> createConditionCell() {
    return new ListCell<>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? "" : toConditionDisplay(item));
      }
    };
  }
}
