package com.studentms.ui.util;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.TableColumn;

import java.util.Map;
import java.util.function.UnaryOperator;

public final class TableViewHelper {
  private TableViewHelper() {
  }

  public static void bindTextColumn(TableColumn<Map<String, Object>, String> column, String key) {
    column.setCellValueFactory(cell -> new ReadOnlyStringWrapper(UiHelper.safeText(cell.getValue().get(key))));
  }

  public static void bindTextColumn(TableColumn<Map<String, Object>, String> column,
                                    String key,
                                    UnaryOperator<String> mapper) {
    column.setCellValueFactory(cell -> {
      String value = UiHelper.safeText(cell.getValue().get(key));
      return new ReadOnlyStringWrapper(mapper == null ? value : mapper.apply(value));
    });
  }
}
