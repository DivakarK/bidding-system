package com.cars24.biddingsystem.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cars24.biddingsystem.constants.AuctionStatus;
import com.cars24.biddingsystem.jpa.model.Auction;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
	Page<Auction> findByStatus(AuctionStatus status, Pageable pageable);

	Page<Auction> findByItemName(String itemName, Pageable pageable);
}
