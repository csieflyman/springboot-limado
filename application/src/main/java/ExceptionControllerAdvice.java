import base.dto.response.Response;
import base.dto.ResponseCode;
import base.exception.BadRequestException;
import base.exception.BaseException;
import base.exception.ConversionException;
import base.exception.ObjectNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * @author csieflyman
 */
@Slf4j
@ControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

    private ErrorLog

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity handleResourceNotFoundException(ObjectNotFoundException ex, HttpServletRequest request) {
        log.error("[Resource Not Found] " + request.getMethod() + " ( " + request.getRequestURI() + " )", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getErrorResponse());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        log.error("[Bad Request] " + request.getMethod() + " ( " + request.getRequestURI() + " )", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getErrorResponse());
    }

    //HttpMessageNotReadableException
    @ExceptionHandler(value = {IllegalArgumentException.class, UnsupportedOperationException.class, ConversionException.class})
    public ResponseEntity handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        log.error("[Bad Request] " + request.getMethod() + " ( " + request.getRequestURI() + " )", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse("400", ex.getMessage()));
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity handleBaseException(BaseException ex, HttpServletRequest request) {
        ResponseCode responseCode = ex.getResponseCode();
        Object result = ex.getResult();
        int statusCode = responseCode.getStatusCode();

        if(responseCode.logError()) {
            errorLogService.create(ctx.request(), e);
        }

        log.error("[Resource Not Found] " + request.getMethod() + " ( " + request.getRequestURI() + " )", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getErrorResponse());
    }

    // Default Exception
    @ExceptionHandler(Throwable.class)
    public ResponseEntity handleDefaultException(Throwable ex, HttpServletRequest request) {
        log.error("[Internal Server Error] " + request.getMethod() + " ( " + request.getRequestURI() + " )", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response("500", ex.getMessage()));
    }
}
