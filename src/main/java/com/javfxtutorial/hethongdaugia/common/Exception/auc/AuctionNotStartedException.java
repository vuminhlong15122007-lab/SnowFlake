package com.javfxtutorial.hethongdaugia.common.Exception.auc;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

public class AuctionNotStartedException extends AuctionException {
    private static final long serialVersionUID = 1L;
    private final int auctionId;

    public AuctionNotStartedException(int auctionId) {
        super(
                ErrorCode.AUC_NOT_STARTED,
                String.format("Phiên đấu giá '%s' chưa bắt đầu", auctionId));
        this.auctionId = auctionId;
    }

    public int getAuctionId() {
        return auctionId;
    }
}
