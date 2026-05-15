package com.javfxtutorial.hethongdaugia.client.handler;

import com.javfxtutorial.hethongdaugia.common.Exception.auc.AuctionAlreadyEndedException;
import com.javfxtutorial.hethongdaugia.common.Exception.auc.AuctionNotFoundException;
import com.javfxtutorial.hethongdaugia.common.Exception.auc.AuctionNotStartedException;
import com.javfxtutorial.hethongdaugia.common.Exception.auth.UserAlreadyExistsException;
import com.javfxtutorial.hethongdaugia.common.Exception.auth.UserNotFoundException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.BidAmountExceedsLimitException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.InsufficientIncrementException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.LowerThanCurrentBidException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.SelfBidException;
import com.javfxtutorial.hethongdaugia.common.Exception.bus.ConcurrentAccessException;
import com.javfxtutorial.hethongdaugia.common.Exception.bus.InvalidInputException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.*;
import com.javfxtutorial.hethongdaugia.common.Exception.sys.ResourceExhaustedException;
import com.javfxtutorial.hethongdaugia.common.Exception.sys.UnknownSystemException;
import com.javfxtutorial.hethongdaugia.common.exception.bid.BidConflictException;
import com.javfxtutorial.hethongdaugia.common.Exception.net.*;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Bộ xử lý ngoại lệ toàn cục cho phía Client (JavaFX)
 */

