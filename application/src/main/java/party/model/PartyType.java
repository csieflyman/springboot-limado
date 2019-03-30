package party.model;

import base.exception.NotImplementedException;
import base.model.ResourceType;

/**
 * @author csieflyman
 */
public enum PartyType implements ResourceType<String> {

    OU, GROUP, USER;

    public static final String OU_ID = "ou";
    public static final String GROUP_ID = "group";
    public static final String USER_ID = "user";

    @Override
    public String getId() {
        switch (this) {
            case OU:
                return OU_ID;
            case GROUP:
                return GROUP_ID;
            case USER:
                return USER_ID;
            default:
                throw new NotImplementedException("Undefined PartyType");
        }
    }


    @Override
    public String toString() {
        return getId();
    }
}
