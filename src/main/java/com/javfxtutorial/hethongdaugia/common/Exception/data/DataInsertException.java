package com.javfxtutorial.hethongdaugia.common.Exception.data;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

/** Lỗi khi thêm dữ liệu vào database */
public class DataInsertException extends DataException {
    private static final long serialVersionUID = 1L;
    private final String entityType;

    public DataInsertException(String entityType) {
        super(ErrorCode.DATA_INSERT_FAILED, String.format("Thêm %s thất bại", entityType));
        this.entityType = entityType;
    }

    public String getEntityType() {
        return entityType;
    }
}
