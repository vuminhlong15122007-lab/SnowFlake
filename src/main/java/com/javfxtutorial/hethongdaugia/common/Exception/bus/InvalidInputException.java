package com.javfxtutorial.hethongdaugia.common.Exception.bus;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

/**
 * Ngoại lệ được ném ra khi dữ liệu đầu vào không hợp lệ. Xảy ra khi: Trường dữ liệu bị null hoặc
 * rỗng Email không đúng định dạng Số điện thoại không hợp lệ Giá trị nằm ngoài phạm vi cho phép
 */
public class InvalidInputException extends BusinessException {
  private static final long serialVersionUID = 1L;

  private final String fieldName; // Tên trường bị lỗi
  private final String invalidValue; // Giá trị không hợp lệ
  private final String validationRule; // Quy tắc validation bị vi phạm

  public InvalidInputException(String fieldName, String invalidValue, String reason) {
    super(
        ErrorCode.BIZ_INVALID_INPUT,
        String.format(
            "Trường '%s' với giá trị '%s' không hợp lệ: %s", fieldName, invalidValue, reason));
    this.fieldName = fieldName;
    this.invalidValue = invalidValue;
    this.validationRule = reason;
  }

  public String getFieldName() {
    return fieldName;
  }

  public String getInvalidValue() {
    return invalidValue;
  }

  public String getValidationRule() {
    return validationRule;
  }
}
