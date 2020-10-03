package com.cars24.biddingsystem.rest.model;

import org.springframework.hateoas.RepresentationModel;

import com.cars24.biddingsystem.constants.AuctionStatus;


public class AuctionResource extends RepresentationModel<AuctionResource>{
	private long itemCode;
	private String itemName;
	private	float basePrice;
	private float stepRate;
	private AuctionStatus status;
	private float recentBidPrice;
	public long getItemCode() {
		return itemCode;
	}
	public void setItemCode(long itemCode) {
		this.itemCode = itemCode;
	}
	public String getItemName() {
		return itemName;
	}
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	public float getBasePrice() {
		return basePrice;
	}
	public void setBasePrice(float basePrice) {
		this.basePrice = basePrice;
	}
	public float getStepRate() {
		return stepRate;
	}
	public void setStepRate(float stepRate) {
		this.stepRate = stepRate;
	}
	public AuctionStatus getStatus() {
		return status;
	}
	public void setStatus(AuctionStatus status) {
		this.status = status;
	}
	public float getRecentBidPrice() {
		return recentBidPrice;
	}
	public void setRecentBidPrice(float recentBidPrice) {
		this.recentBidPrice = recentBidPrice;
	}	
}
