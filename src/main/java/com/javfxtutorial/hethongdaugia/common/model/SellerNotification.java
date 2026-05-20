package com.javfxtutorial.hethongdaugia.common.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SellerNotification implements Serializable {
    private static int idCounter = 0;

    public enum Type {
        CLOSED,
        PAID,
        CANCELLED
    }

    private int notificationId;
    private int auctionId;
    private Type type;
    private String productName;
    private String winnerName;
    private BigDecimal winningPrice;
    private LocalDateTime createdAt;
    private boolean read;

    public SellerNotification() {}

    public SellerNotification(
            int auctionId,
            Type type,
            String productName,
            String winnerName,
            BigDecimal winningPrice) {
        idCounter += 1;
        this.auctionId = auctionId;
        this.type = type;
        this.productName = productName;
        this.winnerName = winnerName;
        this.winningPrice = winningPrice;
        this.createdAt = LocalDateTime.now();
        this.read = false;
        this.notificationId = idCounter;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int v) {
        notificationId = v;
    }

    public int getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(int v) {
        auctionId = v;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String v) {
        productName = v;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String v) {
        winnerName = v;
    }

    public BigDecimal getWinningPrice() {
        return winningPrice;
    }

    public void setWinningPrice(BigDecimal v) {
        winningPrice = v;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime v) {
        createdAt = v;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getMessage() {
        String price = String.format("%,.0f", winningPrice);
        return switch (type) {
            case CLOSED ->
                    String.format(
                            "🏆 Phiên \"%s\" kết thúc!\nNgười thắng: %s — Giá: %s VND\nĐang chờ họ thanh toán.",
                            productName, winnerName, price);
            case PAID ->
                    String.format(
                            "✅ Phiên \"%s\" đã được thanh toán!\n%s đã thanh toán thành công %s VND.\nChuẩn bị giao hàng cho họ nhé!",
                            productName, winnerName, price);
            case CANCELLED ->
                    String.format(
                            "❌ Phiên \"%s\" bị hủy!\n%s không thanh toán trong 24h.\nBạn có thể kiện nếu cần.",
                            productName, winnerName);
        };
    }
}
