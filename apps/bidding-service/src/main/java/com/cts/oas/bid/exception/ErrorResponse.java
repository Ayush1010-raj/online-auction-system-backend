package com.cts.oas.bid.exception;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;



@Getter @AllArgsConstructor
public class ErrorResponse {

	int status;
	String error;
	String message;
	LocalDateTime timestamp;
}
