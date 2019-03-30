package party.model;

import base.exception.NotImplementedException;
import base.model.Operation;
import base.model.Permission;
import com.google.common.collect.ImmutableSet;

import java.util.HashSet;
import java.util.Set;

/**
 * @author csieflyman
 */
public final class PartyPermission extends Permission<PartyType> {

    // OU
    public static final PartyPermission CREATE_OU = new PartyPermission(Operation.CREATE, PartyType.OU);
    public static final PartyPermission UPDATE_OU = new PartyPermission(Operation.UPDATE, PartyType.OU);
    public static final PartyPermission DELETE_OU = new PartyPermission(Operation.DELETE, PartyType.OU);
    // OU Relation
    public static final PartyPermission ADD_OU_TO_OU = new PartyPermission(Operation.ADD, PartyType.OU, PartyType.OU);
    public static final PartyPermission REMOVE_OU_FROM_OU = new PartyPermission(Operation.REMOVE, PartyType.OU, PartyType.OU);
    public static final PartyPermission ADD_USER_TO_OU = new PartyPermission(Operation.ADD, PartyType.OU, PartyType.USER);
    public static final PartyPermission REMOVE_USER_FROM_OU = new PartyPermission(Operation.REMOVE, PartyType.OU, PartyType.USER);
    // GROUP
    public static final PartyPermission CREATE_GROUP = new PartyPermission(Operation.CREATE, PartyType.GROUP);
    public static final PartyPermission UPDATE_GROUP = new PartyPermission(Operation.UPDATE, PartyType.GROUP);
    public static final PartyPermission DELETE_GROUP = new PartyPermission(Operation.DELETE, PartyType.GROUP);
    // GROUP Relation
    public static final PartyPermission ADD_USER_TO_GROUP = new PartyPermission(Operation.ADD, PartyType.GROUP, PartyType.USER);
    public static final PartyPermission REMOVE_USER_FROM_GROUP = new PartyPermission(Operation.REMOVE, PartyType.GROUP, PartyType.USER);
    public static final PartyPermission ADD_GROUP_TO_GROUP = new PartyPermission(Operation.ADD, PartyType.GROUP, PartyType.GROUP);
    public static final PartyPermission REMOVE_GROUP_FROM_GROUP = new PartyPermission(Operation.REMOVE, PartyType.GROUP, PartyType.GROUP);
    public static final PartyPermission ADD_OU_TO_GROUP = new PartyPermission(Operation.ADD, PartyType.GROUP, PartyType.OU);
    public static final PartyPermission REMOVE_OU_FROM_GROUP = new PartyPermission(Operation.REMOVE, PartyType.GROUP, PartyType.OU);
    // USER
    public static final PartyPermission CREATE_USER = new PartyPermission(Operation.CREATE, PartyType.USER);
    public static final PartyPermission UPDATE_USER = new PartyPermission(Operation.UPDATE, PartyType.USER);
    public static final PartyPermission DELETE_USER = new PartyPermission(Operation.DELETE, PartyType.USER);

    public static final Set<PartyPermission> OU_PERMISSIONS = ImmutableSet.of(CREATE_OU, UPDATE_OU, DELETE_OU, ADD_OU_TO_OU, REMOVE_OU_FROM_OU, ADD_USER_TO_OU, REMOVE_USER_FROM_OU);
    public static final Set<PartyPermission> GROUP_PERMISSIONS = ImmutableSet.of(CREATE_GROUP, UPDATE_GROUP, DELETE_GROUP,
            ADD_USER_TO_GROUP, REMOVE_USER_FROM_GROUP, ADD_GROUP_TO_GROUP, REMOVE_GROUP_FROM_GROUP, ADD_OU_TO_GROUP, REMOVE_OU_FROM_GROUP);
    public static final Set<PartyPermission> USER_PERMISSIONS = ImmutableSet.of(CREATE_USER, UPDATE_USER, DELETE_USER);

    public static final Set<PartyPermission> ALL_PERMISSIONS = new HashSet<>();
    static {
        ALL_PERMISSIONS.addAll(OU_PERMISSIONS);
        ALL_PERMISSIONS.addAll(GROUP_PERMISSIONS);
        ALL_PERMISSIONS.addAll(USER_PERMISSIONS);
    }

    public static Set<PartyPermission> getPermissions(PartyType partyType) {
        switch (partyType) {
            case OU:
                return OU_PERMISSIONS;
            case GROUP:
                return GROUP_PERMISSIONS;
            case USER:
                return USER_PERMISSIONS;
            default:
                throw new NotImplementedException("Undefined PartyType");
        }
    }

    private PartyPermission(Operation operation, PartyType partyType) {
        super(operation, partyType);
    }

    private PartyPermission(Operation operation, PartyType parentType, PartyType childType) {
        super(operation, parentType, childType);
    }
}
