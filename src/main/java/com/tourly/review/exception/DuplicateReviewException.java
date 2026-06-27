package com.tourly.review.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateReviewException extends RuntimeException {

    public DuplicateReviewException(String message) {
        super(message);
    }
}
