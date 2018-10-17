package com.csieflyman.limado.model;

import java.io.Serializable;

/**
 * @author csieflyman
 */
public interface Identifiable<ID> extends Serializable {

    ID getId();
}