public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler{
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static GlobalExceptionHandler instance;

    private GlobalExceptionHandler(){}

    public static GlobalExceptionHandler getInstance(){
        if (instance == null){
            synchronized (GlobalExceptionHandler.class){
                if(instance==null){
                    instance = new GlobalExceptionHandler();
                }
            }
        }
        return instance;
    }
    /**
     * Phương thức chính được gọi khi có uncaught exception
     *
     * @param thread Luồng nơi xảy ra lỗi
     * @param throwable Ngoại lệ không được catch
     */
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        log.error("Uncatch exception in thread {} : {}", thread.getName(), throwable.getMessage(), throwable);
        //xu ly tren UI thread
        Platform.runLater(() -> handleException(throwable));
    }
    /**
     * Xử lý ngoại lệ và hiển thị thông báo phù hợp
     *
     * @param throwable Ngoại lệ cần xử lý
     */
    public void handleException(Throwable throwable){
        if(throwable == null) return;
        // ========== 1. LỖI MẠNG (NETWORK ERRORS) ==========
        if(throwable instanceof ConnectionTimeoutException){
            ConnectionTimeoutException e = (ConnectionTimeoutException) throwable;
            showErrorAlert(
                    "Lỗi kết nối",
                    "Không thể kết nối đến server",
                    String.format("Kết nối đến %s bị timeout sau %d giây.\nVui lòng kiểm tra mạng và thử lại.",
                            e.getServerAddress(), e.getTimeoutSeconds()),
                    "Kiểm tra kết nối mạng hoặc thử lại sau"
            );
        }
        else if (throwable instanceof ConnectionFailedException) {
            ConnectionFailedException e = (ConnectionFailedException) throwable;
            boolean shouldReconnect = showConfirmAlert(
                    "Mất kết nối",
                    "Kết nối đến server bị gián đoạn",
                    String.format("Đã mất kết nối đến %s.\nHệ thống sẽ tự động kết nối lại.",
                            e.getServerAddress()),
                    "Thử kết nối lại"
            );
//            if (shouldReconnect) {
//                // Kích hoạt reconnect
//                reconnectToServer();
//            }
        }
        else if (throwable instanceof ServerUnvailableException) {
            ServerUnvailableException e = (ServerUnvailableException) throwable;
            showErrorAlert(
                    "Server không khả dụng",
                    "Hệ thống không phản hồi",
                    "Server hiện đang không khả dụng. Vui lòng thử lại sau.",
                    "Thử lại sau"
            );
        }
        else if (throwable instanceof ReceiveFailedException) {
            ReceiveFailedException e = (ReceiveFailedException) throwable;
            showErrorAlert(
                    "Nhận dữ liệu thất bại",
                    "Không thể nhận dữ liệu",
                    "\"Máy chủ hiện đang quá tải. Vui lòng thử lại sau.",
                    "Thử lại sau"
            );}
        else if (throwable instanceof SendFailedException) {
            SendFailedException e = (SendFailedException) throwable;
            showErrorAlert(
                    "Gửi dữ liệu thất bại",
                    "Không thể gửi dữ liệu",
                    "\"Máy chủ hiện đang quá tải. Vui lòng thử lại sau.",
                    "Thử lại sau"
            );}
        else if (throwable instanceof NetworkException) {
            showErrorAlert(
                    "Lỗi mạng",
                    "Có lỗi xảy ra khi kết nối đến server",
                    throwable.getMessage(),
                    "Vui lòng kiểm tra kết nối mạng"
            );
        }
        // ========== 2. LỖI XÁC THỰC (AUTHENTICATION ERRORS) ==========
        else if (throwable instanceof UserNotFoundException) {
            showErrorAlert(
                    "Lỗi xác thực",
                    "Không tìm thấy người dùng",
                    throwable.getMessage(),
                    "Vui lòng xác thực lại thông tin tìm kiếm."
            );}
        // ========== 3. LỖI ĐẤU GIÁ  ==========
        else if (throwable instanceof LowerThanCurrentBidException) {
            LowerThanCurrentBidException e =
                    (LowerThanCurrentBidException) throwable;
            showErrorAlert(
                    "Giá đặt không hợp lệ",
                    "Giá đặt phải cao hơn giá hiện tại",
                    String.format("Giá hiện tại: %,.0f VND",
                            e.getCurrentPrice()),
                    "Vui lòng đặt giá cao hơn"
            );
        }
        else if (throwable instanceof InsufficientIncrementException) {
            InsufficientIncrementException e =
                    (InsufficientIncrementException) throwable;
            showErrorAlert(
                    "Bước giá không đủ",
                    "Giá đặt phải tăng ít nhất một bước giá",
                    e.getMessage(),
                    "Vui lòng đặt giá cao hơn"
            );
        }
        else if (throwable instanceof SelfBidException) {
            showErrorAlert(
                    "Không thể tự đặt giá",
                    "Bạn không thể đặt giá cho sản phẩm của chính mình",
                    throwable.getMessage(),
                    "Vui lòng tham gia các phiên đấu giá khác"
            );
        }
        else if (throwable instanceof BidAmountExceedsLimitException) {
            showErrorAlert(
                    "Giá đấu không hợp lệ",
                    "Giá đấu không được vượt quá mực giớ hạn",
                    throwable.getMessage(),
                    "Vui lòng đặt giá thấp hơn "
            );}
        else if (throwable instanceof AuctionAlreadyEndedException) {
            showErrorAlert(
                    "Phiên đấu giá đã kết thúc",
                    "Phiên đấu giá này đã đóng",
                    throwable.getMessage(),
                    "Vui lòng tham gia phiên đấu giá khác"
            );
        }
        else if (throwable instanceof AuctionNotStartedException) {
            showErrorAlert(
                    "Phiên đấu giá chưa bắt đầu",
                    "Phiên đấu giá này chưa diễn ra",
                    throwable.getMessage(),
                    "Vui lòng quay lại đúng giờ"
            );
        }
        else if (throwable instanceof AuctionNotFoundException) {
            showErrorAlert(
                    "Không tìm thấy phiên đấu giá",
                    "Phiên đấu giá không tồn tại",
                    throwable.getMessage(),
                    "Vui lòng làm mới trang"
            );}
//        else if (throwable instanceof BidConflictException) {
//            boolean shouldRetry = showConfirmAlert(
//                    "Xung đột đặt giá",
//                    "Có người vừa đặt giá trước bạn",
//                    "Giá hiện tại đã thay đổi. Bạn có muốn làm mới và thử lại không?",
//                    "Làm mới và thử lại"
//            );
//            if (shouldRetry) {
//                refreshCurrentPage();
//            }
//        }
        // ========== 4. LỖI NGHIỆP VỤ (BUSINESS RULES) ==========
        else if (throwable instanceof InvalidInputException) {
            InvalidInputException e = (InvalidInputException) throwable;
            String fieldInfo = "";
            if (e.getFieldName() != null) {
                fieldInfo = " - Trường: " + e.getFieldName();
                if (e.getInvalidValue() != null) {
                    fieldInfo += ", Giá trị: '" + e.getInvalidValue() + "'";
                }
            }
            showErrorAlert(
                    "Dữ liệu không hợp lệ",
                    "Vui lòng kiểm tra lại thông tin nhập",
                    e.getMessage() + fieldInfo,
                    "Vui lòng sửa lại dữ liệu theo hướng dẫn"
            );
        }

//        else if (throwable instanceof ConcurrentAccessException) {
//            ConcurrentAccessException e = (ConcurrentAccessException) throwable;
//            boolean shouldRetry = showConfirmAlert(
//                    "Xung đột dữ liệu",
//                    "Có người khác đang thao tác cùng lúc",
//                    e.getMessage() + "\n\nBạn có muốn thử lại không?",
//                    "Thử lại"
//            );
//            if (shouldRetry) {
//                refreshCurrentPage();
//            }
//        }
        // ========== 5. LỖI DỮ LIỆU (DATA ERRORS) ==========
        else if (throwable instanceof EntityNotFoundException) {
            EntityNotFoundException e = (EntityNotFoundException) throwable;
            showErrorAlert(
                    "Không tìm thấy dữ liệu",
                    "Không thể tìm thấy thông tin yêu cầu",
                    e.getMessage() != null ? e.getMessage() : "Dữ liệu không tồn tại trong hệ thống",
                    "Vui lòng kiểm tra lại ID hoặc làm mới trang"
            );
        }

        else if (throwable instanceof DuplicateKeyException) {
            DuplicateKeyException e = (DuplicateKeyException) throwable;
            showErrorAlert(
                    "Dữ liệu đã tồn tại",
                    "Không thể thêm mới do trùng lặp",
                    e.getMessage() != null ? e.getMessage() : "Bản ghi đã tồn tại trong hệ thống",
                    "Vui lòng sử dụng thông tin khác"
            );
        }

        else if (throwable instanceof DatabaseConnectionException) {
            DatabaseConnectionException e = (DatabaseConnectionException) throwable;
            showErrorAlert(
                    "Lỗi kết nối dữ liệu",
                    "Không thể kết nối đến cơ sở dữ liệu",
                    "Hệ thống đang gặp sự cố kết nối. Vui lòng thử lại sau.",
                    "Kiểm tra kết nối mạng hoặc liên hệ quản trị viên"
            );
        }

        else if (throwable instanceof QueryExecutionException) {
            QueryExecutionException e = (QueryExecutionException) throwable;
            showErrorAlert(
                    "Lỗi truy vấn dữ liệu",
                    "Có lỗi xảy ra khi truy xuất dữ liệu",
                    "Vui lòng thử lại sau.",
                    "Liên hệ hỗ trợ nếu lỗi tiếp diễn"
            );
        }

        else if (throwable instanceof DataInsertException) {
            showErrorAlert(
                    "Lỗi thêm dữ liệu",
                    "Không thể thêm dữ liệu vào hệ thống",
                    throwable.getMessage() != null ? throwable.getMessage() : "Lỗi khi thêm bản ghi mới",
                    "Vui lòng kiểm tra lại thông tin và thử lại"
            );
        }

        else if (throwable instanceof DataUpdateException) {
            showErrorAlert(
                    "Lỗi cập nhật dữ liệu",
                    "Không thể cập nhật thông tin",
                    throwable.getMessage() != null ? throwable.getMessage() : "Lỗi khi cập nhật bản ghi",
                    "Vui lòng làm mới trang và thử lại"
            );
        }

        else if (throwable instanceof DataDeleteException) {
            showErrorAlert(
                    "Lỗi xóa dữ liệu",
                    "Không thể xóa dữ liệu",
                    throwable.getMessage() != null ? throwable.getMessage() : "Lỗi khi xóa bản ghi",
                    "Vui lòng kiểm tra lại hoặc liên hệ hỗ trợ"
            );
        }
        // ========== 6. LỖI HỆ THỐNG (SYSTEM ERRORS) ==========
        else if (throwable instanceof NullPointerException) {
            log.error("NullPointerException - Đây là lỗi lập trình cần sửa", throwable);
            showErrorAlert(
                    "Lỗi hệ thống",
                    "Đã xảy ra lỗi xử lý dữ liệu",
                    "Vui lòng thử lại. Nếu lỗi tiếp diễn, vui lòng liên hệ hỗ trợ.",
                    "Báo cáo cho quản trị viên"
            );
        }
        else if (throwable instanceof IllegalArgumentException) {
            showErrorAlert(
                    "Tham số không hợp lệ",
                    "Dữ liệu đầu vào không đúng định dạng",
                    throwable.getMessage(),
                    "Vui lòng kiểm tra lại dữ liệu nhập"
            );
        }
        else if (throwable instanceof UnknownSystemException) {
            log.error("Lỗi hệ thống không xác định", throwable);
            showErrorAlert("Lỗi hệ thống", "Đã xảy ra lỗi không mong đợi",
                    "Vui lòng thử lại sau", "Liên hệ hỗ trợ nếu lỗi tiếp diễn");
        }

        else if (throwable instanceof ResourceExhaustedException) {
            showErrorAlert("Hệ thống quá tải", "Server đang bận",
                    "Hệ thống đang xử lý nhiều yêu cầu, vui lòng thử lại sau vài phút",
                    "Thử lại sau");
        }

        //continue...
        // ========== 7. LỖI KHÁC (UNKNOWN) ==========
        else {
            // Ghi log stack trace chi tiết
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            log.error("Unknown error: {}", sw.toString());

            showErrorAlert(
                    "Lỗi không xác định",
                    "Đã xảy ra lỗi không mong đợi",
                    "Vui lòng thử lại. Nếu lỗi tiếp diễn, vui lòng liên hệ bộ phận hỗ trợ.\n\n" +
                            "Chi tiết: " + throwable.getClass().getSimpleName() + " - " + throwable.getMessage(),
                    "Liên hệ hỗ trợ kỹ thuật"
            );
        }

    }
    /**
     * Xử lý exception từ Server response (đã được đóng gói)
     *
     * @param errorCode Mã lỗi từ server
     * @param message Thông báo lỗi
     */
    public void handleServerError(String errorCode, String message) {
        log.warn("Server error: {} - {}", errorCode, message);

        Platform.runLater(() -> {
            String title = "Lỗi từ server";
            String suggestion = "Vui lòng thử lại sau";

            // Map error code sang thông báo thân thiện
            if (errorCode != null) {
                if (errorCode.startsWith("AUTH-")) {
                    title = "Lỗi xác thực";
                    suggestion = "Vui lòng đăng nhập lại";
                } else if (errorCode.startsWith("BID-")) {
                    title = "Lỗi đặt giá";
                    suggestion = "Vui lòng kiểm tra lại giá đặt";
                } else if (errorCode.startsWith("AUC-")) {
                    title = "Lỗi phiên đấu giá";
                    suggestion = "Vui lòng làm mới trang";
                } else if (errorCode.startsWith("DATA-")) {
                    title = "Lỗi dữ liệu";
                    suggestion = "Vui lòng thử lại sau";
                }
            }

            showErrorAlert(title, "Thao tác thất bại", message, suggestion);
        });}
    private void showErrorAlert(String title, String header, String content, String suggestion) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        if (suggestion != null && !suggestion.isEmpty()) {
            alert.setContentText(content + "\n\n Gợi ý: " + suggestion);
        }

        alert.showAndWait();
    }
    public boolean showConfirmAlert(String title, String header, String content, String confirmText) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        ButtonType confirmButton = new ButtonType(confirmText);
        ButtonType cancelButton = new ButtonType("Hủy");
        alert.getButtonTypes().setAll(confirmButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == confirmButton;
    }
    /**
     * Đăng ký global exception handler cho client
     * Gọi phương thức này ngay khi khởi động ứng dụng
     */
    public static void register() {
        GlobalExceptionHandler handler = getInstance();
        Thread.setDefaultUncaughtExceptionHandler(handler);
        log.info("GlobalExceptionHandler đã được đăng ký thành công");
    }

    /**
     * Đăng ký cho JavaFX Application Thread
     */
    public static void registerForJavaFX() {
        GlobalExceptionHandler handler = getInstance();

        // Lưu handler cũ nếu có
        Thread.UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            handler.uncaughtException(thread, throwable);
            if (oldHandler != null && oldHandler != Thread.getDefaultUncaughtExceptionHandler()) {
                oldHandler.uncaughtException(thread, throwable);
            }
        });

        log.info("GlobalExceptionHandler đã được đăng ký cho JavaFX");
    }
}
