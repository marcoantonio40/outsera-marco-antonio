package com.outsera.awards.adapter.in.web.exception;

import com.outsera.awards.adapter.in.web.exception.UploadException;
import com.outsera.awards.adapter.in.web.response.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    @ExceptionHandler(UploadException.class)
    public ResponseEntity<ApiErrorResponse> handleUploadException(UploadException exception) {
        log.error("AEH-HUE-00 | Requisição rejeitada | status={} reason={}",
                exception.getStatus().value(), exception.getMessage());
        return error(exception.getStatus(), exception.getMessage());
    }

    private ResponseEntity<ApiErrorResponse> error(
            HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(status.value(), status.getReasonPhrase(), message));
    }
}
