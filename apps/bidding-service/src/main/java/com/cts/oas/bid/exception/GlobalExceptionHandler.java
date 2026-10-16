package com.cts.oas.bid.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
@ExceptionHandler(AuctionNotFoundException.class)
public ResponseEntity<ErrorResponse> handleAuctionNotFound (AuctionNotFoundException ex){
	ErrorResponse errorResponse = new ErrorResponse(
		    404,
		    "Auction Not Found",
		    ex.getMessage(),
		    LocalDateTime.now()
		);
	return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
}

@ExceptionHandler(BidValidationException.class)
public ResponseEntity<ErrorResponse> handleBidValidation(BidValidationException ex){
	ErrorResponse errorResponse=new ErrorResponse(
			400,
			"Bid Validation Failed",
			ex.getMessage(),
			LocalDateTime.now()
			);
	return ResponseEntity.badRequest().body(errorResponse);
}
	
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex){
	ErrorResponse errorResponse=new ErrorResponse(
			500,
			"Internal server error",
			ex.getMessage(),
			LocalDateTime.now()
			);
	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
			
}
}
