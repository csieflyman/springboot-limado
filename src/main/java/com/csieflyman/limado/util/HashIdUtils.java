package com.csieflyman.limado.util;

import org.hashids.Hashids;

/**
 * @author James Lin
 */
public class HashIdUtils {

    private static final String SALT = "69d00b62-b7df-47f6-a756-b7bf6032566c";

    private static final Hashids instance = new Hashids(SALT, 4);

    private HashIdUtils() {

    }

    public static String encode(Long id) {
        return instance.encode(id);
    }

    public static Long decode(String hashId) {
        return instance.decode(hashId)[0];
    }
}
