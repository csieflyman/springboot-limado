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
@DiscriminatorValue(PartyType.OU_ID)
public class Organization extends Party {

    public Organization() {
        this(null);
    }

    public Organization(String identity) {
        super(PartyType.OU);
        setIdentity(identity);
    }

    @JsonIgnore
    public Set<User> getUsers() {
        return getChildren().stream().filter(party -> party.getType() == PartyType.USER).map(party -> (User) party).collect(Collectors.toSet());
    }

    @JsonIgnore
    public Set<Organization> getSubOrganizations() {
        return getChildren().stream().filter(party -> party.getType() == PartyType.OU).map(party -> (Organization) party).collect(Collectors.toSet());
    }

    @JsonIgnore
    public Optional<Organization> getSuperOrganization() {
        return getParents().stream().filter(party -> party.getType() == PartyType.OU).map(party -> (Organization) party).findAny();
    }
}
