package com.cars24.biddingsystem.payload.response;

import org.springframework.hateoas.RepresentationModel;

public class MessageResponse extends RepresentationModel<MessageResponse> {
	private String message;

	public MessageResponse() {
		// TODO Auto-generated constructor stub
	}

	public MessageResponse(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}