package com.cars24.biddingsystem.payload.request;

import javax.validation.constraints.NotBlank;

public class BidRequest {
	@NotBlank
	private float bidAmount;

	public float getBidAmount() {
		return bidAmount;
	}

	public void setBidAmount(float bidAmount) {
		this.bidAmount = bidAmount;
	}
}
