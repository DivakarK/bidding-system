package com.cars24.biddingsystem.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.cars24.biddingsystem.constants.AuctionStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;


@Entity
@Table(name = "AUCTIONS")
public class Auction {
	@Id
	@GeneratedValue
	private long itemCode;
	
	private String itemName;
	
	private	float basePrice;
	
	private float stepRate;
	
	@Enumerated(EnumType.STRING)
	private AuctionStatus status;
	
	public Auction() {
		// TODO Auto-generated constructor stub
	}
	
	public Auction(String itemName, float basePrice, float stepRate, AuctionStatus status) {
		this.itemName = itemName;
		this.basePrice = basePrice;
		this.stepRate = stepRate;
		this.status = status;
	}
	
	@JsonBackReference
	@OneToMany(mappedBy="auction")
	private Collection<Bid> bids = new ArrayList<>();
	
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
	public Collection<Bid> getBids() {
		return bids;
	}
	public void setBids(Collection<Bid> bids) {
		this.bids = bids;
	}
}
