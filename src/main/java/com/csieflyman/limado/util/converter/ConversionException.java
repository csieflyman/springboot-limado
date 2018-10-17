package com.csieflyman.limado.util.converter;

/**
 * @author flyman
 */
public class ConversionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ConversionException(String message) {
        this(message, null);
    }

    public ConversionException(String message, Throwable e) {
        super(message, e);
    }
}
