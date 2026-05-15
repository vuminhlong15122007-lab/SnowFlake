package com.javfxtutorial.hethongdaugia.common.Exception.bus;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

public class ConcurrentAccessException extends BusinessException {
    private static final long serialVersionUID = 1L;

    private final String entityType;
    private final String entityId;

    public ConcurrentAccessException() {
        super(ErrorCode.BIZ_CONCURRENT_ACCESS);
        this.entityType = null;
        this.entityId = null;
    }

    public ConcurrentAccessException(String entityType, String entityId) {
        super(ErrorCode.BIZ_CONCURRENT_ACCESS,
                String.format("Xung đột dữ liệu trên %s [%s], vui lòng thử lại", entityType, entityId));
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public ConcurrentAccessException(String message) {
        super(ErrorCode.BIZ_CONCURRENT_ACCESS, message);
        this.entityType = null;
        this.entityId = null;
    }

    public String getEntityType() { return entityType; }
    public String getEntityId() { return entityId; }
}
