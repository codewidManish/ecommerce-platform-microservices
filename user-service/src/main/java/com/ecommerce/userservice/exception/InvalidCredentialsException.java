package com.ecommerce.userservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends UserServiceException {
    public InvalidCredentialsException() {
        super("Invalid email/username or password", HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS");
    }
}
