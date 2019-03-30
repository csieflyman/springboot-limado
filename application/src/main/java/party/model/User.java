package party.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author csieflyman
 */
@Entity
@DiscriminatorValue(PartyType.USER_ID)
public class User extends Party {

    public User() {
        this(null);
    }

    public User(String identity) {
        super(PartyType.USER);
        setIdentity(identity);
    }

    @JsonIgnore
    public Optional<Organization> getOrganization() {
        return getParents().stream().filter(party -> party.getType() == PartyType.OU).map(party -> (Organization) party).findAny();
    }

    @JsonIgnore
    public Set<Group> getGroups() {
        return getParents().stream().filter(party -> party.getType() == PartyType.GROUP).map(party -> (Group) party).collect(Collectors.toSet());
    }
}
