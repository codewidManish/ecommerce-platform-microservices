package com.ecommerce.userservice.exception;

import org.springframework.http.HttpStatus;

public class TokenException extends UserServiceException {
    public TokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "TOKEN_ERROR");
    }
}
