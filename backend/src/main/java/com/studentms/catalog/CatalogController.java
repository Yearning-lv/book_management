package com.studentms.catalog;

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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {
  private final CatalogService catalogService;

  public CatalogController(CatalogService catalogService) {
    this.catalogService = catalogService;
  }

  @PostMapping("/getBookList")
  @RequiresRoles({Role.ADMIN, Role.STAFF, Role.READER})
  public DataResponse<List<Map<String, Object>>> getBookList(@RequestBody DataRequest<Map<String, Object>> request) {
    Map<String, Object> data = request == null ? null : request.getData();
    int page = request == null || request.getPage() == null ? 1 : request.getPage();
    int pageSize = request == null || request.getPageSize() == null ? 20 : request.getPageSize();
    Page<Map<String, Object>> result = catalogService.getBookList(
        RequestUtils.stringValue(data, "keyword"),
        RequestUtils.intValue(data, "categoryId"),
        RequestUtils.stringValue(data, "shelfStatus"),
        page,
        pageSize
    );
    return DataResponse.success(result.getContent(), result.getTotalElements());
  }

  @PostMapping("/getBookInfo")
  @RequiresRoles({Role.ADMIN, Role.STAFF, Role.READER})
  public DataResponse<Map<String, Object>> getBookInfo(@RequestBody DataRequest<Map<String, Object>> request) {
    Integer bookId = RequestUtils.intValue(request == null ? null : request.getData(), "bookId");
    if (bookId == null) {
      throw new IllegalArgumentException("bookId不能为空");
    }
    return DataResponse.success(catalogService.getBookInfo(bookId));
  }

  @PostMapping("/saveBook")
  @RequiresRoles({Role.ADMIN, Role.STAFF})
  public DataResponse<Map<String, Object>> saveBook(@RequestBody DataRequest<Map<String, Object>> request) {
    return DataResponse.success(catalogService.saveBook(request == null ? null : request.getData()));
  }

  @PostMapping("/deleteBook")
  @RequiresRoles({Role.ADMIN, Role.STAFF})
  public DataResponse<String> deleteBook(@RequestBody DataRequest<Map<String, Object>> request) {
    Integer bookId = RequestUtils.intValue(request == null ? null : request.getData(), "bookId");
    if (bookId == null) {
      throw new IllegalArgumentException("bookId不能为空");
    }
    catalogService.deleteBook(bookId);
    return DataResponse.success("OK");
  }

  @PostMapping("/getCopyList")
  @RequiresRoles({Role.ADMIN, Role.STAFF})
  public DataResponse<List<Map<String, Object>>> getCopyList(@RequestBody DataRequest<Map<String, Object>> request) {
    Map<String, Object> data = request == null ? null : request.getData();
    int page = request == null || request.getPage() == null ? 1 : request.getPage();
    int pageSize = request == null || request.getPageSize() == null ? 20 : request.getPageSize();
    Page<Map<String, Object>> result = catalogService.getCopyList(
        RequestUtils.stringValue(data, "keyword"),
        RequestUtils.stringValue(data, "status"),
        page,
        pageSize
    );
    return DataResponse.success(result.getContent(), result.getTotalElements());
  }

  @PostMapping("/getCopyInfo")
  @RequiresRoles({Role.ADMIN, Role.STAFF})
  public DataResponse<Map<String, Object>> getCopyInfo(@RequestBody DataRequest<Map<String, Object>> request) {
    Integer copyId = RequestUtils.intValue(request == null ? null : request.getData(), "copyId");
    if (copyId == null) {
      throw new IllegalArgumentException("copyId不能为空");
    }
    return DataResponse.success(catalogService.getCopyInfo(copyId));
  }

  @PostMapping("/saveCopy")
  @RequiresRoles({Role.ADMIN, Role.STAFF})
  public DataResponse<Map<String, Object>> saveCopy(@RequestBody DataRequest<Map<String, Object>> request) {
    return DataResponse.success(catalogService.saveCopy(request == null ? null : request.getData()));
  }

  @PostMapping("/deleteCopy")
  @RequiresRoles({Role.ADMIN, Role.STAFF})
  public DataResponse<String> deleteCopy(@RequestBody DataRequest<Map<String, Object>> request) {
    Integer copyId = RequestUtils.intValue(request == null ? null : request.getData(), "copyId");
    if (copyId == null) {
      throw new IllegalArgumentException("copyId不能为空");
    }
    catalogService.deleteCopy(copyId);
    return DataResponse.success("OK");
  }

  @PostMapping("/getCategoryOptions")
  @RequiresRoles({Role.ADMIN, Role.STAFF, Role.READER})
  public DataResponse<List<Map<String, Object>>> getCategoryOptions() {
    return DataResponse.success(catalogService.getCategoryOptions());
  }

  @PostMapping("/getPublisherOptions")
  @RequiresRoles({Role.ADMIN, Role.STAFF, Role.READER})
  public DataResponse<List<Map<String, Object>>> getPublisherOptions() {
    return DataResponse.success(catalogService.getPublisherOptions());
  }
}
