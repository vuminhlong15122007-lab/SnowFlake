package com.javfxtutorial.hethongdaugia.server.manager;

import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import com.javfxtutorial.hethongdaugia.common.model.domain.AutoBidConfig;
import com.javfxtutorial.hethongdaugia.common.model.domain.BidTransaction;
import com.javfxtutorial.hethongdaugia.server.network.BidListener;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandlerContextHolder;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

final class TestStateSupport {
  private TestStateSupport() {}

  static void resetAuctionManager(AuctionManager manager) throws Exception {
    auctionSubscribers(manager).clear();
    activeAuctions(manager).clear();
    autoBidRegistry(manager).clear();
    ClientHandlerContextHolder.clear();
  }

  @SuppressWarnings("unchecked")
  static Map<Integer, List<BidListener>> auctionSubscribers(AuctionManager manager)
      throws Exception {
    return (Map<Integer, List<BidListener>>) getField(manager, "auctionSubscribers");
  }

  @SuppressWarnings("unchecked")
  static Map<Integer, Auction> activeAuctions(AuctionManager manager) throws Exception {
    return (Map<Integer, Auction>) getField(manager, "activeAuctions");
  }

  @SuppressWarnings("unchecked")
  static Map<Integer, List<AutoBidConfig>> autoBidRegistry(AuctionManager manager)
      throws Exception {
    return (Map<Integer, List<AutoBidConfig>>) getField(manager, "autoBidRegistry");
  }

  static void notifySubscribers(AuctionManager manager, int auctionId, BidTransaction bid)
      throws Exception {
    Method method =
        AuctionManager.class.getDeclaredMethod("notifySubscribers", int.class, BidTransaction.class);
    method.setAccessible(true);
    method.invoke(manager, auctionId, bid);
  }

  static void executeAutoBidCheck(AuctionManager manager, Auction auction) throws Exception {
    Method method =
        AuctionManager.class.getDeclaredMethod("checkAndExecuteAutoBids", Auction.class);
    method.setAccessible(true);
    method.invoke(manager, auction);
  }

  private static Object getField(Object target, String fieldName) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    return field.get(target);
  }
}
