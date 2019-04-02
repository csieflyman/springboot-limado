package util;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import party.model.GlobalRole;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author csieflyman
 */
public class Subject implements Serializable{

    private static final long serialVersionUID = -4611677829653296723L;

    private String identity;
    private String account;
    private Set<GlobalRole> roles = new HashSet<>();

    private Subject() {
        roles.add(GlobalRole.USER);
    }

    public static Subject createAdmin() {
        Subject subject = new Subject();
        subject.roles.add(GlobalRole.SYS_ADMIN);
        return subject;
    }

    public String getIdentity() {
        Preconditions.checkNotNull(identity);
        return identity;
    }

    public String getAccount() {
        Preconditions.checkNotNull(account);
        return account;
    }

    public Set<GlobalRole> getRoles() {
        return roles;
    }

    public Boolean isAdmin() {
        return roles.contains(GlobalRole.SYS_ADMIN);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identity).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Subject subject = (Subject) obj;
        return new EqualsBuilder().append(identity, subject.getIdentity()).isEquals();
    }

    @Override
    public String toString() {
        return identity + "(" + account + ")" + "(" + roles + ")";
    }
}
