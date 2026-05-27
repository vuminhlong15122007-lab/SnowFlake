package com.javfxtutorial.hethongdaugia.common.Exception.auc;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

public class AuctionAlreadyEndedException extends AuctionException {
  private static final long serialVersionUID = 1L;
  private final int auctionId;

  public AuctionAlreadyEndedException(int auctionId) {
    super(ErrorCode.AUC_ALREADY_ENDED, String.format("Phiên đấu giá '%s' đã kết thúc", auctionId));
    this.auctionId = auctionId;
  }

  public int getAuctionId() {
    return auctionId;
  }
}
