package com.javfxtutorial.hethongdaugia.common.model.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AutoBidConfig implements Serializable {
  private int userId;
  private String userName;
  private int auctionId;
  private BigDecimal maxPrice;
  private boolean isActive;
  private LocalDateTime registeredAt;

  public AutoBidConfig(
      int userId, String userName, int auctionId, BigDecimal maxPrice, boolean isActive) {
    this.userId = userId;
    this.userName = userName;
    this.auctionId = auctionId;
    this.maxPrice = maxPrice;
    this.isActive = isActive;
    this.registeredAt = LocalDateTime.now();
  }

  public LocalDateTime getRegisteredAt() {
    return registeredAt;
  }

  public void setRegisteredAt(LocalDateTime registeredAt) {
    this.registeredAt = registeredAt;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public AutoBidConfig() {}

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public int getAuctionId() {
    return auctionId;
  }

  public void setAuctionId(int auctionId) {
    this.auctionId = auctionId;
  }

  public BigDecimal getMaxPrice() {
    return maxPrice;
  }

  public void setMaxPrice(BigDecimal maxPrice) {
    this.maxPrice = maxPrice;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }
}
