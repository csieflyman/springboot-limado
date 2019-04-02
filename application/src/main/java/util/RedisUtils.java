package util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author csieflyman
 */
@Slf4j
public class RedisUtils {

    private static RedisTemplate<String, Object> template;

    private RedisUtils() {
    }

    public static void setRedisTemplate(RedisTemplate<String, Object> template) {
        RedisUtils.template = template;
    }

    private static void set(String key, Object value) {
        try {
            template.opsForValue().set(key, value);
        }catch (Throwable e) {
            log.warn("fail to set cache key = {}", key, e);
        }
    }

    private static void set(String key, Object value, long expiration) {
        try {
            template.opsForValue().set(key, value, expiration, TimeUnit.SECONDS);
        }catch (Throwable e) {
            log.warn("fail to set cache key = {} with expire {} seconds", key, expiration, e);
        }
    }

    private static <T> T get(String key) {
        try {
            return (T) template.opsForValue().get(key);
        }catch (Throwable e) {
            log.warn("fail to get cache key = {}", key, e);
        }
        return null;
    }

    private static void delete(String key) {
        try {
            template.delete(key);
        }catch (Throwable e) {
            log.warn("fail to remove cache key = {}", key, e);
        }
    }

    private static final String SESSION_PREFIX = "session";

    public static void setSession(String sessionId, Subject subject, int expiration) {
        set(composeKey(SESSION_PREFIX, sessionId), subject, expiration);
    }

    public static void setSession(String sessionId, Subject subject) {
        set(composeKey(SESSION_PREFIX, sessionId), subject);
    }

    public static Subject getSession(String sessionId) {
        return get(composeKey(SESSION_PREFIX, sessionId));
    }

    public static void deleteSession(String sessionId) {
        delete(composeKey(SESSION_PREFIX, sessionId));
    }

    private static String composeKey(String... args) {
        return String.join(":", args);
    }
}
