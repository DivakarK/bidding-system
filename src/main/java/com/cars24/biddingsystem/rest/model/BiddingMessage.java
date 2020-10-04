package com.cars24.biddingsystem.rest.model;

import org.springframework.hateoas.RepresentationModel;

import com.cars24.biddingsystem.constants.BidStatus;

public class BiddingMessage extends RepresentationModel<BiddingMessage> {
	private String message;

	private String description;
	
	private BidStatus status;
	
	public BidStatus getStatus() {
		return status;
	}

	public void setStatus(BidStatus status) {
		this.status = status;
	}

	public BiddingMessage() {
		// TODO Auto-generated constructor stub
	}

	public BiddingMessage(String meessage, String description) {
		this.message = meessage;
		this.description = description;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
