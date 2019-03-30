package util;

import admin.model.Store;
import admin.model.User;
import com.google.common.base.Preconditions;
import member.model.Member;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * @author csieflyman
 */
public class Subject implements Serializable{

    private static final long serialVersionUID = -4611677829653296723L;

    private String identity;
    private String account;
    private SubjectRole role;

    private Subject() {
    }

    public static Subject createAdmin() {
        Subject subject = new Subject();
        subject.role = SubjectRole.ADMIN;
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

    public Long getBrandId() {
        Preconditions.checkNotNull(brandId);
        return brandId;
    }

    public void setBrandId(Long brandId) {
        Preconditions.checkNotNull(brandId);
        Preconditions.checkState(isAdmin() || isBrandAdmin());
        this.brandId = brandId;
    }

    public Long getStoreId() {
        Preconditions.checkNotNull(storeId);
        return storeId;
    }

    public void setStoreId(Long storeId) {
        Preconditions.checkNotNull(storeId);
        Preconditions.checkState(isAdmin() || isBrandAdmin() || isStoreAdmin());
        this.storeId = storeId;
    }

    public Long getUserId() {
        Preconditions.checkNotNull(userId);
        return userId;
    }

    public SubjectRole getRole() {
        Preconditions.checkNotNull(role);
        return role;
    }

    public Boolean isAdmin() {
        return role == SubjectRole.ADMIN;
    }

    public boolean isBrandAdmin() {
        return role == SubjectRole.BRAND_ADMIN;
    }

    public boolean isStoreAdmin() {
        return role == SubjectRole.STORE_ADMIN;
    }

    public boolean isMember() {
        return role == SubjectRole.MEMBER;
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
        return brandId + "/" + storeId + "/" + identity + "(" + account + ")" + "(" + role + ")";
    }
}
