package com.javfxtutorial.hethongdaugia.common.Exception.bid;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

public class BidConflictException extends BidException {
    private static final long serialVersionUID = 1L;

    private final double currentPrice;
    private final double attemptedBid;
    private final String conflictingUserId;

    public BidConflictException() {
        super(ErrorCode.BID_CONFLICT);
        this.currentPrice = 0;
        this.attemptedBid = 0;
        this.conflictingUserId = null;
    }

    public BidConflictException(double currentPrice, double attemptedBid) {
        super(
                ErrorCode.BID_CONFLICT,
                String.format("Giá hiện tại đã thay đổi thành %.0f VNĐ", currentPrice));
        this.currentPrice = currentPrice;
        this.attemptedBid = attemptedBid;
        this.conflictingUserId = null;
    }

    public BidConflictException(
            double currentPrice, double attemptedBid, String conflictingUserId) {
        super(
                ErrorCode.BID_CONFLICT,
                String.format(
                        "Người dùng %s vừa đặt giá %.0f VNĐ", conflictingUserId, currentPrice));
        this.currentPrice = currentPrice;
        this.attemptedBid = attemptedBid;
        this.conflictingUserId = conflictingUserId;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getAttemptedBid() {
        return attemptedBid;
    }

    public String getConflictingUserId() {
        return conflictingUserId;
    }
}
