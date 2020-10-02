package com.cars24.biddingsystem.exception;

public class AuctionNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public AuctionNotFoundException(String msg) {
		super(msg);
	}
}