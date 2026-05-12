package com.javfxtutorial.hethongdaugia.common.Exception.net;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

/**
 * Lỗi gửi dữ liệu thất bại
 */
public class SendFailedException extends NetworkException {
    private static final long serialVersionUID = 1L;
    private final String dataType;

    public SendFailedException(String dataType) {
        super(ErrorCode.NET_SEND_FAILED,
                String.format("Gửi %s thất bại", dataType));
        this.dataType = dataType;
    }

    public String getDataType() {
        return dataType;
    }
}
