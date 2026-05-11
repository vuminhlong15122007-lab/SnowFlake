package com.javfxtutorial.hethongdaugia.common.Exception.data;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

/**
 * Lỗi dữ liệu bị trùng
 */
public class DuplicateKeyException extends DataException {
    private static final long serialVersionUID = 1L;
    private final String entityType;
    private final String fieldName;
    private final Object fieldValue;

    public DuplicateKeyException(String entityType, String fieldName, Object fieldValue){
        super(ErrorCode.DATA_DUPLICATE,
            String.format("%s với %s = %s đã tồn tại trong hệ thống", entityType, fieldName, fieldValue) );
        this.entityType = entityType;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
