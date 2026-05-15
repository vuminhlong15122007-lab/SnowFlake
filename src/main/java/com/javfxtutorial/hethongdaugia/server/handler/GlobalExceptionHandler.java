package com.javfxtutorial.hethongdaugia.server.handler;

import com.javfxtutorial.hethongdaugia.common.Exception.auc.AuctionAlreadyEndedException;
import com.javfxtutorial.hethongdaugia.common.Exception.auc.AuctionNotFoundException;
import com.javfxtutorial.hethongdaugia.common.Exception.auc.AuctionNotStartedException;
import com.javfxtutorial.hethongdaugia.common.Exception.auth.InvalidCredentialsException;
import com.javfxtutorial.hethongdaugia.common.Exception.auth.UserAlreadyExistsException;
import com.javfxtutorial.hethongdaugia.common.Exception.auth.UserNotFoundException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.BidAmountExceedsLimitException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.InsufficientIncrementException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.LowerThanCurrentBidException;
import com.javfxtutorial.hethongdaugia.common.Exception.bid.SelfBidException;
import com.javfxtutorial.hethongdaugia.common.Exception.bus.ConcurrentAccessException;
import com.javfxtutorial.hethongdaugia.common.Exception.bus.InvalidInputException;
import com.javfxtutorial.hethongdaugia.common.Exception.data.*;
import com.javfxtutorial.hethongdaugia.common.Exception.net.*;
import com.javfxtutorial.hethongdaugia.common.Exception.sys.ResourceExhaustedException;
import com.javfxtutorial.hethongdaugia.common.Exception.sys.UnknownSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static GlobalExceptionHandler instance;

    private GlobalExceptionHandler() {}

    public static GlobalExceptionHandler getInstance() {
        if (instance == null) {
            synchronized (GlobalExceptionHandler.class) {
                if (instance == null) {
                    instance = new GlobalExceptionHandler();
                }
            }
        }
        return instance;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        String timestamp = LocalDateTime.now().format(TIME_FORMATTER);

        log.error("UNCAUGHT EXCEPTION at: {}", timestamp);
        log.error("Thread: {} (ID: {}, State: {})", thread.getName(), thread.getId(), thread.getState());
        log.error("Exception Type: {}", throwable.getClass().getName());
        log.error("Exception Message: {}", throwable.getMessage());

        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        log.error("Stack Trace:\n{}", sw.toString());

        // Xử lý theo loại
        handleByType(throwable);

        // Gửi cảnh báo nếu lỗi nghiêm trọng
        if (isCritical(throwable)) {
            sendAdminAlert(throwable);
        }
    }

    private void handleByType(Throwable t) {

        // ========== 1. LỖI XÁC THỰC ==========
        if (t instanceof InvalidCredentialsException) {
            log.warn("AUTH-001 - Sai tên đăng nhập hoặc mật khẩu: {}", t.getMessage());
        }
        else if (t instanceof UserAlreadyExistsException) {
            log.warn("AUTH-002 - Tài khoản đã tồn tại: {}", t.getMessage());
        }
        else if (t instanceof UserNotFoundException) {
            log.warn("AUTH-003 - Không tìm thấy người dùng: {}", t.getMessage());
        }

        // ========== 2. LỖI ĐẤU GIÁ ==========
        else if (t instanceof LowerThanCurrentBidException) {
            log.warn("BID-001 - Giá đặt thấp hơn giá hiện tại: {}", t.getMessage());
        }
        else if (t instanceof SelfBidException) {
            log.warn("BID-002 - Tự đặt giá sản phẩm của mình: {}", t.getMessage());
        }
        else if (t instanceof InsufficientIncrementException) {
            log.warn("BID-003 - Giá đặt chưa đạt mức tối thiểu: {}", t.getMessage());
        }
        else if (t instanceof BidAmountExceedsLimitException) {
            log.warn("BID-004 - Giá đấu vượt quá giới hạn: {}", t.getMessage());
        }
        else if (t instanceof com.javfxtutorial.hethongdaugia.common.exception.bid.BidConflictException) {
            log.warn("BID-005 - Xung đột đấu giá: {}", t.getMessage());
        }

        // ========== 3. LỖI PHIÊN ĐẤU GIÁ ==========
        else if (t instanceof AuctionNotFoundException) {
            log.warn("AUC-001 - Không tìm thấy phiên đấu giá: {}", t.getMessage());
        }
        else if (t instanceof AuctionAlreadyEndedException) {
            log.warn("AUC-002 - Phiên đấu giá đã kết thúc: {}", t.getMessage());
        }
        else if (t instanceof AuctionNotStartedException) {
            log.warn("AUC-003 - Phiên đấu giá chưa bắt đầu: {}", t.getMessage());
        }

        // ========== 4. LỖI DỮ LIỆU ==========
        else if (t instanceof EntityNotFoundException) {
            log.warn("DATA-001 - Không tìm thấy dữ liệu: {}", t.getMessage());
        }
        else if (t instanceof DuplicateKeyException) {
            log.warn("DATA-002 - Dữ liệu đã tồn tại: {}", t.getMessage());
        }
        else if (t instanceof DatabaseConnectionException) {
            log.error("DATA-003 - Không thể kết nối database: {}", t.getMessage());
        }
        else if (t instanceof QueryExecutionException) {
            log.error("DATA-004 - Lỗi truy vấn dữ liệu: {}", t.getMessage());
        }
        else if (t instanceof DataInsertException) {
            log.error("DATA-005 - Lỗi thêm dữ liệu: {}", t.getMessage());
        }
        else if (t instanceof DataUpdateException) {
            log.error("DATA-006 - Lỗi cập nhật dữ liệu: {}", t.getMessage());
        }
        else if (t instanceof DataDeleteException) {
            log.error("DATA-007 - Lỗi xóa dữ liệu: {}", t.getMessage());
        }

        // ========== 5. LỖI MẠNG ==========
        else if (t instanceof ConnectionTimeoutException) {
            log.error("NET-001 - Kết nối timeout: {}", t.getMessage());
        }
        else if (t instanceof ConnectionFailedException) {
            log.error("NET-002 - Mất kết nối server: {}", t.getMessage());
        }
        else if (t instanceof SendFailedException) {
            log.error("NET-003 - Gửi dữ liệu thất bại: {}", t.getMessage());
        }
        else if (t instanceof ReceiveFailedException) {
            log.error("NET-004 - Nhận dữ liệu thất bại: {}", t.getMessage());
        }
        else if (t instanceof ServerUnvailableException) {
            log.error("NET-005 - Server không khả dụng: {}", t.getMessage());
        }

        // ========== 6. LỖI NGHIỆP VỤ ==========
        else if (t instanceof InvalidInputException) {
            log.warn("BIZ-001 - Dữ liệu đầu vào không hợp lệ: {}", t.getMessage());
        }
        else if (t instanceof ConcurrentAccessException) {
            log.warn("BIZ-002 - Xung đột dữ liệu: {}", t.getMessage());
        }

        // ========== 7. LỖI HỆ THỐNG ==========
        else if (t instanceof UnknownSystemException) {
            log.error("SYS-001 - Lỗi không xác định: {}", t.getMessage(), t);
        }
        else if (t instanceof ResourceExhaustedException) {
            log.error("SYS-002 - Hệ thống quá tải: {}", t.getMessage());
            logMemoryStatus();
        }

        // ========== 8. LỖI JVM ==========
        else if (t instanceof OutOfMemoryError) {
            log.error("OUT_OF_MEMORY - Hết bộ nhớ");
            logMemoryStatus();
            System.gc();
        }
        else if (t instanceof StackOverflowError) {
            log.error("STACK_OVERFLOW - Có thể do đệ quy vô hạn");
        }

        // ========== 9. LỖI RUNTIME THÔNG DỤNG (CHƯA PHÂN LOẠI) ==========

        else if (t instanceof NullPointerException) {
            log.error("RUNTIME-001 - NullPointerException: {}", t.getMessage(), t);
        }

        else if (t instanceof IllegalArgumentException) {
            log.error("RUNTIME-002 - Tham số không hợp lệ: {}", t.getMessage(), t);
        }

        else if (t instanceof NumberFormatException) {
            log.error("RUNTIME-003 - Lỗi định dạng số: {}", t.getMessage(), t);
        }

        else if (t instanceof IndexOutOfBoundsException) {
            log.error("RUNTIME-004 - Chỉ số ngoài phạm vi: {}", t.getMessage(), t);
        }

        else if (t instanceof ClassCastException) {
            log.error("RUNTIME-005 - Lỗi ép kiểu: {}", t.getMessage(), t);
        }

        else if (t instanceof IllegalStateException) {
            log.error("RUNTIME-006 - Trạng thái không hợp lệ: {}", t.getMessage(), t);
        }

        else if (t instanceof ArithmeticException) {
            log.error("RUNTIME-007 - Lỗi tính toán: {}", t.getMessage(), t);
        }

        // ========== 10. LỖI IO ==========

        else if (t instanceof java.io.IOException) {
            log.error("IO-001 - Lỗi nhập/xuất: {}", t.getMessage(), t);
        }

        // ========== 11. FALLBACK - BẮT TẤT CẢ NHỮNG GÌ CÒN LẠI ==========

        else {
            log.error("UNCAUGHT - Lỗi chưa được phân loại: {} - {}",
                    t.getClass().getName(), t.getMessage(), t);
        }
    }

    private boolean isCritical(Throwable t) {
        return t instanceof OutOfMemoryError ||
                t instanceof StackOverflowError ||
                t instanceof ResourceExhaustedException ||
                t instanceof DatabaseConnectionException ||
                t instanceof UnknownSystemException;
    }

    private void sendAdminAlert(Throwable t) {
        log.error(" ADMIN ALERT: {} - {}", t.getClass().getSimpleName(), t.getMessage());
    }

    private void logMemoryStatus() {
        Runtime rt = Runtime.getRuntime();
        long max = rt.maxMemory();
        long total = rt.totalMemory();
        long free = rt.freeMemory();
        long used = total - free;

        log.info("MEMORY - Max: {}MB, Used: {}MB, Free: {}MB, Usage: {}%",
                max / (1024 * 1024),
                used / (1024 * 1024),
                free / (1024 * 1024),
                (used * 100) / max);
    }

    public static void register() {
        GlobalExceptionHandler handler = getInstance();

        Thread.UncaughtExceptionHandler old = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            handler.uncaughtException(thread, throwable);
            if (old != null && old != Thread.getDefaultUncaughtExceptionHandler()) {
                old.uncaughtException(thread, throwable);
            }
        });

        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("SERVER SHUTDOWN at: {}", LocalDateTime.now().format(TIME_FORMATTER));
        }));

        log.info("Server GlobalExceptionHandler registered");
    }
}

