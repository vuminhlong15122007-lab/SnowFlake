package com.javfxtutorial.hethongdaugia.common.model;

import com.javfxtutorial.hethongdaugia.common.model.enums.BidStatus;

import java.io.Serializable;
import java.time.LocalDate;

public class BidTransaction implements Serializable {
    private int bidId;
    private int bidderId;
    private int itemId;
    private double amount;
    private LocalDate timestamp;
    private BidStatus bidStatus;
}

