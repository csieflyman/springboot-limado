package com.csieflyman.limado.util;

import com.google.inject.Inject;
import models.SettingCacheDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.SyncCacheApi;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author James Lin
 */
public class CacheUtils {

    private static final Logger logger = LoggerFactory.getLogger(CacheUtils.class);

    @Inject
    private static SyncCacheApi cacheApi;

    private CacheUtils() {
    }

    private static void set(String key, Object value) {
        try {
            cacheApi.set(key, value);
        }catch (Throwable e) {
            logger.warn("fail to set cache key = {}", key, e);
        }
    }

    private static void set(String key, Object value, int expiration) {
        try {
            cacheApi.set(key, value, expiration);
        }catch (Throwable e) {
            logger.warn("fail to set cache key = {}", key, e);
        }
    }

    private static <T> T get(String key) {
        try {
            return cacheApi.get(key);
        }catch (Throwable e) {
            logger.warn("fail to get cache key = {}", key, e);
        }
        return null;
    }

    private static void remove(String key) {
        try {
            cacheApi.remove(key);
        }catch (Throwable e) {
            logger.warn("fail to remove cache key = {}", key, e);
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

    public static void removeSession(String sessionId) {
        remove(composeKey(SESSION_PREFIX, sessionId));
    }

    private static String composeKey(String... args) {
        return Arrays.stream(args).collect(Collectors.joining(":"));
    }
}
