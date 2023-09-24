package com.soko.minifirfin.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionResponse> badRequestExceptionHandler(final BadRequestException badRequestException) {
        logger.warn(badRequestException.getMessage());
        return ResponseEntity.badRequest().body(new ExceptionResponse(badRequestException));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> unExpectedExceptionHandler(Exception exception) {
        if (Objects.isNull(exception.getMessage())) {
            logger.error("========== unexpected exception occurred ==========");
            logger.error(exception.getClass().toString());
            logger.error(convertStackTraceMessage(exception.getStackTrace()));
            logger.error("===================================================");
        } else {
            logger.error(exception.getMessage());
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExceptionResponse(500, "예상치 못한 에러"));
    }

    private String convertStackTraceMessage(StackTraceElement[] stackTrace) {
        return Arrays.stream(stackTrace)
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));
    }
}
