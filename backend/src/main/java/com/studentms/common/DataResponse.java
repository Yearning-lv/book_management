package com.studentms.common;

public class DataResponse<T> {
  private int code;
  private String msg;
  private T data;
  private long total;

  public static <T> DataResponse<T> success(T data) {
    DataResponse<T> resp = new DataResponse<>();
    resp.code = ErrorCode.SUCCESS.getCode();
    resp.msg = ErrorCode.SUCCESS.getMessage();
    resp.data = data;
    resp.total = 0;
    return resp;
  }

  public static <T> DataResponse<T> success(T data, long total) {
    DataResponse<T> resp = new DataResponse<>();
    resp.code = ErrorCode.SUCCESS.getCode();
    resp.msg = ErrorCode.SUCCESS.getMessage();
    resp.data = data;
    resp.total = total;
    return resp;
  }

  public static <T> DataResponse<T> failure(ErrorCode code, String message) {
    DataResponse<T> resp = new DataResponse<>();
    resp.code = code.getCode();
    resp.msg = message == null || message.isBlank() ? code.getMessage() : message;
    resp.data = null;
    resp.total = 0;
    return resp;
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }
}
