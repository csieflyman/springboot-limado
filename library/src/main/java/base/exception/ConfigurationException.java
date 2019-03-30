package base.exception;

import base.dto.ResponseCode;

/**
 * @author csieflyman
 */
public class ConfigurationException extends BaseException {

    public ConfigurationException(String message) {
        this(message, null);
    }

    public ConfigurationException(String message, Throwable e) {
        super(message, e, ResponseCode.INTERNAL_SERVER_ERROR);
    }
}
