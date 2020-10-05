package com.cars24.biddingsystem.services;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.cars24.biddingsystem.constants.AuctionStatus;
import com.cars24.biddingsystem.constants.BidStatus;
import com.cars24.biddingsystem.controller.AuctionController;
import com.cars24.biddingsystem.exception.AuctionNotFoundException;
import com.cars24.biddingsystem.exception.UserNotFoundException;
import com.cars24.biddingsystem.jpa.model.Auction;
import com.cars24.biddingsystem.jpa.model.Bid;
import com.cars24.biddingsystem.jpa.model.User;
import com.cars24.biddingsystem.payload.request.BidRequest;
import com.cars24.biddingsystem.repository.AuctionRepository;
import com.cars24.biddingsystem.repository.BidRepository;
import com.cars24.biddingsystem.repository.UserRepository;
import com.cars24.biddingsystem.rest.model.AuctionResponse;
import com.cars24.biddingsystem.rest.model.BiddingMessage;

@Service
public class AuctionService {

	@Autowired
	private AuctionRepository auctionRepository;

	@Autowired
	private BidRepository bidRepository;

	@Autowired
	private UserRepository userRepository;

	private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);

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

	public AuctionResponse createAuction(Auction auction) {

		Auction _auction = auctionRepository.save(
				new Auction(auction.getItemName(), auction.getBasePrice(), auction.getStepRate(), auction.getStatus()));

		AuctionResponse response = new AuctionResponse();
		response.setItemName(_auction.getItemName());
		response.setBasePrice(_auction.getBasePrice());
		response.setStepRate(_auction.getStepRate());
		response.setStatus(_auction.getStatus());
		response.setItemCode(_auction.getItemCode());
		response.add(linkTo(methodOn(AuctionController.class).getAuctionById(response.getItemCode())).withSelfRel());
		response.add(linkTo(methodOn(AuctionController.class).updateAuction(response.getItemCode(), null))
				.withRel("update_auction"));
		response.add(linkTo(methodOn(AuctionController.class).placeBid(null, response.getItemCode())).withRel("bid"));

		return response;
	}

	public AuctionResponse updateAuctionById(long id, Auction auction) {

		Auction _auction = auctionRepository.findById(id)
				.orElseThrow(() -> new AuctionNotFoundException("Auction not found"));

		_auction.setStatus(auction.getStatus());

		_auction = auctionRepository.save(_auction);

		AuctionResponse response = new AuctionResponse();
		response.setItemName(_auction.getItemName());
		response.setBasePrice(_auction.getBasePrice());
		response.setStepRate(_auction.getStepRate());
		response.setStatus(_auction.getStatus());
		response.setItemCode(_auction.getItemCode());
		response.add(
				linkTo(methodOn(AuctionController.class).updateAuction(response.getItemCode(), null)).withSelfRel());
		response.add(linkTo(methodOn(AuctionController.class).placeBid(null, response.getItemCode())).withRel("bid"));

		return response;
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

	public BiddingMessage placeBid(long itemCode, BidRequest bidRequest) {

		/**
		 * Get user info from Security context
		 */
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		User user = this.userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new UserNotFoundException("User not found"));

		Auction auction = this.auctionRepository.findById(itemCode)
				.orElseThrow(() -> new AuctionNotFoundException("Item code does not exists."));

		return placeBidSecurely(user, auction, bidRequest);

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
	public synchronized strictfp BiddingMessage placeBidSecurely(User user, Auction auction, BidRequest bidRequest) {

		BiddingMessage bm = new BiddingMessage();
		Link link = linkTo(methodOn(AuctionController.class).getAuctions(AuctionStatus.RUNNING, 0, 10))
				.withRel("auctions");
		bm.add(link);
		
		logger.info("Status:: " + auction.getStatus());

		if (auction.getStatus() == AuctionStatus.OVER) {
			this.bidRepository
					.save(new Bid(auction, user, BidStatus.REJECTED, bidRequest.getBidAmount(), "auction closed"));
			bm.setMessage("Bid Rejected");
			bm.setDescription("selected Auction is not in RUNNING state");
			bm.setStatus(BidStatus.REJECTED);
			
			return bm;
		}

		float basePrice = auction.getBasePrice();
		float stepRate = auction.getStepRate();

		Bid maxBid = auction.getBids().stream().filter(b -> b.getStatus() == BidStatus.ACCEPTED)
				.collect(Collectors.maxBy(Comparator.comparing(Bid::getBidPrice))).orElseGet(() -> new Bid());

		float minAllowedBid = 0.0f;
		float prevBidAmount = maxBid.getBidPrice();
		
		logger.info("Min price:: " + maxBid.getBidPrice());
		
		if (prevBidAmount == 0.0f) {
			minAllowedBid = basePrice;
		} else {
			minAllowedBid = Float.sum(stepRate, prevBidAmount);
		}

		if (bidRequest.getBidAmount() > minAllowedBid) {
			this.bidRepository.save(new Bid(auction, user, BidStatus.ACCEPTED, bidRequest.getBidAmount()));
			bm.setMessage("Bidding Accepted");
			bm.setDescription("Bidding Success");
			bm.setStatus(BidStatus.ACCEPTED);

		} else {
			String reason = "bid amount is less";
			bm.setDescription("Because your bidding amount " + bidRequest.getBidAmount()
					+ " is less than min_bid_amount " + minAllowedBid);
			logger.info(bidRequest.getBidAmount() + "<------>" + minAllowedBid);
			if (bidRequest.getBidAmount() == minAllowedBid) {
				reason = "bid amount is same as existing";
				bm.setDescription("Because your bidding amount " + bidRequest.getBidAmount()
						+ " is equals min_bid_amount " + minAllowedBid);
			}

			bm.setMessage("Bid Rejected");
			this.bidRepository.save(new Bid(auction, user, BidStatus.REJECTED, bidRequest.getBidAmount(), reason));
			bm.setStatus(BidStatus.REJECTED);
		}

		return bm;
	}
}
