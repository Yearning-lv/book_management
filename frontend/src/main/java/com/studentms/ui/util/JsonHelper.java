package com.studentms.ui.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class JsonHelper {
  private JsonHelper() {
  }

  public static Map<String, Object> toMap(JsonObject jsonObject) {
    Map<String, Object> map = new HashMap<>();
    if (jsonObject == null) {
      return map;
    }
    for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
      map.put(entry.getKey(), toValue(entry.getValue()));
    }
    return map;
  }

  public static List<Map<String, Object>> toList(JsonArray jsonArray) {
    List<Map<String, Object>> list = new ArrayList<>();
    if (jsonArray == null) {
      return list;
    }
    for (JsonElement element : jsonArray) {
      list.add(toMap(element.getAsJsonObject()));
    }
    return list;
  }

  public static String getString(JsonObject object, String key) {
    if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
      return "";
    }
    return object.get(key).getAsString();
  }

  public static JsonObject object(JsonObject object, String key) {
    if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
      return new JsonObject();
    }
    return object.getAsJsonObject(key);
  }

  public static JsonArray array(JsonObject object, String key) {
    if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
      return new JsonArray();
    }
    return object.getAsJsonArray(key);
  }

  private static Object toValue(JsonElement element) {
    if (element == null || element instanceof JsonNull || element.isJsonNull()) {
      return null;
    }
    if (element.isJsonObject()) {
      return toMap(element.getAsJsonObject());
    }
    if (element.isJsonArray()) {
      List<Object> values = new ArrayList<>();
      for (JsonElement child : element.getAsJsonArray()) {
        values.add(toValue(child));
      }
      return values;
    }
    JsonPrimitive primitive = element.getAsJsonPrimitive();
    if (primitive.isBoolean()) {
      return primitive.getAsBoolean();
    }
    if (primitive.isNumber()) {
      return new BigDecimal(primitive.getAsString());
    }
    return primitive.getAsString();
  }
}
