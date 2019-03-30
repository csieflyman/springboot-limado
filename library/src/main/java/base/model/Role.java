package base.model;

import java.util.Set;

/**
 * @author csieflyman
 */
public interface Role<ID> extends Identifiable<ID> {

    boolean isGlobalRole();

    Set<Permission> getPermissions();

    default boolean hasPermission(Permission permission) {
        return getPermissions().contains(permission);
    }
}
