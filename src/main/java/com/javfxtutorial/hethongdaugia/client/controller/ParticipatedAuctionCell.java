package com.javfxtutorial.hethongdaugia.client.controller;

import com.javfxtutorial.hethongdaugia.common.model.domain.Auction;
import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParticipatedAuctionCell extends ListCell<Auction> {
    private static final Logger log = LoggerFactory.getLogger(ParticipatedAuctionCell.class);

    // Cache controller theo auctionId → tránh tạo lại + reset countdown mỗi lần re-render
    private final Map<Integer, CachedCell> cellCache = new HashMap<>();

    private static class CachedCell {
        Parent root;
        ParticipatedAuctionCellController controller;
    }

    @Override
    protected void updateItem(Auction auction, boolean empty) {
        super.updateItem(auction, empty);

        if (empty || auction == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        int id = auction.getAuctionId();

        // Nếu đã có trong cache → dùng lại, không tạo mới (countdown không bị reset)
        if (cellCache.containsKey(id)) {
            setGraphic(cellCache.get(id).root);
            setText(null);
            return;
        }

        try {
            FXMLLoader loader =
                    new FXMLLoader(
                            getClass()
                                    .getResource(
                                            "/com/javfxtutorial/hethongdaugia/view/fxml/ParticipatedAuctionCell.fxml"));
            Parent root = loader.load();
            ParticipatedAuctionCellController controller = loader.getController();
            controller.setData(auction);

            CachedCell cached = new CachedCell();
            cached.root = root;
            cached.controller = controller;
            cellCache.put(id, cached);

            setText(null);
            setGraphic(root);
        } catch (Exception e) {
            log.error("LỖI LOAD CELL: {}", e.getMessage(), e);
            setGraphic(null);
            setText(auction.getItem() != null ? auction.getItem().getName() : "Lỗi");
        }
    }
}
