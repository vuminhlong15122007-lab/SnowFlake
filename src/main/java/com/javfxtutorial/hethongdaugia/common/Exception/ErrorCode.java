package com.javfxtutorial.hethongdaugia.common.Exception;

public enum ErrorCode {
    //loi xac thuc
    AUTH_INVALID_CREDENTIALS("AUTH-001", "Sai tên đăng nhập hoặc mật khẩu", 401),

    AUTH_USER_ALREADY_EXISTS("AUTH-002", "Tài khoản đã tồn tại", 400),

    AUTH_USER_NOTFOUND("AUTH-003", "Không tìm thấy người dùng", 404),

    //loi dau gia

    BID_LOWER_THAN_CURRENT("BID-001", "Giá đặt phải cao hơn giá hiện tại", 400),

    BID_SELF_BID("BID-002", "Không thể tự đặt giá sản phẩm của mình", 400),

    BID_MINIMUM_NOT_MET("BID-003", "Giá đặt chưa đạt mức tối thiểu", 400),

    BID_AMOUNT_EXCEEDS_LIMIT("BID-004", "Giá đấu vượt quá giới hạn", 400),

    //loi phien dau gia

    AUC_NOT_ACTIVE("AUC-001", "Phiên đấu giá chưa bắt đầu hoặc đã kết thúc", 400),

    AUC_EXPIRED("AUC-002", "Phiên đấu giá đã hết hạn", 400),

    AUC_NOT_FOUND("AUC-003", "Không tìm thấy phiên đấu giá", 404),

    AUC_ALREADY_ENDED("AUC-004", "Phiên đấu giá đã kết thúc từ trước", 400),

    AUC_NOT_STARTED("AUC-005", "Phiên đấu giá chưa bắt đầu", 400),

    //loi data

    DATA_NOT_FOUND("DATA-001", "Không tìm thấy dữ liệu", 404),

    DATA_DUPLICATE("DATA-002", "Dữ liệu đã tồn tại", 409),

    DATA_CONNECTION_FAILED("DATA-003", "Không thể kết nối đến database", 503),

    DATA_QUERY_FAILED("DATA-004", "Lỗi khi truy vấn dữ liệu", 500),

    DATA_INSERT_FAILED("DATA-005", "Lỗi khi thêm dữ liệu", 500),

    DATA_UPDATE_FAILED("DATA-006", "Lỗi khi cập nhật dữ liệu", 500),

    DATA_DELETE_FAILED("DATA-007", "Lỗi khi xóa dữ liệu", 500),

    //loi network

    NET_CONNECTION_TIMEOUT("NET-001", "Kết nối đến server bị timeout", 408),

    NET_CONNECTION_LOST("NET-002", "Mất kết nối đến server", 503),

    NET_SEND_FAILED("NET-003", "Gửi dữ liệu thất bại", 500),

    NET_RECEIVE_FAILED("NET-004", "Nhận dữ liệu thất bại", 500),

    NET_SERVER_UNVAILABLE("NET-005", "Server hiện không khả dụng", 503),

    //loi nghiep vu

    BIZ_INVALID_INPUT("BIZ-001", "Dữ liệu đầu vào không hợp lệ", 400),

    BIZ_CONCURRENT_ACCESS("BIZ-002", "Có xung đột dữ liệu", 409),

    //loi he thong

    SYS_UNKNOW_ERROR("SYS-001", "Đã xảy ra lỗi không xác định", 500),

    SYS_RESOURCE_EXHAUSTED("SYS-002", "Hệ thống quá tải", 503);

    private final String code;
    private final String defaultMessage;
    private final int httpStatusCode;

    ErrorCode(String code, String defaultMessage, int httpStatusCode) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.httpStatusCode = httpStatusCode;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }
    //tim loi theo ma loi
    public static ErrorCode fromCode(String code){
        for(ErrorCode errorCode : values()){
            if ((errorCode.code.equals(code))){
                return errorCode;
            }
        }
        return SYS_UNKNOW_ERROR;
    }
}
