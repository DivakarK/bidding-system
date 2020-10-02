package com.cars24.biddingsystem.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cars24.biddingsystem.constants.AuctionStatus;
import com.cars24.biddingsystem.constants.BidStatus;
import com.cars24.biddingsystem.exception.AuctionNotFoundException;
import com.cars24.biddingsystem.exception.UserNotFoundException;
import com.cars24.biddingsystem.model.Auction;
import com.cars24.biddingsystem.model.Bid;
import com.cars24.biddingsystem.model.ErrorMessage;
import com.cars24.biddingsystem.model.BiddingMessage;
import com.cars24.biddingsystem.model.User;
import com.cars24.biddingsystem.payload.request.BidRequest;
import com.cars24.biddingsystem.repository.AuctionRepository;
import com.cars24.biddingsystem.repository.BidRepository;
import com.cars24.biddingsystem.repository.UserRepository;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class AuctionController {

	@Autowired
	AuctionRepository auctionRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	BidRepository bidRepository;

	@GetMapping("/auctions")
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<List<Auction>> getAuctions(@RequestParam(required = false) AuctionStatus status) {
		try {
			List<Auction> auctions = new ArrayList<Auction>();
			System.out.println("Divakar: " + status);
			if (status == null)
				auctionRepository.findAll().forEach(auctions::add);
			else
				auctionRepository.findByStatus(status).forEach(auctions::add);

			if (auctions.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NO_CONTENT);
			}

			return new ResponseEntity<>(auctions, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/auctions/{id}")
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<Auction> getAuctionById(@PathVariable("id") long id) {
		Optional<Auction> auctionData = auctionRepository.findById(id);

		if (auctionData.isPresent()) {
			return new ResponseEntity<>(auctionData.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping("/auctions")
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<Auction> createAuction(@RequestBody Auction auction) {
		try {
			Auction _auction = auctionRepository.save(new Auction(auction.getItemName(), auction.getBasePrice(),
					auction.getStepRate(), auction.getStatus()));
			return new ResponseEntity<>(_auction, HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/auctions/{id}")
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<Auction> updateAuction(@PathVariable("id") long id, @RequestBody Auction auction) {
		Optional<Auction> auctionData = auctionRepository.findById(id);

		if (auctionData.isPresent()) {
			Auction _auction = auctionData.get();
			_auction.setItemName(auction.getItemName());
			_auction.setBasePrice(auction.getBasePrice());
			_auction.setStepRate(auction.getStepRate());
			_auction.setStatus(auction.getStatus());

			return new ResponseEntity<>(auctionRepository.save(_auction), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@DeleteMapping("/auctions/{id}")
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<HttpStatus> deleteTutorial(@PathVariable("id") long id) {
		try {
			auctionRepository.deleteById(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping("/auctions/{itemCode}/bid")
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<BiddingMessage> placeBid(@RequestBody BidRequest bidRequest, @PathVariable("itemCode") long itemCode) {
		
		/**
		 * Get user info from Security context
		 */
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		User user = this.userRepository.findByUsername(authentication.getName())
				.orElseThrow(() -> new UserNotFoundException("User not found"));
				
		Auction auction = this.auctionRepository.findById(itemCode)
							.orElseThrow(() -> new AuctionNotFoundException("Item code does not exists."));
		
		System.out.println("User name:: " + authentication.getName());
		
		System.out.println("Auction id:: " + auction.getItemCode());
		
		ResponseEntity<BiddingMessage> response = updateBid(user, auction, bidRequest);
		
		return response;
	}
	
	public synchronized ResponseEntity<BiddingMessage> updateBid(User user, Auction auction, BidRequest bidRequest) {
		
		Bid maxBid = auction.getBids().stream()
						.filter(b -> b.getStatus() == BidStatus.ACCEPTED)
						.collect(Collectors.maxBy(Comparator.comparing(Bid::getBidPrice)))
						.orElseGet(() -> new Bid());
		
		float minBidAllowed = Float.sum(auction.getStepRate(), maxBid.getBidPrice());
		
		BiddingMessage bm = new BiddingMessage();
		ResponseEntity<BiddingMessage> response;
		
		if(bidRequest.getBidAmount() > minBidAllowed) {
			this.bidRepository.save(new Bid(auction, user, BidStatus.ACCEPTED, bidRequest.getBidAmount()));
			bm.setMessage("Bidding Accepted");
			response = new ResponseEntity<>(bm, HttpStatus.CREATED);
		} else {
			this.bidRepository.save(new Bid(auction, user, BidStatus.REJECTED, bidRequest.getBidAmount()));
			bm.setMessage("Bidding Rejected");
			bm.setDescription("Rejected because your bidding amount " + bidRequest.getBidAmount() + " is less than min bid amount " + minBidAllowed);
			response = new ResponseEntity<>(bm, HttpStatus.NOT_ACCEPTABLE);
		}
		
		return response;
	}
}
