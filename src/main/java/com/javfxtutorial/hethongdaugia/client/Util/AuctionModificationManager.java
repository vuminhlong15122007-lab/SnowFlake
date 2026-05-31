package com.javfxtutorial.hethongdaugia.client.Util;

import com.javfxtutorial.hethongdaugia.client.model.ClientModel;
import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.client.network.ServerConnection;
import com.javfxtutorial.hethongdaugia.common.Exception.net.ConnectionFailedException;
import com.javfxtutorial.hethongdaugia.common.Exception.net.SendFailedException;
import com.javfxtutorial.hethongdaugia.common.model.Command.AddAuctionCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.DeleteAuctionCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateAuctionCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateAuctionStatusCommand;
import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import com.javfxtutorial.hethongdaugia.common.model.enums.AuctionStatus;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import java.time.LocalDateTime;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuctionModificationManager implements ResponseListener {
  private static final Logger log = LoggerFactory.getLogger(AuctionModificationManager.class);
  private static AuctionModificationManager instance;
  ObservableList<Auction> allAuctionsList = ClientModel.getInstance().getAllAuctions();
  ObservableList<Auction> myAuctionsList = ClientModel.getInstance().getMyAuctions();

  private AuctionModificationManager() {}

  public static AuctionModificationManager getInstance() {
    if (instance == null) {
      instance = new AuctionModificationManager();
    }
    return instance;
  }

  public void start() {
    NetworkManager.getInstance().register(AddAuctionCommand.class, this);
    NetworkManager.getInstance().register(DeleteAuctionCommand.class, this);
    NetworkManager.getInstance().register(UpdateAuctionCommand.class, this);
    NetworkManager.getInstance().register(UpdateAuctionStatusCommand.class, this);
  }

  public void refreshAuctionStatus(List<Auction> auctionList) {
    for (Auction auction : auctionList) {
      AuctionStatus previousStatus = auction.getStatus();
      LocalDateTime now = LocalDateTime.now();

      if (previousStatus.equals(AuctionStatus.CANCELLED)
          || previousStatus.equals(AuctionStatus.CANCELLED_BY_ADMIN)
          || previousStatus.equals(AuctionStatus.PAID)) {
        return;
      }
      if (now.isBefore(auction.getStartingTime())) {
        auction.setStatus(AuctionStatus.NOT_START);
      } else if (now.isAfter(auction.getEndingTime())) {
        auction.setStatus(AuctionStatus.CLOSED);
      } else {
        auction.setStatus(AuctionStatus.RUNNING);
      }
      if (previousStatus != auction.getStatus()) {
        try {
          ServerConnection connection = NetworkManager.getConnection();
          connection.sendCommand(new UpdateAuctionStatusCommand(auction));
        } catch (ConnectionFailedException | SendFailedException e) {
          log.error("Không thể kết nối Server");
        }
      }
    }
  }

  public void updateAuctionInList(List<Auction> listAuction, Auction auction) {
    int foundIndex = -1; // 1. Khai báo 1 biến tạm để lưu vị trí nếu tìm thấy
    // Duyệt list để tìm
    for (int i = 0; i < listAuction.size(); i++) {
      if (auction.getAuctionId()
          == ClientModel.getInstance().getAllAuctions().get(i).getAuctionId()) {
        foundIndex = i; // 2. Tìm thấy rồi thì lưu vị trí i lại
        break; // Thoát vòng lặp cho nhẹ máy
      }
    }
    // 3. Kiểm tra xem có thực sự tìm thấy không (khác -1)
    if (foundIndex != -1) {
      listAuction.set(foundIndex, auction);
    }
  }

  @Override
  public void onResponse(Response rp) {
    // luu sp
    if (rp.getCommand().getClass().equals(AddAuctionCommand.class)) {
      if (rp.isSuccess()) {
        Auction savedAuction = (Auction) rp.getPayLoad();
        log.info("Đã nhận ddc command có auction mới:{}", savedAuction);
        Platform.runLater(
            () -> {
              allAuctionsList.addFirst(savedAuction);
            });
      }
    }

    // delete
    if (rp.getCommand().getClass().equals(DeleteAuctionCommand.class)) {
      if (rp.isSuccess()) {
        DeleteAuctionCommand command = (DeleteAuctionCommand) rp.getCommand();
        Auction selectedAuction =
            (rp.getPayLoad() instanceof Auction) ? (Auction) rp.getPayLoad() : command.getAuction();
        Platform.runLater(
            () -> {
              ClientModel.getInstance()
                  .getAllAuctions()
                  .removeIf(auction -> auction.getAuctionId() == selectedAuction.getAuctionId());
            });
      }
    }

    // suaSp
    if (rp.getCommand().getClass().equals(UpdateAuctionCommand.class)
        || rp.getCommand().getClass().equals(UpdateAuctionStatusCommand.class)) {
      if (rp.isSuccess()) {
        if (!(rp.getPayLoad() instanceof Auction updatedAuction)) return;
        if ("ADMIN_CANCELLED_AUCTION".equals(rp.getMessage())) {
          Platform.runLater(
              () -> allAuctionsList.remove(updatedAuction));
        }
        log.info("Đã nhận ddc command update auction mới:{}", updatedAuction);
        Platform.runLater(
            () -> {
              updateAuctionInList(allAuctionsList, updatedAuction);
              if (ClientModel.getInstance().getCurrentUser() == null) return;
              if (updatedAuction.getSellerId()
                  == ClientModel.getInstance().getCurrentUser().getId()) {
                updateAuctionInList(myAuctionsList, updatedAuction);
              }
            });
      }
    }
  }
}
