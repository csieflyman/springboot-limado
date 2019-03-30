package party.model;

import base.exception.NotImplementedException;
import base.model.Permission;
import base.model.Role;
import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * @author csieflyman
 */
public enum PartyRole implements Role<String> {

    OU_ADMIN(ImmutableSet.of(PartyPermission.UPDATE_OU, PartyPermission.DELETE_OU)),
    GROUP_ADMIN(ImmutableSet.of(PartyPermission.UPDATE_GROUP, PartyPermission.DELETE_GROUP)),
    MEMBER(ImmutableSet.of());

    public static final String OU_ADMIN_ID = "ou_admin";
    public static final String GROUP_ADMIN_ID = "group_admin";
    public static final String MEMBER_ID = "member";

    private Set<Permission> permissions;

    PartyRole(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    @Override
    public String getId() {
        switch (this) {
            case OU_ADMIN:
                return OU_ADMIN_ID;
            case GROUP_ADMIN:
                return GROUP_ADMIN_ID;
            case MEMBER:
                return MEMBER_ID;
            default:
                throw new NotImplementedException("Undefined PartyRole");
        }
    }

    @Override
    public boolean isGlobalRole() {
        return false;
    }

    @Override
    public Set<Permission> getPermissions() {
        return permissions;
    }
}
