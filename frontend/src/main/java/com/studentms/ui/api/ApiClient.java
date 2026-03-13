package com.studentms.ui.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {
  private final HttpClient httpClient;

  public ApiClient() {
    this.httpClient = HttpClient.newHttpClient();
  }

  public JsonObject post(String path, JsonObject payload, String token) throws IOException, InterruptedException {
    String url = ApiConfig.baseUrl() + path;
    HttpRequest.Builder builder = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("Content-Type", "application/json; charset=UTF-8")
        .header("Accept", "application/json; charset=UTF-8");
    if (token != null && !token.isBlank()) {
      builder.header("Authorization", "Bearer " + token);
    }
    HttpRequest request = builder.POST(HttpRequest.BodyPublishers.ofString(payload.toString(), StandardCharsets.UTF_8)).build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    return JsonParser.parseString(response.body()).getAsJsonObject();
  }
}
