package party.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author csieflyman
 */
@Entity
@DiscriminatorValue(PartyType.GROUP_ID)
public class Group extends Party {

    public Group() {
        this(null);
    }

    public Group(String identity) {
        super(PartyType.GROUP);
        setIdentity(identity);
    }

    @JsonIgnore
    public Set<User> getUsers() {
        return getChildren().stream().filter(party -> party.getType() == PartyType.USER).map(party -> (User) party).collect(Collectors.toSet());
    }

    @JsonIgnore
    public Set<Organization> getOrganizations() {
        return getChildren().stream().filter(party -> party.getType() == PartyType.OU).map(party -> (Organization) party).collect(Collectors.toSet());
    }

    @JsonIgnore
    public Set<Group> getSubGroups() {
        return getChildren().stream().filter(party -> party.getType() == PartyType.GROUP).map(party -> (Group) party).collect(Collectors.toSet());
    }

    @JsonIgnore
    public Set<Group> getSuperGroups() {
        return getParents().stream().filter(party -> party.getType() == PartyType.GROUP).map(party -> (Group) party).collect(Collectors.toSet());
    }
}
