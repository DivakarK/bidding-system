package com.cars24.biddingsystem.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.cars24.biddingsystem.model.Auction;
import com.cars24.biddingsystem.repository.AuctionRepository;

@RestController
@RequestMapping("/auctions")
public class AuctionController {

	@Autowired
	AuctionRepository auctionRepository;

	@GetMapping("/")
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<List<Auction>> getAuctions(@RequestParam(required = false) AuctionStatus status) {
		try {
			List<Auction> auctions = new ArrayList<Auction>();
			System.out.println("DIvakar: " + status);
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

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<Auction> getAuctionById(@PathVariable("id") long id) {
		Optional<Auction> auctionData = auctionRepository.findById(id);

		if (auctionData.isPresent()) {
			return new ResponseEntity<>(auctionData.get(), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	@PostMapping("/")
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

	@PutMapping("/{id}")
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

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity<HttpStatus> deleteTutorial(@PathVariable("id") long id) {
		try {
			auctionRepository.deleteById(id);
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
