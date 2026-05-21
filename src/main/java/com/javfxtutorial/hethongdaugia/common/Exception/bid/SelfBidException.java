package com.javfxtutorial.hethongdaugia.common.Exception.bid;

import com.javfxtutorial.hethongdaugia.common.Exception.ErrorCode;

public class SelfBidException extends BidException {
    private static final long serialVersionUID = 1L;
    private final int userId;
    private final int itemId;

    public SelfBidException() {
        super(ErrorCode.BID_SELF_BID);
        this.userId = 0;
        this.itemId = 0;
    }

    public SelfBidException(int userId, int itemId) {
        super(
                ErrorCode.BID_SELF_BID,
                String.format(
                        "Người dùng %s không thể đặt giá cho sản phẩm %s của chính mình",
                        userId, itemId));
        this.userId = userId;
        this.itemId = itemId;
    }

    public int getUserId() {
        return userId;
    }

    public int getItemId() {
        return itemId;
    }
}
