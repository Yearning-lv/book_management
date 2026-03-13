package com.studentms.common;

import com.studentms.exception.ForbiddenException;
import com.studentms.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<DataResponse<Object>> handleUnauthorized(UnauthorizedException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(DataResponse.failure(ErrorCode.UNAUTHORIZED, ex.getMessage()));
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<DataResponse<Object>> handleForbidden(ForbiddenException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(DataResponse.failure(ErrorCode.FORBIDDEN, ex.getMessage()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<DataResponse<Object>> handleBadRequest(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(DataResponse.failure(ErrorCode.BAD_REQUEST, ex.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<DataResponse<Object>> handleValidation(MethodArgumentNotValidException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(DataResponse.failure(ErrorCode.BAD_REQUEST, "参数校验失败"));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<DataResponse<Object>> handleNotReadable(HttpMessageNotReadableException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(DataResponse.failure(ErrorCode.BAD_REQUEST, "请求体解析失败"));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<DataResponse<Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(DataResponse.failure(ErrorCode.BAD_REQUEST, "数据不符合约束"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<DataResponse<Object>> handleGeneric(Exception ex) {
    log.error("Unhandled exception", ex);
    String msg = ex.getClass().getSimpleName() + ": " + (ex.getMessage() == null ? "" : ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(DataResponse.failure(ErrorCode.SERVER_ERROR, msg));
  }
}
