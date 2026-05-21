package com.javfxtutorial.hethongdaugia.common.Exception.data;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

/** Lỗi khi cập nhật dữ liệu vào database */
public class DataUpdateException extends DataException {
    private static final long serialVersionUID = 1L;
    private final int entityId;
    private final String entityType;
    private final String dataType;

    public DataUpdateException(int entityId, String entityType, String dataType) {
        super(
                ErrorCode.DATA_UPDATE_FAILED,
                String.format(
                        "Không thể cập nhật %s của %s - ID: %s", dataType, entityType, entityId));
        this.dataType = dataType;
        this.entityId = entityId;
        this.entityType = entityType;
    }

    public int getEntityId() {
        return entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getDataType() {
        return dataType;
    }
}
