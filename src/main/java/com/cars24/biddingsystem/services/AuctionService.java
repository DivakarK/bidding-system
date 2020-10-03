package com.cars24.biddingsystem.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cars24.biddingsystem.constants.AuctionStatus;
import com.cars24.biddingsystem.constants.BidStatus;
import com.cars24.biddingsystem.exception.AuctionNotFoundException;
import com.cars24.biddingsystem.jpa.model.Auction;
import com.cars24.biddingsystem.jpa.model.Bid;
import com.cars24.biddingsystem.jpa.model.User;
import com.cars24.biddingsystem.payload.request.BidRequest;
import com.cars24.biddingsystem.repository.AuctionRepository;
import com.cars24.biddingsystem.repository.BidRepository;
import com.cars24.biddingsystem.rest.model.AuctionResource;
import com.cars24.biddingsystem.rest.model.BiddingMessage;


@Service
public class AuctionService {
	
	@Autowired
	private AuctionRepository auctionRepository;
	
	@Autowired
	private BidRepository bidRepository;
	
	public ResponseEntity<Map<String, Object>> find(AuctionStatus status, Pageable paging) {
		
		List<Auction> auctions = new ArrayList<Auction>();
		
		Page<Auction> pageAuctions;

		if (status == null)
			pageAuctions = auctionRepository.findAll(paging);
		else
			pageAuctions = auctionRepository.findByStatus(status, paging);
		
		auctions = pageAuctions.getContent();

		if (auctions.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		
		List<AuctionResource> resources = new ArrayList<>();
		
		auctions.forEach(auction -> {
			resources.add(createAuctionResource(auction));
		});
		
		return new ResponseEntity<>(generateResponse(pageAuctions, resources), HttpStatus.OK);

	}
	
	@SuppressWarnings("deprecation")
	public AuctionResource createAuctionResource(Auction auction) {
		
		AuctionResource resource = new AuctionResource();
		resource.setItemName(auction.getItemName());
		resource.setItemCode(auction.getItemCode());
		resource.setStatus(auction.getStatus());
		resource.setBasePrice(auction.getBasePrice());
		resource.setStepRate(auction.getStepRate());

		Bid maxBid = auction.getBids().stream()
				.filter(b -> b.getStatus() == BidStatus.ACCEPTED)
				.collect(Collectors.maxBy(Comparator.comparing(Bid::getBidPrice)))
				.orElseGet(() -> new Bid());
		
		resource.setRecentBidPrice(maxBid.getBidPrice());
		Link link = new Link("http://localhost:8080/api/auctions/" + auction.getItemCode(), "self");
		resource.add(link);
		link = new Link("http://localhost:8080/api/auctions/" + auction.getItemCode() + "/bid", "bid");
		resource.add(link);
		
		return resource;
	}
	
	public AuctionResource findById(long id) {
		Auction auction = this.auctionRepository.findById(id)
				.orElseThrow(() -> new AuctionNotFoundException("Auction not found"));
		
		return createAuctionResource(auction);
	}
	
	public Map<String, Object> generateResponse(Page<Auction> pageAuctions, List<AuctionResource> auctions) {
		Map<String, Object> response = new HashMap<>();
		response.put("auctions", auctions);
		response.put("currentPage", pageAuctions.getNumber());
		response.put("totalItems", pageAuctions.getTotalElements());
		response.put("totalPages", pageAuctions.getTotalPages());
		
		return response;
	}
	
	/**
	 * Thread safe method to Update Bid
	 * Only Authorized users can access this method.
	 * @param user
	 * @param auction
	 * @param bidRequest
	 * @return
	 */
	public synchronized strictfp ResponseEntity<BiddingMessage> updateBid(User user, Auction auction, BidRequest bidRequest) {

		float basePrice = auction.getBasePrice();
		float stepRate = auction.getStepRate();
		
		float initialBidPrice = Float.sum(basePrice, stepRate);
		
		Bid maxBid = auction.getBids().stream()
						.filter(b -> b.getStatus() == BidStatus.ACCEPTED)
						.collect(Collectors.maxBy(Comparator.comparing(Bid::getBidPrice)))
						.orElseGet(() -> new Bid());
		
		float minAllowedBid = 0.0f;
		float maxBidAmount = maxBid.getBidPrice();
		System.out.println("Min price:: " + maxBid.getBidPrice());
		if(maxBidAmount == 0.0f) {
			minAllowedBid = initialBidPrice;
		} else {
			minAllowedBid = Float.sum(stepRate, maxBidAmount);
		}
		
		BiddingMessage bm = new BiddingMessage();
		ResponseEntity<BiddingMessage> response;

		if (bidRequest.getBidAmount() > minAllowedBid) {
			this.bidRepository.save(new Bid(auction, user, BidStatus.ACCEPTED, bidRequest.getBidAmount()));
			bm.setMessage("Bidding Accepted");
			response = new ResponseEntity<>(bm, HttpStatus.CREATED);
		} else {
			this.bidRepository.save(new Bid(auction, user, BidStatus.REJECTED, bidRequest.getBidAmount()));
			bm.setMessage("Bidding Rejected");
			bm.setDescription("Because your bidding amount " + bidRequest.getBidAmount()
					+ " is less than or equal to min_bid_amount " + minAllowedBid);
			response = new ResponseEntity<>(bm, HttpStatus.NOT_ACCEPTABLE);
		}

		return response;
	}
}
