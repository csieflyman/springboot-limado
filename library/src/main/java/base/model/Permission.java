package base.model;

import lombok.Getter;

/**
 * @author csieflyman
 */
public abstract class Permission<T extends ResourceType> implements Identifiable<String> {

    @Getter
    protected Operation operation;
    @Getter
    protected T resourceType;
    protected ResourceType parentType;
    protected ResourceType childType;
    @Getter
    protected boolean isRelationPermission;

    public Permission(Operation operation, T resourceType) {
        this.operation = operation;
        this.resourceType = resourceType;
        this.isRelationPermission = false;
    }

    public Permission(Operation operation, ResourceType parentType, ResourceType childType) {
        this.operation = operation;
        this.parentType = parentType;
        this.childType = childType;
        this.isRelationPermission = true;
    }

    public ResourceType getParentType() {
        if(!isRelationPermission()) {
            throw new IllegalStateException(String.format("permission %s is not relation permission", this));
        }
        return parentType;
    }

    public ResourceType getChildType() {
        if(!isRelationPermission()) {
            throw new IllegalStateException(String.format("permission %s is not relation permission", this));
        }
        return childType;
    }

    @Override
    public String getId() {
        return operation + "_" + (isRelationPermission ? parentType.getId() + "_" + childType.getId() : resourceType.getId());
    }

    @Override
    public String toString() {
        return getId();
    }
}
