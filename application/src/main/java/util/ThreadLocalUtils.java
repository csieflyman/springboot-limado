package util;

import party.model.GlobalRole;

import java.util.Set;

/**
 * @author csieflyman
 */
public class ThreadLocalUtils {

    private static final ThreadLocal<Subject> subjectThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> sessionThreadLocal = new ThreadLocal<>();

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

    public static void setAdmin() {
        setSubject(Subject.createAdmin());
    }

    public static String getIdentity() {
        return getSubject().getIdentity();
    }

    public static String getAccount() {
        return getSubject().getAccount();
    }

    public static Set<GlobalRole> getRoles() {
        return getSubject().getRoles();
    }

    public static boolean isAdmin() {
        return getSubject().isAdmin();
    }

    public static void setSessionId(String sessionId) {
        sessionThreadLocal.set(sessionId);
    }

    public static String getSessionId() {
        return sessionThreadLocal.get();
    }

    public static void clear() {
        subjectThreadLocal.remove();
        sessionThreadLocal.remove();
    }
}
