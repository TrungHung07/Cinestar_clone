package com.cinestar.exception;

import org.springframework.http.HttpStatus;

/** Exception co san HTTP status, cac exception nghiep vu ke thua tu day */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
