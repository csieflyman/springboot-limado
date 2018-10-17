package com.csieflyman.limado.util;

/**
 * @author csieflyman
 */
public class ThreadLocalUtils {

    private static final ThreadLocal<Subject> subjectThreadLocal = new ThreadLocal<>();

    private ThreadLocalUtils() {

    }

    public static Subject getSubject() {
        Subject subject = subjectThreadLocal.get();
        if(subject == null) {
            throw new IllegalStateException("subject is not set in thread local");
        }
        return subject;
    }

    public static void setSubject(Subject subject) {
        subjectThreadLocal.set(subject);
    }

    public static void clear() {
        subjectThreadLocal.remove();
    }
}
