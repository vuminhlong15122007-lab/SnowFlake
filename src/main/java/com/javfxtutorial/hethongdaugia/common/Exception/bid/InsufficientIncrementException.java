package com.javfxtutorial.hethongdaugia.common.Exception.bid;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

public class InsufficientIncrementException extends BidException {
  private static final long serialVersionUID = 1L;
  private final double minIncrement;
  private final double actualIncrement;

  public InsufficientIncrementException() {
    super(ErrorCode.BID_MINIMUM_NOT_MET);
    this.minIncrement = 0;
    this.actualIncrement = 0;
  }

  public InsufficientIncrementException(double minIncrement, double actualIncrement) {
    super(
        ErrorCode.BID_MINIMUM_NOT_MET,
        String.format(
            "Bước giá %.2f VNĐ không đạt mức tối thiểu %.2f VNĐ", actualIncrement, minIncrement));
    this.minIncrement = minIncrement;
    this.actualIncrement = actualIncrement;
  }

  public double getMinIncrement() {
    return minIncrement;
  }

  public double getActualIncrement() {
    return actualIncrement;
  }
}
