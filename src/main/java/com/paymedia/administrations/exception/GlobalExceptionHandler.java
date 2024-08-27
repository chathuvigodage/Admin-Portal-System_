package com.paymedia.administrations.exception;

import com.paymedia.administrations.model.response.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserLockedException.class)
    public ResponseEntity<CommonResponse<Void>> handleUserAlreadyLockedException(UserLockedException ex) {
        CommonResponse<Void> response = new CommonResponse<>(false, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserStatusException.class)
    public ResponseEntity<CommonResponse<Void>> handleUserStatusException(UserStatusException ex) {
        CommonResponse<Void> response = new CommonResponse<>(false, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }
}