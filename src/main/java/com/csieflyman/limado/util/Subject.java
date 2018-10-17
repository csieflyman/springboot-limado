package com.csieflyman.limado.util;

import java.io.Serializable;

/**
 * @author csieflyman
 */
public class Subject implements Serializable {

    private static final long serialVersionUID = -4611677829653296723L;

    private Boolean isSuperUser = false;

    private Subject() {
    }

    public static Subject createSuperUser() {
        Subject subject = new Subject();
        subject.isSuperUser = true;
        return subject;
    }

    public static Subject createGuestUser() {
        return new Subject();
    }
}
