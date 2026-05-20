package com.javfxtutorial.hethongdaugia.common.Exception.bid;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

public class BidAmountExceedsLimitException extends BidException {
    private static final long serialVersionUID = 1L;
    private final double maxLimit;
    private final double bidAmount;

    public BidAmountExceedsLimitException() {
        super(ErrorCode.BID_AMOUNT_EXCEEDS_LIMIT);
        this.maxLimit = 0;
        this.bidAmount = 0;
    }

    public BidAmountExceedsLimitException(double maxLimit, double bidAmount) {
        super(
                ErrorCode.BID_AMOUNT_EXCEEDS_LIMIT,
                String.format("Giá đấu %.2f VNĐ vượt quá giới hạn %.2f VNĐ", bidAmount, maxLimit));
        this.bidAmount = bidAmount;
        this.maxLimit = maxLimit;
    }

    public double getMaxLimit() {
        return maxLimit;
    }

    public double getBidAmount() {
        return bidAmount;
    }
}
