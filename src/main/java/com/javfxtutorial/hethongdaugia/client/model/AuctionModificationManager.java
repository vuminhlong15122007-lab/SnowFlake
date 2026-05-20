package com.javfxtutorial.hethongdaugia.client.model;

import com.javfxtutorial.hethongdaugia.client.network.NetworkManager;
import com.javfxtutorial.hethongdaugia.client.network.ResponseListener;
import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.common.model.Command.AddAuctionCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.DeleteAuctionCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateAuctionCommand;
import com.javfxtutorial.hethongdaugia.common.model.Command.UpdateAuctionStatusCommand;
import com.javfxtutorial.hethongdaugia.common.network.Response;
import com.javfxtutorial.hethongdaugia.server.network.ClientHandler;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

public class AuctionModificationManager implements ResponseListener {
  private static AuctionModificationManager instance;

  private AuctionModificationManager() {
  }

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
  }

  @Override
  public void onResponse(Response rp) {
    // luu sp
    if (rp.getCommand().getClass() == AddAuctionCommand.class) {
      if (rp.isSuccess()) {
        Auction savedAuction = (Auction) rp.getPayLoad();
        Platform.runLater(() -> {
          ClientModel.getInstance().getAllAuctions().add(savedAuction);
        });
      }
    }

    //delete
    if (rp.getCommand().getClass() == DeleteAuctionCommand.class) {
      if (rp.isSuccess()) {
        DeleteAuctionCommand command = (DeleteAuctionCommand) rp.getCommand();
        Auction selectedAuction = (rp.getPayLoad() instanceof Auction)
            ? (Auction) rp.getPayLoad()
            : command.getAuction();
        Platform.runLater(() -> {
          ClientModel.getInstance().getAllAuctions().removeIf(auction -> auction.getAuctionId() == selectedAuction.getAuctionId());
        });
      }
    }

    //suaSp
    if (rp.getCommand().getClass() == UpdateAuctionCommand.class || rp.getCommand().getClass().equals(UpdateAuctionStatusCommand.class)) {
      if (rp.isSuccess()) {
        Auction selectedAuction = (Auction) rp.getPayLoad();
        int foundIndex = -1; // 1. Khai báo 1 biến tạm để lưu vị trí nếu tìm thấy
        // Duyệt list để tìm
        for (int i = 0; i < ClientModel.getInstance().getAllAuctions().size(); i++) {
          if (selectedAuction.getAuctionId() == ClientModel.getInstance().getAllAuctions().get(i).getAuctionId()) {
            foundIndex = i; // 2. Tìm thấy rồi thì lưu vị trí i lại
            break;          // Thoát vòng lặp cho nhẹ máy
          }
        }
        // 3. Kiểm tra xem có thực sự tìm thấy không (khác -1)
        if (foundIndex != -1) {
          int finalIndex = foundIndex;
          Platform.runLater(() -> {
            ClientModel.getInstance().getAllAuctions().set(finalIndex, selectedAuction);
          });
        }
      }
    }
  }
}

