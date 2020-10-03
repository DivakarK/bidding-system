package com.cars24.biddingsystem.jpa.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.cars24.biddingsystem.constants.BidStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "BIDS")
public class Bid {
	@Id
	@GeneratedValue
	private long id;
	
	@JsonManagedReference
	@ManyToOne
	@JoinColumn(name="auction_id")
	private Auction auction;
	
	@OneToOne
	@JoinColumn(name="user_id")
	private User user;
	
	@Enumerated(EnumType.STRING)
	private BidStatus status;
	
	private float bidPrice;
	
	private Date createdDate;
	
	public Bid() {
		// TODO Auto-generated constructor stub
	}
	
	public Bid(Auction auction, User user, BidStatus status, float bidPrice) {
		this.auction = auction;
		this.user = user;
		this.status = status;
		this.bidPrice = bidPrice;
		this.createdDate = new Date();
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public Auction getAuction() {
		return auction;
	}
	public void setAuction(Auction auction) {
		this.auction = auction;
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public BidStatus getStatus() {
		return status;
	}
	public void setStatus(BidStatus status) {
		this.status = status;
	}
	public float getBidPrice() {
		return bidPrice;
	}
	public void setBidPrice(float bidPrice) {
		this.bidPrice = bidPrice;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
}
