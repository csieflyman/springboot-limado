package util;

import com.google.common.base.Preconditions;
import org.hashids.Hashids;

/**
 * @author csieflyman
 */
public class HashIdUtils {

    private static final String SALT = "69d00b62-b7df-47f6-a756-b7bf6032566c";

    private static final Hashids instance = new Hashids(SALT, 4);

    private HashIdUtils() {

    }

    public static String encode(Long id) {
        Preconditions.checkNotNull(id);
        return instance.encode(id);
    }

    public static Long decode(String hashId) {
        Preconditions.checkNotNull(hashId);
        return instance.decode(hashId)[0];
    }
}
