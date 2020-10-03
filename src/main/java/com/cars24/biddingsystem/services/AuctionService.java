package com.cars24.biddingsystem.services;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
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
import com.cars24.biddingsystem.controller.AuctionController;
import com.cars24.biddingsystem.exception.AuctionNotFoundException;
import com.cars24.biddingsystem.jpa.model.Auction;
import com.cars24.biddingsystem.jpa.model.Bid;
import com.cars24.biddingsystem.jpa.model.User;
import com.cars24.biddingsystem.payload.request.BidRequest;
import com.cars24.biddingsystem.repository.AuctionRepository;
import com.cars24.biddingsystem.repository.BidRepository;
import com.cars24.biddingsystem.rest.model.AuctionResponse;
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

		List<AuctionResponse> resources = new ArrayList<>();

		auctions.forEach(auction -> {
			resources.add(createAuctionResource(auction));
		});

		return new ResponseEntity<>(generateResponse(pageAuctions, resources), HttpStatus.OK);

	}

	public AuctionResponse createAuctionResource(Auction auction) {

		AuctionResponse resource = new AuctionResponse();
		resource.setItemName(auction.getItemName());
		resource.setItemCode(auction.getItemCode());
		resource.setStatus(auction.getStatus());
		resource.setBasePrice(auction.getBasePrice());
		resource.setStepRate(auction.getStepRate());

		Bid maxBid = auction.getBids().stream().filter(b -> b.getStatus() == BidStatus.ACCEPTED)
				.collect(Collectors.maxBy(Comparator.comparing(Bid::getBidPrice))).orElseGet(() -> new Bid());

		resource.setRecentBidPrice(maxBid.getBidPrice());
		Link newLink = linkTo(methodOn(AuctionController.class).getAuctionById(auction.getItemCode())).withSelfRel();
		resource.add(newLink);
		newLink = linkTo(methodOn(AuctionController.class).placeBid(null, auction.getItemCode())).withRel("bid");
		resource.add(newLink);

		return resource;
	}

	public AuctionResponse findById(long id) {
		Auction auction = this.auctionRepository.findById(id)
				.orElseThrow(() -> new AuctionNotFoundException("Auction not found"));

		return createAuctionResource(auction);
	}

	public Map<String, Object> generateResponse(Page<Auction> pageAuctions, List<AuctionResponse> auctions) {
		Map<String, Object> response = new LinkedHashMap<>();
		response.put("auctions", auctions);
		response.put("currentPage", pageAuctions.getNumber());
		response.put("totalItems", pageAuctions.getTotalElements());
		response.put("totalPages", pageAuctions.getTotalPages());
		List<Link> links = new ArrayList<>();
		Link link = linkTo(methodOn(AuctionController.class).getAuctions(AuctionStatus.RUNNING, 0, 10)).withSelfRel();
		links.add(link);
		response.put("links", links);
		return response;
	}

	/**
	 * Thread safe method to Update Bid Only Authorized users can access this
	 * method.
	 * 
	 * @param user
	 * @param auction
	 * @param bidRequest
	 * @return
	 */
	public synchronized strictfp ResponseEntity<BiddingMessage> updateBid(User user, Auction auction,
			BidRequest bidRequest) {

		BiddingMessage bm = new BiddingMessage();
		Link link = linkTo(methodOn(AuctionController.class).getAuctions(AuctionStatus.RUNNING, 0, 10))
				.withRel("auctions");
		bm.add(link);
		ResponseEntity<BiddingMessage> response;

		if (auction.getStatus() == AuctionStatus.OVER) {
			this.bidRepository
					.save(new Bid(auction, user, BidStatus.REJECTED, bidRequest.getBidAmount(), "auction closed"));
			bm.setMessage("Bidding Rejected");
			bm.setDescription("Auction is not RUNNING");
			return new ResponseEntity<>(bm, HttpStatus.NOT_ACCEPTABLE);
		}

		float basePrice = auction.getBasePrice();
		float stepRate = auction.getStepRate();

		Bid maxBid = auction.getBids().stream().filter(b -> b.getStatus() == BidStatus.ACCEPTED)
				.collect(Collectors.maxBy(Comparator.comparing(Bid::getBidPrice))).orElseGet(() -> new Bid());

		float minAllowedBid = 0.0f;
		float prevBidAmount = maxBid.getBidPrice();
		System.out.println("Min price:: " + maxBid.getBidPrice());
		if (prevBidAmount == 0.0f) {
			minAllowedBid = basePrice;
		} else {
			minAllowedBid = Float.sum(stepRate, prevBidAmount);
		}

		if (bidRequest.getBidAmount() > minAllowedBid) {
			this.bidRepository.save(new Bid(auction, user, BidStatus.ACCEPTED, bidRequest.getBidAmount()));
			bm.setMessage("Bidding Accepted");
			bm.setDescription("Bidding Success");
			response = new ResponseEntity<>(bm, HttpStatus.CREATED);

		} else {
			String reason = "bid amount is less";
			bm.setDescription("Because your bidding amount " + bidRequest.getBidAmount()
					+ " is less than min_bid_amount " + minAllowedBid);
			if (bidRequest.getBidAmount() == minAllowedBid) {
				reason = "bid amount is same as existing";
				bm.setDescription("Because your bidding amount " + bidRequest.getBidAmount()
						+ " is equals min_bid_amount " + minAllowedBid);
			}

			bm.setMessage("Bidding Rejected");
			this.bidRepository.save(new Bid(auction, user, BidStatus.REJECTED, bidRequest.getBidAmount(), reason));
			response = new ResponseEntity<>(bm, HttpStatus.NOT_ACCEPTABLE);
		}

		return response;
	}
}
