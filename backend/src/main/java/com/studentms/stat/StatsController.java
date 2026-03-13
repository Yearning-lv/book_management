package com.studentms.stat;

import com.studentms.common.DataResponse;
import com.studentms.security.RequiresRoles;
import com.studentms.security.Role;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/stat")
@RequiresRoles({Role.ADMIN, Role.STAFF, Role.READER})
public class StatsController {
  private final StatsService statsService;

  public StatsController(StatsService statsService) {
    this.statsService = statsService;
  }

  @PostMapping("/overview")
  public DataResponse<Map<String, Object>> overview() {
    return DataResponse.success(statsService.overview());
  }

  @PostMapping("/category")
  public DataResponse<Map<String, Object>> category() {
    return DataResponse.success(statsService.categoryDistribution());
  }

  @PostMapping("/trend")
  public DataResponse<Map<String, Object>> trend() {
    return DataResponse.success(statsService.borrowTrend());
  }

  @PostMapping("/topBooks")
  public DataResponse<Map<String, Object>> topBooks() {
    return DataResponse.success(statsService.topBooks());
  }
}
