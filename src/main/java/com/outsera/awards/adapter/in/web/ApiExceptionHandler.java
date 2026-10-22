package com.outsera.awards.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UploadException.class)
    public ResponseEntity<ApiErrorResponse> handleUploadException(UploadException exception) {
        return error(exception.getStatus(), exception.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleFileTooLarge() {
        return error(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "O arquivo excede o limite máximo de 10 MB.");
    }

    private ResponseEntity<ApiErrorResponse> error(
            HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(status.value(), status.getReasonPhrase(), message));
    }
}
