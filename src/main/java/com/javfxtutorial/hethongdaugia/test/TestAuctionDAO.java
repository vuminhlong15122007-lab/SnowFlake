package com.javfxtutorial.hethongdaugia.test;

import com.javfxtutorial.hethongdaugia.common.model.Auction;
import com.javfxtutorial.hethongdaugia.server.dao.AuctionDAO;

import java.util.ArrayList;

public class TestAuctionDAO {
    static void main() {
        ArrayList<Auction> auctions = AuctionDAO.getInstance().selectAll();
        System.out.println(auctions);
    }
}
