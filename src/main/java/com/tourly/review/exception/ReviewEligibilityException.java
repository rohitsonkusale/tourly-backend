package com.tourly.review.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ReviewEligibilityException extends RuntimeException {

    public ReviewEligibilityException(String message) {
        super(message);
    }
}
