package com.javfxtutorial.hethongdaugia.common.model.domain;

import java.time.Duration;
import java.time.LocalDateTime;

public class AntiSnipeExtender {
  // Logic ra hạn : có ng đặt giá trong X s cuối => gia hạn thêm Y s

  private final long triggerSeconds; // X
  private final long extendSeconds; // Y

  public AntiSnipeExtender(long triggerSeconds, long extendSeconds) {
    this.triggerSeconds = triggerSeconds;
    this.extendSeconds = extendSeconds;
  }

  public LocalDateTime applyIfNeeded(Auction auction) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime endingTime = auction.getEndingTime();
    long secondsLeft = Duration.between(now, endingTime).toSeconds();

    if (secondsLeft > 0 && secondsLeft <= triggerSeconds) {
      LocalDateTime newEndingTime = endingTime.plusSeconds(extendSeconds);
      auction.setEndingTime(newEndingTime);
      return newEndingTime;
    }

    return endingTime; // không đổi
  }

  // Kiểm tra xem đủ đk gia hạn ko
  public boolean shouldExtend(Auction auction) {
    long secondsLeft = Duration.between(LocalDateTime.now(), auction.getEndingTime()).toSeconds();
    return secondsLeft > 0 && secondsLeft <= triggerSeconds;
  }
}
