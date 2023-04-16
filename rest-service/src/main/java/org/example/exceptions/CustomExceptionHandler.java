package org.example.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDto> badRequestExceptionHandler(Exception ex) {

        return new ResponseEntity(new ErrorDto(ex.getMessage(),HttpStatus.BAD_REQUEST), HttpStatus.BAD_REQUEST);
    }
}
