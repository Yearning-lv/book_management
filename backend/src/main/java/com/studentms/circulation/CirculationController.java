package com.studentms.circulation;

import com.studentms.common.DataRequest;
import com.studentms.common.DataResponse;
import com.studentms.common.RequestUtils;
import com.studentms.security.AuthContext;
import com.studentms.security.AuthSession;
import com.studentms.security.RequiresRoles;
import com.studentms.security.Role;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/circulation")
public class CirculationController {
  private final CirculationService circulationService;

  public CirculationController(CirculationService circulationService) {
    this.circulationService = circulationService;
  }

  @PostMapping("/getBorrowList")
  @RequiresRoles({Role.ADMIN, Role.STAFF, Role.READER})
  public DataResponse<List<Map<String, Object>>> getBorrowList(@RequestBody DataRequest<Map<String, Object>> request) {
    Map<String, Object> data = request == null ? null : request.getData();
    int page = request == null || request.getPage() == null ? 1 : request.getPage();
    int pageSize = request == null || request.getPageSize() == null ? 20 : request.getPageSize();
    Page<Map<String, Object>> result = circulationService.getBorrowList(
        RequestUtils.stringValue(data, "keyword"),
        RequestUtils.stringValue(data, "status"),
        page,
        pageSize,
        currentSession()
    );
    return DataResponse.success(result.getContent(), result.getTotalElements());
  }

  @PostMapping("/borrowBook")
  @RequiresRoles({Role.ADMIN, Role.STAFF})
  public DataResponse<Map<String, Object>> borrowBook(@RequestBody DataRequest<Map<String, Object>> request) {
    return DataResponse.success(circulationService.borrowBook(request == null ? null : request.getData(), currentSession()));
  }

  @PostMapping("/renewBorrow")
  @RequiresRoles({Role.ADMIN, Role.STAFF, Role.READER})
  public DataResponse<Map<String, Object>> renewBorrow(@RequestBody DataRequest<Map<String, Object>> request) {
    return DataResponse.success(circulationService.renewBorrow(request == null ? null : request.getData(), currentSession()));
  }

  @PostMapping("/returnBook")
  @RequiresRoles({Role.ADMIN, Role.STAFF})
  public DataResponse<Map<String, Object>> returnBook(@RequestBody DataRequest<Map<String, Object>> request) {
    return DataResponse.success(circulationService.returnBook(request == null ? null : request.getData()));
  }

  @PostMapping("/getReservationList")
  @RequiresRoles({Role.ADMIN, Role.STAFF, Role.READER})
  public DataResponse<List<Map<String, Object>>> getReservationList(@RequestBody DataRequest<Map<String, Object>> request) {
    Map<String, Object> data = request == null ? null : request.getData();
    int page = request == null || request.getPage() == null ? 1 : request.getPage();
    int pageSize = request == null || request.getPageSize() == null ? 20 : request.getPageSize();
    Page<Map<String, Object>> result = circulationService.getReservationList(
        RequestUtils.stringValue(data, "status"),
        page,
        pageSize,
        currentSession()
    );
    return DataResponse.success(result.getContent(), result.getTotalElements());
  }

  @PostMapping("/reserveBook")
  @RequiresRoles({Role.ADMIN, Role.STAFF, Role.READER})
  public DataResponse<Map<String, Object>> reserveBook(@RequestBody DataRequest<Map<String, Object>> request) {
    return DataResponse.success(circulationService.reserveBook(request == null ? null : request.getData(), currentSession()));
  }

  @PostMapping("/cancelReservation")
  @RequiresRoles({Role.ADMIN, Role.STAFF, Role.READER})
  public DataResponse<Map<String, Object>> cancelReservation(@RequestBody DataRequest<Map<String, Object>> request) {
    return DataResponse.success(circulationService.cancelReservation(request == null ? null : request.getData(), currentSession()));
  }

  private AuthSession currentSession() {
    return AuthContext.get();
  }
}
