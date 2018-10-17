package com.csieflyman.limado;

import com.csieflyman.limado.exception.BadRequestException;
import com.csieflyman.limado.exception.ObjectNotFoundException;
import com.csieflyman.limado.util.converter.ConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * @author csieflyman
 */
@ControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionControllerAdvice.class);

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity handleResourceNotFoundException(ObjectNotFoundException ex, HttpServletRequest request) {
        logger.error("[Resource Not Found] " + request.getMethod() + " ( " + request.getRequestURI() + " )", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getErrorResponse());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        logger.error("[Bad Request] " + request.getMethod() + " ( " + request.getRequestURI() + " )", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getErrorResponse());
    }

    //HttpMessageNotReadableException
    @ExceptionHandler(value = {IllegalArgumentException.class, UnsupportedOperationException.class, ConversionException.class})
    public ResponseEntity handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        logger.error("[Bad Request] " + request.getMethod() + " ( " + request.getRequestURI() + " )", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("400", ex.getMessage()));
    }

    // Default Exception
    @ExceptionHandler(Throwable.class)
    public ResponseEntity handleDefaultException(Throwable ex, HttpServletRequest request) {
        logger.error("[Internal Server Error] " + request.getMethod() + " ( " + request.getRequestURI() + " )", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("500", ex.getMessage()));
    }
}
