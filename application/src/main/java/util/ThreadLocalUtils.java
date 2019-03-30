package util;

import admin.model.User;
import auth.AuthSource;
import member.point.PointVenderService;

import java.util.UUID;

/**
 * @author csieflyman
 */
public class ThreadLocalUtils {

    private static final ThreadLocal<Subject> subjectThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<AuthSource> authSourceThreadLocal = new ThreadLocal<>();
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

    public static String getOauthSub() {
        return getSubject().getOauthSub();
    }

    public static String getIdentity() {
        return getSubject().getIdentity();
    }

    public static UUID getMemberId() {
        return UUID.fromString(getIdentity());
    }

    public static String getAccount() {
        return getSubject().getAccount();
    }

    public static Long getBrandId() {
        return getSubject().getBrandId();
    }

    public static Long getStoreId() {
        return getSubject().getStoreId();
    }

    public static Long getUserId() {
        return getSubject().getUserId();
    }

    public static SubjectRole getRole() {
        return getSubject().getRole();
    }

    public static boolean isAdmin() {
        return getSubject().isAdmin();
    }

    public static boolean isBrandAdmin() {
        return getSubject().isBrandAdmin();
    }

    public static boolean isStoreAdmin() {
        return getSubject().isStoreAdmin();
    }

    public static boolean isMember() {
        return getSubject().isMember();
    }

    public static boolean hasPermission(User.Role role) {
        return isAdmin() ||
                (role == User.Role.BRAND && getRole() == SubjectRole.BRAND_ADMIN) ||
                (role == User.Role.STORE && (getRole() == SubjectRole.BRAND_ADMIN || getRole() == SubjectRole.STORE_ADMIN));
    }

    public static String getPointVender() {
        return PointVenderService.DEFAULT_VENDER; //目前只串接一家服務廠商，所以直接回傳預設值，就不必查詢 DB 的 brand 資料表了
    }

    public static void setAuthSource(AuthSource authSource) {
        authSourceThreadLocal.set(authSource);
    }

    public static AuthSource getAuthSource() {
        return authSourceThreadLocal.get();
    }

    public static String getAuthSourceName() {
        AuthSource source = authSourceThreadLocal.get();
        return source != null ? source.name() : null;
    }

    public static void setSessionId(String sessionId) {
        sessionThreadLocal.set(sessionId);
    }

    public static String getSessionId() {
        return sessionThreadLocal.get();
    }

    public static void clear() {
        subjectThreadLocal.remove();
        authSourceThreadLocal.remove();
        sessionThreadLocal.remove();
        bigDecimalConfigThreadLocal.remove();
    }
}
