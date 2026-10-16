package com.cts.oas.transaction.exception;

public class TransactionNotFoundException extends RuntimeException{
	public TransactionNotFoundException(String message) {
        super(message);
    }
}
