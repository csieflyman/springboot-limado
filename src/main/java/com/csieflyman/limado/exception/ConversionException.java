package com.csieflyman.limado.exception;

import com.csieflyman.limado.dto.ResponseCode;

/**
 * @author James Lin
 */
public class ConversionException extends BaseException {

    public ConversionException(String message, Throwable cause) {
        super(message, cause, ResponseCode.REQUEST_BAD_DATA);
    }
}
