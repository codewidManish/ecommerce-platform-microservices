package com.ecommerce.paymentservice.exception;

public class PaymentAlreadyProcessedException extends RuntimeException {
    public PaymentAlreadyProcessedException(String m) { super(m); }
}
