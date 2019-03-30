package party.model;

import base.exception.NotImplementedException;
import base.model.Permission;
import base.model.Role;
import com.google.common.collect.ImmutableSet;

import java.util.Collections;
import java.util.Set;

/**
 * @author csieflyman
 */
public enum GlobalRole implements Role<String> {

    SYS_ADMIN(ImmutableSet.copyOf(PartyPermission.ALL_PERMISSIONS)), USER(ImmutableSet.of(PartyPermission.CREATE_GROUP)), GUEST(Collections.emptySet());

    public static final String SYS_ADMIN_ID = "sys_admin";
    public static final String USER_ID = "user";
    public static final String GUEST_ID = "guest";

    private Set<Permission> permissions;

    GlobalRole(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    @Override
    public String getId() {
        switch (this) {
            case SYS_ADMIN:
                return SYS_ADMIN_ID;
            case USER:
                return USER_ID;
            case GUEST:
                return GUEST_ID;
            default:
                throw new NotImplementedException("Undefined GlobalRole");
        }
    }

    @Override
    public boolean isGlobalRole() {
        return true;
    }

    @Override
    public Set<Permission> getPermissions() {
        return permissions;
    }
}
