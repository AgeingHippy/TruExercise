package org.charles.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@ResponseBody
@Slf4j
public class SearchControllerAdvice {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(value = HttpStatus.EXPECTATION_FAILED)
    public ResponseErrorMessage illegalArgumentExceptionResponse(IllegalArgumentException ex) {
        log.warn(ex.getMessage());
        ResponseErrorMessage responseErrorMessage = new ResponseErrorMessage(HttpStatus.EXPECTATION_FAILED, ex.getMessage());
        return responseErrorMessage;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ResponseErrorMessage HttpMessageNotReadableExceptionResponse(Exception ex) {
        log.warn(ex.getMessage());
        ResponseErrorMessage responseErrorMessage = new ResponseErrorMessage(HttpStatus.BAD_REQUEST, "Invalid request payload");
        return responseErrorMessage;
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    public ResponseErrorMessage MissingRequestHeaderExceptionResponse(Exception ex) {
        log.warn(ex.getMessage());
        ResponseErrorMessage responseErrorMessage = new ResponseErrorMessage(HttpStatus.FORBIDDEN, "API Key not provided");
        return responseErrorMessage;
    }

    @ExceptionHandler(InternalServerException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseErrorMessage InternalServerExceptionResponse(Exception ex) {
        log.error(ex.getMessage(), ex);
        ResponseErrorMessage responseErrorMessage = new ResponseErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, "Sorry, an internal error has occurred.");
        return responseErrorMessage;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseErrorMessage exceptionResponse(Exception ex) {
        log.error(ex.getMessage(), ex);
        ResponseErrorMessage responseErrorMessage = new ResponseErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, "Sorry, an internal error has occurred.");
        return responseErrorMessage;
    }


}
