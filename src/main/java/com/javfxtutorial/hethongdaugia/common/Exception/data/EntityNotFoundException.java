package com.javfxtutorial.hethongdaugia.common.Exception.data;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

/**
 * Lỗi không tìm thấy entity trong database
 */
public class EntityNotFoundException extends DataException {
    private static final long serialVersionUID = 1L;
    private final String entityType;
    private final int entityId;

    public EntityNotFoundException(String entityType, int entityId){
        super(ErrorCode.DATA_NOT_FOUND,
            String.format("Không tìm thấy %s với ID: %s", entityType, entityId));
        this.entityId = entityId;
        this.entityType = entityType;
    }

    public String getEntityType() {
        return entityType;
    }

    public int getEntityId() {
        return entityId;
    }
}
