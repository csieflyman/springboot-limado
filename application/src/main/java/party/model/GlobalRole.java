package party.model;

import base.exception.NotImplementedException;
import base.model.Permission;
import base.model.Role;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * @author csieflyman
 */
public enum GlobalRole implements Role<String> {

    SYS_ADMIN(ImmutableSet.copyOf(PartyPermission.ALL_PERMISSIONS)), USER(ImmutableSet.of(PartyPermission.CREATE_GROUP));

    public static final String SYS_ADMIN_ID = "sys_admin";
    public static final String USER_ID = "user";

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
