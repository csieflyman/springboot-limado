package party.service;

import base.util.query.Query;
import com.google.common.base.Preconditions;
import graph.IntervalTreeDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import party.dao.PartyDao;
import party.model.Organization;
import party.model.Party;
import party.model.PartyType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author csieflyman
 */
@Slf4j
@Service("organization")
public class OrganizationServiceImpl extends PartyServiceImpl<Organization> implements OrganizationService {

    @Autowired
    @Qualifier("partyIntervalTreeDao")
    private IntervalTreeDao<UUID> intervalTreeDao;

    @Autowired
    public OrganizationServiceImpl(@Qualifier("partyDao") PartyDao<Organization> partyDao) {
        super(partyDao);
    }

    @Transactional
    @Override
    public void movePartyToOrganization(Party child, Organization organization) {
        Preconditions.checkArgument(child != null, "child must not be null");
        Preconditions.checkArgument(organization != null, "organization must not be null");

        if (child.getType() != PartyType.GROUP) {
            child = getById(child.getId(), Party.RELATION_PARENT);
            Optional<Party> parentOrg = child.getParents().stream().filter(parent -> parent.getType() == PartyType.OU).findFirst();
            if (parentOrg.isPresent()) {
                intervalTreeDao.removeChild(parentOrg.get().getId(), child.getId());
                super.removeChild((Organization) parentOrg.get(), child);
            }
            super.addChild(organization, child);
            intervalTreeDao.addChild(organization.getId(), child.getId());
        } else {
            throw new IllegalArgumentException(String.format("organization %s can't add group child %s", organization, child));
        }
    }

    @Transactional
    @Override
    public void addChild(Organization parent, Party child) {
        Preconditions.checkArgument(parent != null, "parent must not be null");
        Preconditions.checkArgument(child != null, "child must not be null");

        child = getById(child.getId(), Party.RELATION_PARENT);
        validateChildType(child);
        validateParentsRelationship(parent, child);

        super.addChild(parent, child);
        intervalTreeDao.addChild(parent.getId(), child.getId());
    }

    @Transactional
    @Override
    public void removeChild(Organization parent, Party child) {
        Preconditions.checkArgument(parent != null, "parent must not be null");
        Preconditions.checkArgument(child != null, "child must not be null");

        intervalTreeDao.removeChild(parent.getId(), child.getId());
        super.removeChild(parent, child);
    }

    @Transactional
    @Override
    public void addChildren(Organization parent, Set<Party> children) {
        Preconditions.checkArgument(parent != null, "parent must not be null");
        Preconditions.checkArgument(children != null, "children must not be null");

        if (children.isEmpty())
            return;

        children = loadChildren(children);
        for (Party child : children) {
            validateChildType(child);
            validateParentsRelationship(parent, child);
        }

        super.addChildren(parent, children);
        children.forEach(child -> intervalTreeDao.addChild(parent.getId(), child.getId()));
    }

    @Transactional
    @Override
    public void removeChildren(Organization parent, Set<Party> children) {
        Preconditions.checkArgument(parent != null, "parent must not be null");
        Preconditions.checkArgument(children != null, "children must not be null");

        if (children.isEmpty())
            return;

        children.forEach(child -> intervalTreeDao.removeChild(parent.getId(), child.getId()));
        super.removeChildren(parent, children);
    }

    @Transactional
    @Override
    public void addParents(Organization child, Set<Party> parents) {
        Preconditions.checkArgument(child != null, "child must not be null");
        Preconditions.checkArgument(parents != null, "parents must not be null");

        if (parents.isEmpty())
            return;

        Map<PartyType, List<Party>> parentsTypeMap = parents.stream().collect(Collectors.groupingBy(Party::getType));
        if (parentsTypeMap.get(PartyType.USER) != null) {
            throw new IllegalArgumentException(String.format("organization %s can't add user parent %s", child, parentsTypeMap.get(PartyType.USER)));
        } else if (parentsTypeMap.get(PartyType.OU) != null && parentsTypeMap.get(PartyType.OU).size() > 1) {
            throw new IllegalArgumentException(String.format("organization %s can't add above two organization parents %s", child, parentsTypeMap.get(PartyType.OU)));
        }

        super.addParents(child, parents);
        for (Party parent : parents) {
            if (parent.getType() == PartyType.OU) {
                intervalTreeDao.addChild(parent.getId(), child.getId());
            }
        }
    }

    @Transactional
    @Override
    public void removeParents(Organization child, Set<Party> parents) {
        Preconditions.checkArgument(child != null, "child must not be null");
        Preconditions.checkArgument(parents != null, "parents must not be null");

        if (parents.isEmpty())
            return;

        for (Party parent : parents) {
            if (parent.getType() == PartyType.OU) {
                intervalTreeDao.removeChild(parent.getId(), child.getId());
            }
        }
        super.removeParents(child, parents);
    }

    @Transactional
    @Override
    public void delete(Organization organization) {
        Preconditions.checkArgument(organization != null, "organization must not be null");

        intervalTreeDao.delete(organization.getId());
        super.delete(organization);
    }

    @Transactional(readOnly = true)
    @Override
    public Set<Party> getDescendants(UUID id) {
        Preconditions.checkArgument(id != null, "id must not be null");

        List<UUID> descendantIds = intervalTreeDao.getSubTree(id);
        if (descendantIds.isEmpty())
            return new HashSet<>();

        List<? extends Party> parties = find(Query.create().where().in("id", new HashSet<>(descendantIds)).end());
        parties = parties.stream().sorted(Comparator.comparing(party -> descendantIds.indexOf(party.getId()))).collect(Collectors.toList());
        return new LinkedHashSet<>(parties);
    }

    private Set<Party> loadChildren(Set<Party> children) {
        Set<UUID> childrenIds = children.stream().map(Party::getId).collect(Collectors.toSet());
        children = new HashSet<>(find(Query.create().where().in("id", childrenIds).end().fetchRelations(Party.RELATION_PARENT)));
        if (children.size() != childrenIds.size()) {
            Set<UUID> foundChildrenIds = children.stream().map(Party::getId).collect(Collectors.toSet());
            throw new IllegalArgumentException(String.format("children id %s are not exist", CollectionUtils.subtract(childrenIds, foundChildrenIds)));
        }
        return children;
    }

    private void validateChildType(Party child) {
        if (child.getType() == PartyType.GROUP) {
            throw new IllegalArgumentException(String.format("organization can't add group child %s", child.getId()));
        }
    }

    private void validateParentsRelationship(Organization newParent, Party child) {
        Optional<Party> parentOrg = child.getParents().stream().filter(parent -> parent.getType() == PartyType.OU).findFirst();
        if (parentOrg.isPresent()) {
            Party currentParent = parentOrg.get();
            if (currentParent.equals(newParent)) {
                throw new IllegalArgumentException(String.format("parent %s already has child %s", newParent, child));
            } else {
                throw new IllegalArgumentException(String.format("child %s can't have above two parents organization %s and %s", child, newParent, currentParent));
            }
        }
    }
}