package com.cinestar.exception;

import org.springframework.http.HttpStatus;

/** 409 - dung cho xung dot dat ghe: ghe da nguoi khac giu/mua */
public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
