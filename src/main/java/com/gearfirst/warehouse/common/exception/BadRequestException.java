package com.gearfirst.warehouse.common.exception;

import com.gearfirst.warehouse.common.response.ErrorStatus;
import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {
    public BadRequestException() {
        super(HttpStatus.BAD_REQUEST);
    }

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public BadRequestException(ErrorStatus errorStatus) {
        super(errorStatus.getHttpStatus(), errorStatus.getMessage());
    }
}
