package com.csieflyman.limado.exception;

import com.csieflyman.limado.dto.ResponseCode;

/**
 * @author James Lin
 */
public class ConfigurationException extends BaseException {

    public ConfigurationException(String message) {
        this(message, null);
    }

    public ConfigurationException(String message, Throwable e) {
        super(message, e, ResponseCode.INTERNAL_SERVER_ERROR);
    }
}
