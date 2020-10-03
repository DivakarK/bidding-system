package com.cars24.biddingsystem.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import com.cars24.biddingsystem.exception.AuctionNotFoundException;
import com.cars24.biddingsystem.exception.UserNotFoundException;
import com.cars24.biddingsystem.jpa.model.Auction;
import com.cars24.biddingsystem.jpa.model.User;
import com.cars24.biddingsystem.payload.request.BidRequest;
import com.cars24.biddingsystem.repository.AuctionRepository;
import com.cars24.biddingsystem.repository.UserRepository;
import com.cars24.biddingsystem.rest.model.AuctionResource;
import com.cars24.biddingsystem.rest.model.BiddingMessage;
import com.cars24.biddingsystem.services.AuctionService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

	@Autowired
	AuctionRepository auctionRepository;

	@Autowired
	UserRepository userRepository;

	@Autowired
	AuctionService auctionService;

	@GetMapping("")
	public ResponseEntity<Map<String, Object>> getAuctions(@RequestParam(required = false) AuctionStatus status,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {

		Pageable paging = PageRequest.of(page, size);
		return this.auctionService.find(status, paging);
	}

	@GetMapping("/{id}")
	public ResponseEntity<AuctionResource> getAuctionById(@PathVariable("id") long id) {
		AuctionResource auctionData = this.auctionService.findById(id);
		return new ResponseEntity<>(auctionData, HttpStatus.OK);
	}

	@PostMapping("")
	@PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<Auction> createAuction(@RequestBody Auction auction) {

		Auction _auction = auctionRepository.save(
				new Auction(auction.getItemName(), auction.getBasePrice(), auction.getStepRate(), auction.getStatus()));
		return new ResponseEntity<>(_auction, HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<Auction> updateAuction(@PathVariable("id") long id, @RequestBody Auction auction) {
		Auction _auction = auctionRepository.findById(id)
				.orElseThrow(() -> new AuctionNotFoundException("Auction not found"));
		_auction.setItemName(auction.getItemName());
		_auction.setBasePrice(auction.getBasePrice());
		_auction.setStepRate(auction.getStepRate());
		_auction.setStatus(auction.getStatus());

		return new ResponseEntity<>(auctionRepository.save(_auction), HttpStatus.OK);

	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<HttpStatus> deleteTutorial(@PathVariable("id") long id) {
		auctionRepository.deleteById(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@PostMapping("/{itemCode}/bid")
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<BiddingMessage> placeBid(@RequestBody BidRequest bidRequest,
			@PathVariable("itemCode") long itemCode) {

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

		ResponseEntity<BiddingMessage> response = this.auctionService.updateBid(user, auction, bidRequest);

		return response;
	}
}
