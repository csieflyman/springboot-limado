package party.service;

import base.exception.ObjectNotFoundException;
import base.service.GenericServiceImpl;
import base.util.query.Query;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import graph.DagEdgeDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import party.dao.PartyDao;
import party.model.GlobalRole;
import party.model.Party;
import party.model.PartyType;

import javax.annotation.security.RolesAllowed;
import java.util.*;

/**
 * @author csieflyman
 */
@Slf4j
@Service("partyService")
public class PartyServiceImpl<T extends Party> extends GenericServiceImpl<T, UUID> implements PartyService<T> {

    private PartyDao<T> partyDao;

    @Autowired
    @Qualifier("partyDagEdgeDao")
    private DagEdgeDao<UUID> dagEdgeDao;

    @Autowired
    public PartyServiceImpl(@Qualifier("partyDao") PartyDao<T> partyDao) {
        super(partyDao);
        this.partyDao = partyDao;
    }

    @RolesAllowed(GlobalRole.SYS_ADMIN_ID)
    @Transactional
    @Override
    public T create(T party) {
        Preconditions.checkArgument(party != null, "party must not be null");

        if (checkExist(party.getType(), party.getIdentity())) {
            throw new IllegalArgumentException(String.format("identity %s must be unique of the type %s", party.getIdentity(), party.getType()));
        }

        Set<Party> parents = party.getParents();
        Set<Party> children = party.getChildren();
        party.setParents(new HashSet<>());
        party.setChildren(new HashSet<>());
        T newParty = partyDao.create(party);

        if (children != null && !children.isEmpty()) {
            addChildren(newParty, children);
        }
        if (parents != null && !parents.isEmpty()) {
            addParents(newParty, parents);
        }

        return newParty;
    }

    @Transactional
    @Override
    public void update(T party) {
        Preconditions.checkArgument(party != null, "party must not be null");
        Preconditions.checkArgument(party.getId() != null, "party id must not be null");

        Party oldParty = getById(party.getId(), Party.RELATION_PARENT, Party.RELATION_CHILDREN);
        if (!party.getIdentity().equals(oldParty.getIdentity()) && checkExist(party.getType(), party.getIdentity())) {
            throw new IllegalArgumentException(String.format("identity %s must be unique of the type %s", party.getIdentity(), party.getType()));
        }

        Set<Party> parents = party.getParents();
        Set<Party> children = party.getChildren();
        party.setChildren(oldParty.getChildren());
        party.setParents(oldParty.getParents());
        partyDao.update(party);

        Collection<Party> addParents = parents == null ? Collections.emptySet() : CollectionUtils.subtract(parents, oldParty.getParents());
        Collection<Party> removeParents = parents == null ? Collections.emptySet() : CollectionUtils.subtract(oldParty.getParents(), parents);
        Collection<Party> addChildren = children == null ? Collections.emptySet() : CollectionUtils.subtract(children, oldParty.getChildren());
        Collection<Party> removeChildren = children == null ? Collections.emptySet() : CollectionUtils.subtract(oldParty.getChildren(), children);
        if (!removeChildren.isEmpty()) {
            party = get(party, Party.RELATION_CHILDREN);
            removeChildren(party, new HashSet<>(removeChildren));
        }
        if (!removeParents.isEmpty()) {
            removeParents(party, new HashSet<>(removeParents));
        }
        if (!addChildren.isEmpty()) {
            party = get(party, Party.RELATION_CHILDREN);
            addChildren(party, new HashSet<>(addChildren));
        }
        if (!addParents.isEmpty()) {
            addParents(party, new HashSet<>(addParents));
        }
    }

    @Override
    public T get(T party, String... relations) {
        return get(party, Sets.newHashSet(relations));
    }

    @Override
    public T get(T party, Set<String> relations) {
        Preconditions.checkArgument(party != null, "party must not be null");

        if (party.getId() != null) {
            return getById(party.getId(), relations);
        } else {
            Query query = Query.create().eq("type", party.getType()).eq("identity", party.getIdentity());
            if (!CollectionUtils.isEmpty(relations)) {
                query.fetchRelations(relations);
            }
            return findOne(query).orElseThrow(() -> new ObjectNotFoundException(String.format("party %s doesn't exist.", party.getType() + "/" + party.getIdentity())));
        }
    }

    @Override
    public T getById(UUID id, String... relations) {
        return getById(id, Sets.newHashSet(relations));
    }

    @Override
    public T getById(UUID id, Set<String> relations) {
        Preconditions.checkArgument(id != null, "id must not be null");
        
        return CollectionUtils.isEmpty(relations) ? getById(id) : findOne(Query.create().eq("id", id).fetchRelations(relations))
                .orElseThrow(() -> new ObjectNotFoundException(String.format("party %s doesn't exist.", id)));
    }

    @Override
    public List<Party> findParties(Query query) {
        return (List<Party>) find(query);
    }

