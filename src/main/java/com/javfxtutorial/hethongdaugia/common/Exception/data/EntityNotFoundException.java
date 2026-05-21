package com.javfxtutorial.hethongdaugia.common.Exception.data;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

/** Lỗi không tìm thấy entity trong database */
public class EntityNotFoundException extends DataException {
    private static final long serialVersionUID = 1L;
    private final String entityType;
    private final int entityId;
    private final String entityName;

    public EntityNotFoundException(String entityType, int entityId) {
        super(
                ErrorCode.DATA_NOT_FOUND,
                String.format("Không tìm thấy %s với ID: %s", entityType, entityId));
        this.entityId = entityId;
        this.entityType = entityType;
        this.entityName = null;
    }

    public EntityNotFoundException(String entityType, String entityName) {
        super(
                ErrorCode.DATA_NOT_FOUND,
                String.format("Không tìm thấy %s với tên: %s", entityType, entityName));
        this.entityId = 0;
        this.entityName = entityName;
        this.entityType = entityType;
    }

    public String getEntityType() {
        return entityType;
    }

    public int getEntityId() {
        return entityId;
    }

    public String getEntityName() {
        return entityName;
    }
}
