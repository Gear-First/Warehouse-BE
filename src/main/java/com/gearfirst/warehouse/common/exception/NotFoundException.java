package com.gearfirst.warehouse.common.exception;

import com.gearfirst.warehouse.common.response.ErrorStatus;
import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException {
    public NotFoundException() {
        super(HttpStatus.NOT_FOUND);
    }

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    public NotFoundException(ErrorStatus errorStatus) {
        super(errorStatus.getHttpStatus(), errorStatus.getMessage());
    }
}
