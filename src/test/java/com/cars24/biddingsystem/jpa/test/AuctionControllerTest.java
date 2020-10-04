package com.cars24.biddingsystem.jpa.test;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.cars24.biddingsystem.constants.AuctionStatus;
import com.cars24.biddingsystem.constants.BidStatus;
import com.cars24.biddingsystem.controller.AuctionController;
import com.cars24.biddingsystem.jpa.model.Auction;
import com.cars24.biddingsystem.payload.request.BidRequest;
import com.cars24.biddingsystem.repository.AuctionRepository;
import com.cars24.biddingsystem.repository.UserRepository;
import com.cars24.biddingsystem.rest.model.AuctionResponse;
import com.cars24.biddingsystem.rest.model.BiddingMessage;
import com.cars24.biddingsystem.security.jwt.AuthEntryPointJwt;
import com.cars24.biddingsystem.security.jwt.JwtUtils;
import com.cars24.biddingsystem.security.services.UserDetailsServiceImpl;
import com.cars24.biddingsystem.services.AuctionService;

@RunWith(SpringRunner.class)
@WebMvcTest(value = AuctionController.class)
@WithMockUser
public class AuctionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private AuctionService auctionService;

	@MockBean
	UserRepository userRepository;

	@MockBean
	UserDetailsServiceImpl userDetailsServiceImpl;

	@MockBean
	AuthEntryPointJwt authEntryPointJwt;

	@MockBean
	AuctionRepository auctionRepository;

	@MockBean
	JwtUtils JwtUtils;

	@Test
	public void getAuctions() throws Exception {

		Map<String, Object> response = new LinkedHashMap<>();

		Mockito.when(auctionService.find(Mockito.any(AuctionStatus.class), Mockito.any(Pageable.class)))
				.thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

		RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/auctions").accept(MediaType.APPLICATION_JSON);

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		assertEquals("Status codes are not equal", 200, result.getResponse().getStatus());
	}

	@Test
	public void auctionBidInvalid() throws Exception {

		String bidReq = "{\"bidAmount\":\"100\"}";

		BiddingMessage bm = new BiddingMessage();
		bm.setMessage("Bid Rejected");
		bm.setDescription("selected Auction is not in RUNNING state");
		bm.setStatus(BidStatus.REJECTED);

		Mockito.when(auctionService.placeBid(Mockito.anyLong(), Mockito.any(BidRequest.class))).thenReturn(bm);

		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auctions/6/bid")
				.accept(MediaType.APPLICATION_JSON).content(bidReq).contentType(MediaType.APPLICATION_JSON);

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		assertEquals("Status codes are not equal", 406, result.getResponse().getStatus());
	}

	@Test
	public void auctionBidValid() throws Exception {

		String bidReq = "{\"bidAmount\":\"10000\"}";

		BiddingMessage bm = new BiddingMessage();
		bm.setMessage("Bidding Accepted");
		bm.setDescription("Bidding Success");
		bm.setStatus(BidStatus.ACCEPTED);

		Mockito.when(auctionService.placeBid(Mockito.anyLong(), Mockito.any(BidRequest.class))).thenReturn(bm);

		RequestBuilder requestBuilder = MockMvcRequestBuilders.post("/api/auctions/8/bid")
				.accept(MediaType.APPLICATION_JSON).content(bidReq).contentType(MediaType.APPLICATION_JSON);

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		assertEquals("Status codes are not equal", 201, result.getResponse().getStatus());
	}
	
	@Test
	public void updateAuction() throws Exception {

		String auction = "{\"status\":\"RUNNING\"}";

		AuctionResponse response = new AuctionResponse();
		response.setItemName("Chair");
		response.setBasePrice(10000);
		response.setStepRate(10);
		response.setStatus(AuctionStatus.RUNNING);
		response.setItemCode(2);

		Mockito.when(auctionService.updateAuctionById(Mockito.anyLong(), Mockito.any(Auction.class))).thenReturn(response);

		RequestBuilder requestBuilder = MockMvcRequestBuilders.put("/api/auctions/8")
				.accept(MediaType.APPLICATION_JSON).content(auction).contentType(MediaType.APPLICATION_JSON);

		MvcResult result = mockMvc.perform(requestBuilder).andReturn();

		assertEquals("Status codes are not equal", 403, result.getResponse().getStatus());
	}

}