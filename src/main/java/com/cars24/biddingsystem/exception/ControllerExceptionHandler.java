package com.cars24.biddingsystem.exception;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.cars24.biddingsystem.constants.AuctionStatus;
import com.cars24.biddingsystem.controller.AuctionController;
import com.cars24.biddingsystem.controller.AuthController;
import com.cars24.biddingsystem.rest.model.ErrorMessage;

@ControllerAdvice
public class ControllerExceptionHandler {

	@ExceptionHandler(AuctionNotFoundException.class)
	public ResponseEntity<ErrorMessage> resourceNotFoundException(AuctionNotFoundException ex, WebRequest request) {

		ErrorMessage message = new ErrorMessage(HttpStatus.NOT_FOUND.value(), new Date(), ex.getMessage(),
				request.getDescription(false));
		
		message.add(linkTo(methodOn(AuctionController.class).getAuctions(AuctionStatus.RUNNING, 0, 5)).withRel("available_auctions"));

		return new ResponseEntity<ErrorMessage>(message, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ErrorMessage> userNotFoundException(UserNotFoundException ex, WebRequest request) {

		ErrorMessage message = new ErrorMessage(HttpStatus.NOT_FOUND.value(), new Date(), ex.getMessage(),
				request.getDescription(false));
		
		message.add(linkTo(methodOn(AuthController.class).authenticateUser(null)).withRel("signin"));
		message.add(linkTo(methodOn(AuthController.class).registerUser(null)).withRel("signup"));
		
		return new ResponseEntity<ErrorMessage>(message, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorMessage> badCredentials(BadCredentialsException ex, WebRequest request) {
		ErrorMessage message = new ErrorMessage(HttpStatus.UNAUTHORIZED.value(), new Date(), ex.getMessage(),
				request.getDescription(true));

		message.add(linkTo(methodOn(AuthController.class).authenticateUser(null)).withSelfRel());
		message.add(linkTo(methodOn(AuthController.class).registerUser(null)).withRel("signup"));

		return new ResponseEntity<ErrorMessage>(message, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorMessage> accessDenied(AccessDeniedException ex, WebRequest request) {
		ErrorMessage message = new ErrorMessage(HttpStatus.FORBIDDEN.value(), new Date(), ex.getMessage(),
				request.getDescription(true));
		
		message.add(
				linkTo(methodOn(AuctionController.class).getAuctions(AuctionStatus.RUNNING, 0, 5)).withRel("auctions"));

		return new ResponseEntity<ErrorMessage>(message, HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorMessage> globalExceptionHandler(Exception ex, WebRequest request) {
		ErrorMessage message = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), new Date(), ex.getMessage(),
				request.getDescription(false));
		
		message.add(linkTo(methodOn(AuthController.class).authenticateUser(null)).withRel("signin"));
		message.add(linkTo(methodOn(AuthController.class).registerUser(null)).withRel("signup"));
		message.add(linkTo(methodOn(AuctionController.class).getAuctions(AuctionStatus.RUNNING, 0, 5)).withRel("auctions"));
		

		return new ResponseEntity<ErrorMessage>(message, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorMessage> handleError404(HttpServletRequest request, Exception ex)   {
		ErrorMessage message = new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(), new Date(), ex.getMessage(),
				null);
		
		message.add(linkTo(methodOn(AuthController.class).authenticateUser(null)).withRel("signin"));
		message.add(linkTo(methodOn(AuthController.class).registerUser(null)).withRel("signup"));
		message.add(linkTo(methodOn(AuctionController.class).getAuctions(AuctionStatus.RUNNING, 0, 5)).withRel("auctions"));
		

		return new ResponseEntity<ErrorMessage>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
