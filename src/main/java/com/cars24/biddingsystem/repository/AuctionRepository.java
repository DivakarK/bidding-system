package com.cars24.biddingsystem.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cars24.biddingsystem.constants.AuctionStatus;
import com.cars24.biddingsystem.model.Auction;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
	List<Auction> findByStatus(AuctionStatus status);
	List<Auction> findByItemName(String itemName);
}
