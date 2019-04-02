import base.dto.ResponseCode;
import base.dto.response.Response;
import base.exception.BaseException;
import log.service.ErrorLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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

    @Autowired
    private ErrorLogService errorLogService;

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity handleBadRequest(RuntimeException ex, HttpServletRequest request) {
        log.info("[Bad Request] " + request.getMethod() + " ( " + request.getRequestURI() + " )", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(ResponseCode.REQUEST_BAD_DATA, ex.getMessage()));
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity handleBaseException(BaseException ex, HttpServletRequest request) {
        log.error("[Resource Not Found] " + request.getMethod() + " ( " + request.getRequestURI() + " )", ex);
        ResponseCode responseCode = ex.getResponseCode();
        if(responseCode.isLogError()) {
            errorLogService.create(request, ex);
        }
        return ResponseEntity.status(HttpStatus.valueOf(responseCode.getStatusCode())).body(new Response(responseCode, ex.getMessage()));
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity handleDefaultException(Throwable ex, HttpServletRequest request) {
        log.error("[Internal Server Error] " + request.getMethod() + " ( " + request.getRequestURI() + " )", ex);
        errorLogService.create(request, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(ResponseCode.INTERNAL_SERVER_ERROR, ex.getMessage()));
    }
}
