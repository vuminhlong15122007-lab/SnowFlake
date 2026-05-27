package com.javfxtutorial.hethongdaugia.common.Exception.sys;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

public class ResourceExhaustedException extends SystemException {
  private static final long serialVersionUID = 1L;

  private final String resourceType;

  public ResourceExhaustedException() {
    super(ErrorCode.SYS_RESOURCE_EXHAUSTED, "Hệ thống đang quá tải, vui lòng thử lại sau");
    this.resourceType = null;
  }

  public ResourceExhaustedException(String resourceType) {
    super(
        ErrorCode.SYS_RESOURCE_EXHAUSTED,
        String.format("Tài nguyên '%s' đã cạn kiệt, vui lòng thử lại sau", resourceType));
    this.resourceType = resourceType;
  }

  public ResourceExhaustedException(String resourceType, String message) {
    super(ErrorCode.SYS_RESOURCE_EXHAUSTED, message);
    this.resourceType = resourceType;
  }

  public String getResourceType() {
    return resourceType;
  }
}
