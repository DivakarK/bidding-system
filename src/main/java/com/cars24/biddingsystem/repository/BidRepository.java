package com.cars24.biddingsystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cars24.biddingsystem.model.Auction;
import com.cars24.biddingsystem.model.Bid;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long>{
	List<Bid> findByAuction(Auction auction);
}
