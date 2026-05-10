package com.javfxtutorial.hethongdaugia.common.Exception.data;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

/**
 * Lỗi kết nối database
 */
public class DatabaseConnectionException extends DataException {
    private static final long serialVersionUID = 1L;
    public DatabaseConnectionException(Throwable cause){
        super(ErrorCode.DATA_CONNECTION_FAILED, "Không thể kết nối đến cơ sở dữ liệu", cause);
    }
}
