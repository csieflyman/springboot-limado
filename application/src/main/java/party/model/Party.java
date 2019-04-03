package party.model;

import base.model.Resource;
import base.model.EntityModel;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author csieflyman
 */
@Getter
@Setter
@Entity
@Table(name = "party")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = User.class, name = PartyType.USER_ID),
        @JsonSubTypes.Type(value = Organization.class, name = PartyType.OU_ID),
        @JsonSubTypes.Type(value = Group.class, name = PartyType.GROUP_ID)})
public abstract class Party extends EntityModel<UUID> implements Resource<UUID> {

    public static final String RELATION_CHILDREN = "children";
    public static final String RELATION_PARENT = "parents";

    public Party(PartyType type) {
        this.type = type;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Version
    private Long version;

    @Column
    private String identity;

    @Column
    private String name;

    @Column
    private String email;

    @Column
    private Boolean enabled = true;

    @Enumerated(EnumType.STRING)
    @Column(insertable = false, updatable = false)
    private PartyType type;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "party_rel", joinColumns = {@JoinColumn(name = "parent_id")}, inverseJoinColumns = {@JoinColumn(name = "child_id")})
    private Set<Party> children = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "children")
    private Set<Party> parents = new HashSet<>();

    public void addChild(Party child) {
        if (children == null)
            children = new HashSet<>();
        children.add(child);
        child.addParent(this);
    }

    public void removeChild(Party child) {
        if (children == null)
            children = new HashSet<>();
        children.remove(child);
        child.removeParent(this);
    }

    private void addParent(Party parent) {
        if (parents == null)
            parents = new HashSet<>();
        parents.add(parent);
    }

    private void removeParent(Party parent) {
        if (parents == null)
            parents = new HashSet<>();
        parents.remove(parent);
    }

    public void removeRelations() {
        setParents(null);
        setChildren(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Party party = (Party) o;
        return new EqualsBuilder().append(this.getType(), party.getType()).append(this.getIdentity(), party.getIdentity()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.getType()).append(this.getIdentity()).toHashCode();
    }

    @Override
    public String toString() {
        return toString(this) + ", " + RELATION_CHILDREN + " = " + children + ", " + RELATION_PARENT + " = " + parents;
    }

    private static String toString(Party party) {
        return party.getType() + "/" + party.getIdentity() + " (" + party.getEnabled() + ")" + "(" + party.getId() + ")";
    }
}
