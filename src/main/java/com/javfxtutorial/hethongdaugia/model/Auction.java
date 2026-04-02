package com.javfxtutorial.hethongdaugia.model;

import com.javfxtutorial.hethongdaugia.model.enums.AuctionStatus;

import java.time.LocalDate;

public class Auction {
    private String auctionId;

    // Liên kết với sản phẩm đang được đấu giá
    // (Trong CSDL đây sẽ là Khóa ngoại - Foreign Key)
    private String itemId;

    // Ai là người tổ chức phiên đấu giá này
    private int sellerId;

    // Các thông tin về giá
    private double initPrice;
    private double currentPrice;
    private double stepPrice;
    private double winningPrice; // Giá chốt cuối cùng (nếu có)

    // Thông tin về thời gian
    private LocalDate startingTime;
    private LocalDate endingTime;

    // Trạng thái của phiên đấu giá (VD: PENDING, ONGOING, ENDED, CANCELLED)
    private AuctionStatus status;

    // ID của người chiến thắng (sau khi phiên kết thúc)
    private int winnerId;
}
