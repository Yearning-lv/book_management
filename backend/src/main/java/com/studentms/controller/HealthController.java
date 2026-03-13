package com.studentms.controller;

import com.studentms.common.DataResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

  @GetMapping("/health")
  public DataResponse<String> health() {
    return DataResponse.success("OK");
  }
}
