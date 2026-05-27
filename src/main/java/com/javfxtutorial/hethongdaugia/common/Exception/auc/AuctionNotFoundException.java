package com.javfxtutorial.hethongdaugia.common.Exception.auc;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

public class AuctionNotFoundException extends AuctionException {
  private static final long serialVersionUID = 1L;
  private final int auctionId;

  public AuctionNotFoundException(int auctionId) {
    super(
        ErrorCode.AUC_NOT_FOUND,
        String.format("Không tìm thấy phiên đấu giá với ID: %s", auctionId));
    this.auctionId = auctionId;
  }

  public int getAuctionId() {
    return auctionId;
  }
}