    @Override
    public boolean checkExist(PartyType type, String identity) {
        Preconditions.checkNotNull(type);
        Preconditions.checkArgument(StringUtils.isNotEmpty(identity), "party identity must not be empty");

        return findSize(Query.create().eq("type", type).eq("identity", identity)) > 0;
    }

    @Transactional
    @Override
    public void delete(T party) {
        Preconditions.checkArgument(party != null, "party must not be null");

        party = getById(party.getId(), Party.RELATION_PARENT, Party.RELATION_CHILDREN);
        if (party.getChildren() != null && !party.getChildren().isEmpty()) {
            partyDao.removeChildren(party, party.getChildren());
        }
        if (party.getParents() != null && !party.getParents().isEmpty()) {
            partyDao.removeParents(party, party.getParents());
        }
        dagEdgeDao.removeEdgesOfVertex(party.getId());
        partyDao.delete(party);
    }

    @Transactional
    @Override
    public void enable(Set<UUID> ids) {
        Preconditions.checkArgument(ids != null, "party ids must not be null");

        Map<String, Object> updatedValueMap = new HashMap<>();
        updatedValueMap.put("enabled", true);
        partyDao.executeUpdate(updatedValueMap, Query.create().in("id", ids).getJunction());
    }

    @Transactional
    @Override
    public void disable(Set<UUID> ids) {
        Preconditions.checkArgument(ids != null, "party ids must not be null");

        Map<String, Object> updatedValueMap = new HashMap<>();
        updatedValueMap.put("enabled", false);
        partyDao.executeUpdate(updatedValueMap, Query.create().in("id", ids).getJunction());
    }

    @Override
    public Set<Party> getParents(UUID id) {
        Preconditions.checkArgument(id != null, "party must not be null");

        List<T> parents = find(Query.create().eq("children.id", id));
        return new HashSet<>(parents);
    }

    @Override
    public Set<Party> getChildren(UUID id) {
        Preconditions.checkArgument(id != null, "party must not be null");

        List<T> children = find(Query.create().eq("parents.id", id));
        return new HashSet<>(children);
    }

    @Override
    public Set<Party> getAscendants(UUID id) {
        Preconditions.checkArgument(id != null, "id must not be null");

        Set ascendantIds = dagEdgeDao.findIncomingVertices(id);
        if (ascendantIds.isEmpty())
            return new HashSet<>();

        return new HashSet<>(find(Query.create().in("id", ascendantIds)));
    }

    @Override
    public Set<Party> getDescendants(UUID id) {
        Preconditions.checkArgument(id != null, "id must not be null");

        Set descendantIds = dagEdgeDao.findOutgoingVertices(id);
        if (descendantIds.isEmpty())
            return new HashSet<>();

        return new HashSet<>(find(Query.create().in("id", descendantIds)));
    }

    @Transactional
    @Override
    public void addChild(T parent, Party child) {
        Preconditions.checkArgument(parent != null, "parent must not be null");
        Preconditions.checkArgument(child != null, "child must not be null");

        partyDao.addChild(parent, child);
        dagEdgeDao.addEdges(parent.getId(), child.getId());
    }

    @Transactional
    @Override
    public void removeChild(T parent, Party child) {
        Preconditions.checkArgument(parent != null, "parent must not be null");
        Preconditions.checkArgument(child != null, "child must not be null");

        partyDao.removeChild(parent, child);
        dagEdgeDao.removeEdges(parent.getId(), child.getId());
    }

    @Transactional
    @Override
    public void addChildren(T parent, Set<Party> children) {
        Preconditions.checkArgument(parent != null, "parent must not be null");
        Preconditions.checkArgument(children != null, "children must not be null");

        if (children.isEmpty())
            return;
        partyDao.addChildren(parent, children);
        children.forEach(child -> dagEdgeDao.addEdges(parent.getId(), child.getId()));
    }

    @Transactional
    @Override
    public void removeChildren(T parent, Set<Party> children) {
        Preconditions.checkArgument(parent != null, "parent must not be null");
        Preconditions.checkArgument(children != null, "children must not be null");

        if (children.isEmpty())
            return;
        partyDao.removeChildren(parent, children);
        children.forEach(child -> dagEdgeDao.removeEdges(parent.getId(), child.getId()));
    }

    @Transactional
    @Override
    public void addParents(T child, Set<Party> parents) {
        Preconditions.checkArgument(child != null, "child must not be null");
        Preconditions.checkArgument(parents != null, "parents must not be null");

        if (parents.isEmpty())
            return;
        partyDao.addParents(child, parents);
        parents.forEach(parent -> dagEdgeDao.addEdges(parent.getId(), child.getId()));
    }

    @Transactional
    @Override
    public void removeParents(T child, Set<Party> parents) {
        Preconditions.checkArgument(child != null, "child must not be null");
        Preconditions.checkArgument(parents != null, "parents must not be null");

        if (parents.isEmpty())
            return;
        partyDao.removeParents(child, parents);
        parents.forEach(parent -> dagEdgeDao.removeEdges(parent.getId(), child.getId()));
    }
}
