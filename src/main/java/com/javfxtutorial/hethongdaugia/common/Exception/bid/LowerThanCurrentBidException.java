package com.javfxtutorial.hethongdaugia.common.Exception.bid;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

public class LowerThanCurrentBidException extends BidException {
    private static final long serialVersionUID = 1L;
    private final double currentPrice;
    private final double offeredPrice;

    public LowerThanCurrentBidException() {
        super(ErrorCode.BID_LOWER_THAN_CURRENT);
        this.currentPrice = 0;
        this.offeredPrice = 0;
    }

    public LowerThanCurrentBidException(double currentPrice, double offeredPrice) {
        super(
                ErrorCode.BID_LOWER_THAN_CURRENT,
                String.format(
                        "Giá đấu %.2f VNĐ thấp hơn giá hiện tại %.2f VNĐ",
                        offeredPrice, currentPrice));
        this.currentPrice = currentPrice;
        this.offeredPrice = offeredPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public double getOfferedPrice() {
        return offeredPrice;
    }
}
