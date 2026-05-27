package com.javfxtutorial.hethongdaugia.server.network;

import com.javfxtutorial.hethongdaugia.common.model.domain.BidTransaction;

public interface BidListener {
  public void onPlaceBid(BidTransaction bid);
}
