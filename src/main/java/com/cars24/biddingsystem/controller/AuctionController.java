package com.cars24.biddingsystem.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import com.cars24.biddingsystem.jpa.model.Auction;
import com.cars24.biddingsystem.payload.request.BidRequest;
import com.cars24.biddingsystem.rest.model.AuctionResponse;
import com.cars24.biddingsystem.rest.model.BiddingMessage;
import com.cars24.biddingsystem.services.AuctionService;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auctions")
public class AuctionController {

	@Autowired
	AuctionService auctionService;

	private static final Logger logger = LoggerFactory.getLogger(AuctionController.class);

	@GetMapping
	public ResponseEntity<Map<String, Object>> getAuctions(@RequestParam(required = false) AuctionStatus status,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size) {

		logger.info("Page:: "+ page);
		logger.info("Size:: "+ size);
		logger.info("Status:: "+ status);

		Pageable paging = PageRequest.of(page, size);
		return this.auctionService.find(status, paging);
	}

	@GetMapping("/{id}")
	public ResponseEntity<AuctionResponse> getAuctionById(@PathVariable("id") long id) {

		logger.info("Id:: "+ id);

		AuctionResponse auctionData = this.auctionService.findById(id);
		return new ResponseEntity<>(auctionData, HttpStatus.OK);
	}

	@PostMapping
	@PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<AuctionResponse> createAuction(@RequestBody Auction auction) {

		logger.info("Create Auction :: "+ auction);

		AuctionResponse response = this.auctionService.createAuction(auction);

		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<AuctionResponse> updateAuction(@PathVariable("id") long id, @RequestBody Auction auction) {

		logger.info("Id:: "+ id);
		logger.info("Auction:: "+ auction);

		AuctionResponse response = this.auctionService.updateAuctionById(id, auction);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/{itemCode}/bid")
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<BiddingMessage> placeBid(@RequestBody BidRequest bidRequest,
			@PathVariable("itemCode") long itemCode) {

		logger.info("Bid Request:: " + bidRequest);
		logger.info("itemCode:: " + itemCode);

		BiddingMessage message = this.auctionService.placeBid(itemCode, bidRequest);

		if (message.getStatus() == BidStatus.ACCEPTED) {
			return new ResponseEntity<>(message, HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>(message, HttpStatus.NOT_ACCEPTABLE);
		}
	}
}
