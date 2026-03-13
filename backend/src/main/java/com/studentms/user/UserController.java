package com.studentms.user;

import com.studentms.common.DataRequest;
import com.studentms.common.DataResponse;
import com.studentms.common.RequestUtils;
import com.studentms.security.RequiresRoles;
import com.studentms.security.Role;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/getUserList")
  @RequiresRoles({Role.ADMIN, Role.STAFF})
  public DataResponse<List<Map<String, Object>>> getUserList(@RequestBody DataRequest<Map<String, Object>> request) {
    String keyword = request == null ? null : RequestUtils.stringValue(request.getData(), "keyword");
    String roleCode = request == null ? null : RequestUtils.stringValue(request.getData(), "roleCode");
    int page = request == null || request.getPage() == null ? 1 : request.getPage();
    int pageSize = request == null || request.getPageSize() == null ? 20 : request.getPageSize();
    Page<Map<String, Object>> result = userService.getUserList(keyword, roleCode, page, pageSize);
    return DataResponse.success(result.getContent(), result.getTotalElements());
  }

  @PostMapping("/getUserInfo")
  @RequiresRoles({Role.ADMIN, Role.STAFF})
  public DataResponse<Map<String, Object>> getUserInfo(@RequestBody DataRequest<Map<String, Object>> request) {
    Integer userId = RequestUtils.intValue(request == null ? null : request.getData(), "userId");
    if (userId == null) {
      throw new IllegalArgumentException("userId不能为空");
    }
    return DataResponse.success(userService.getUserInfo(userId));
  }

  @PostMapping("/saveUser")
  @RequiresRoles({Role.ADMIN})
  public DataResponse<Map<String, Object>> saveUser(@RequestBody DataRequest<Map<String, Object>> request) {
    return DataResponse.success(userService.saveUser(request == null ? null : request.getData()));
  }

  @PostMapping("/deleteUser")
  @RequiresRoles({Role.ADMIN})
  public DataResponse<String> deleteUser(@RequestBody DataRequest<Map<String, Object>> request) {
    Integer userId = RequestUtils.intValue(request == null ? null : request.getData(), "userId");
    if (userId == null) {
      throw new IllegalArgumentException("userId不能为空");
    }
    userService.deleteUser(userId);
    return DataResponse.success("OK");
  }

  @PostMapping("/payFine")
  @RequiresRoles({Role.ADMIN})
  public DataResponse<Map<String, Object>> payFine(@RequestBody DataRequest<Map<String, Object>> request) {
    Map<String, Object> data = request == null ? null : request.getData();
    Integer userId = RequestUtils.intValue(data, "userId");
    BigDecimal amount = RequestUtils.decimalValue(data, "amount");
    if (userId == null) {
      throw new IllegalArgumentException("userId不能为空");
    }
    return DataResponse.success(userService.payFine(userId, amount));
  }
}
