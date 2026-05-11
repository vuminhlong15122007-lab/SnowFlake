package com.javfxtutorial.hethongdaugia.server.dao;

import com.javfxtutorial.hethongdaugia.common.model.Auction;

import java.util.List;

public class ParticipatedAuctionDAO implements DAOInterface<Auction>{
  @Override
  public int insert(Auction auction) {
    return 0;
  }

  @Override
  public int update(Auction auction) {
    return 0;
  }

  @Override
  public int delete(Auction auction) {
    return 0;
  }

  @Override
  public List<Auction> selectAll() {
    return List.of();
  }

  @Override
  public Auction selectById(int id) {
    return null;
  }
}
