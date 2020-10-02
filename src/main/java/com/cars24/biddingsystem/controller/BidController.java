package com.cars24.biddingsystem.controller;


import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cars24.biddingsystem.exception.AuctionNotFoundException;
import com.cars24.biddingsystem.model.Auction;
import com.cars24.biddingsystem.model.Bid;
import com.cars24.biddingsystem.model.ErrorMessage;
import com.cars24.biddingsystem.model.User;
import com.cars24.biddingsystem.payload.request.BidRequest;
import com.cars24.biddingsystem.payload.request.LoginRequest;
import com.cars24.biddingsystem.payload.response.JwtResponse;
import com.cars24.biddingsystem.repository.AuctionRepository;
import com.cars24.biddingsystem.repository.BidRepository;
import com.cars24.biddingsystem.repository.UserRepository;
import com.cars24.biddingsystem.security.services.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auctions/{itemCode}")
public class BidController {
	@Autowired
	AuctionRepository auctionRepository;
	
	@Autowired
	BidRepository bidRepository;
	
	@Autowired
	UserRepository userRepository;
	
	@PostMapping("/bid")
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
	public ResponseEntity placeBid(@RequestBody BidRequest bidRequest, @PathVariable("itemCode") long itemCode) {
		
		/**
		 * Get user info from Security context
		 */
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getDetails();
		
		Auction auction = this.auctionRepository.findById(itemCode)
							.orElseThrow(() -> new AuctionNotFoundException("Item code does not exists."));
		
		System.out.println("User name:: " + user.getUsername());
		
		System.out.println("Auction id:: " + auction.getItemCode());
		
		
		updateBid(user, bidRequest);

//		Authentication authentication = authenticationManager.authenticate(
//				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
//
//		SecurityContextHolder.getContext().setAuthentication(authentication);
//		String jwt = jwtUtils.generateJwtToken(authentication);
//		
//		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();		
//		List<String> roles = userDetails.getAuthorities().stream()
//				.map(item -> item.getAuthority())
//				.collect(Collectors.toList());
//
//		return ResponseEntity.ok(new JwtResponse(jwt, 
////												 userDetails.getId(), 
////												 userDetails.getUsername(), 
////												 userDetails.getEmail(), 
//												 roles));
		
		return ResponseEntity.ok(new ErrorMessage(10, new Date(), "ok", ""));
	}
	
	public synchronized void updateBid(User user, BidRequest bidRequest) {
//	    this.bidRepository.
	}
}
