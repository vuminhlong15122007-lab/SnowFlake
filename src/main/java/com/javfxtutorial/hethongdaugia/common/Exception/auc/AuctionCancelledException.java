package com.javfxtutorial.hethongdaugia.common.Exception.auc;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

public class AuctionCancelledException extends AuctionException {
  private static final long serialVersionUID = 1L;
  private final int auctionId;

  public AuctionCancelledException(int auctionId) {
    super(ErrorCode.AUC_NOT_FOUND, String.format("Phiên đấu giá đã bị hủy bởi Admin"));
    this.auctionId = auctionId;
  }

  public int getAuctionId() {
    return auctionId;
  }
}
