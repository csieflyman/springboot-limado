package com.csieflyman.limado.service;

import com.csieflyman.limado.dao.DagEdgeDao;
import com.csieflyman.limado.dao.PartyDao;
import com.csieflyman.limado.model.Party;
import com.csieflyman.limado.util.query.Predicate;
import com.csieflyman.limado.util.query.Predicates;
import com.csieflyman.limado.util.query.QueryParams;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * author flyman
 */
@Service("partyService")
public class PartyServiceImpl<T extends Party> implements PartyService<T> {

    private static final Logger logger = LoggerFactory.getLogger(PartyServiceImpl.class);

    @Autowired
    @Qualifier("partyDao")
    private PartyDao partyDao;

    @Autowired
    @Qualifier("partyDagEdgeDao")
    private DagEdgeDao<UUID> dagEdgeDao;

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
        T newParty = (T) partyDao.create(party);

        if (children != null && !children.isEmpty()) {
            addChildren(newParty, children);
        }
        if (parents != null && !parents.isEmpty()) {
            addParents(newParty, parents);
        }

        return newParty;
    }

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
            return (T) getById(party.getId(), relations);
        } else {
            return (T) getByTypeAndIdentity(party.getType(), party.getIdentity(), relations);
        }
    }

    @Override
    public T getById(UUID id, String... relations) {
        return getById(id, Sets.newHashSet(relations));
    }

    @Override
    public T getById(UUID id, Set<String> relations) {
        Preconditions.checkArgument(id != null, "id must not be null");
        Optional<T> party;
        if (relations == null || relations.isEmpty()) {
            party = partyDao.getById(id);
        } else {
            List<T> parties = partyDao.find(QueryParams.create().eq("id", id).fetchRelations(relations));
            party = parties.isEmpty() ? Optional.empty() : Optional.of(parties.get(0));
        }

        if (!party.isPresent()) {
            throw new IllegalArgumentException(String.format("party id %s is not exist", id));
        }

        return party.get();
    }

    @Override
    public boolean checkExist(String type, String identity) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(type), "party type must not be empty");
        Preconditions.checkArgument(StringUtils.isNotEmpty(identity), "party identity must not be empty");

        int size = findSize(QueryParams.create().eq("type", type).eq("identity", identity));
        return size > 0;
    }

    @Override
    public void delete(T party) {
        Preconditions.checkArgument(party != null, "party must not be null");

        party = (T) getById(party.getId(), Party.RELATION_PARENT, Party.RELATION_CHILDREN);
        if (party.getChildren() != null && !party.getChildren().isEmpty()) {
            partyDao.removeChildren(party, party.getChildren());
        }
        if (party.getParents() != null && !party.getParents().isEmpty()) {
            partyDao.removeParents(party, party.getParents());
        }
        dagEdgeDao.removeEdgesOfVertex(party.getId());
        partyDao.delete(party);
    }

    @Override
    public List<T> find(QueryParams queryParams) {
        Preconditions.checkArgument(queryParams != null, "queryParams must not be null");
        return partyDao.find(queryParams);
    }

    @Override
    public int findSize(QueryParams queryParams) {
        Preconditions.checkArgument(queryParams != null, "queryParams must not be null");

        return partyDao.findSize(queryParams);
    }

    @Override
    public void enable(Set<UUID> ids) {
        Preconditions.checkArgument(ids != null, "party ids must not be null");

        Map<String, Object> updatedValueMap = new HashMap<>();
        updatedValueMap.put("enabled", true);
        partyDao.executeUpdate(updatedValueMap, Predicates.and(Predicate.in("id", ids)));
    }

    @Override
    public void disable(Set<UUID> ids) {
        Preconditions.checkArgument(ids != null, "party ids must not be null");

        Map<String, Object> updatedValueMap = new HashMap<>();
        updatedValueMap.put("enabled", false);
        partyDao.executeUpdate(updatedValueMap, Predicates.and(Predicate.in("id", ids)));
    }

    @Override
    public Set<T> getParents(UUID id) {
        Preconditions.checkArgument(id != null, "party must not be null");

        List<T> parents = find(QueryParams.create().eq("children.id", id));
        return new HashSet<>(parents);
    }

    @Override
    public Set<T> getChildren(UUID id) {
        Preconditions.checkArgument(id != null, "party must not be null");

        List<T> children = find(QueryParams.create().eq("parents.id", id));
        return new HashSet<>(children);
    }

    @Override
    public Set<T> getAscendants(UUID id) {
        Preconditions.checkArgument(id != null, "id must not be null");

        Set ascendantIds = dagEdgeDao.findIncomingVertices(id);
        if (ascendantIds.isEmpty())
            return new HashSet<>();

        return new HashSet<>(find(QueryParams.create().in("id", ascendantIds)));
    }

    @Override
    public Set<T> getDescendants(UUID id) {
        Preconditions.checkArgument(id != null, "id must not be null");

        Set descendantIds = dagEdgeDao.findOutgoingVertices(id);
        if (descendantIds.isEmpty())
            return new HashSet<>();

        return new HashSet<>(find(QueryParams.create().in("id", descendantIds)));
    }

    @Override
    public void addChild(T parent, Party child) {
        Preconditions.checkArgument(parent != null, "parent must not be null");
        Preconditions.checkArgument(child != null, "child must not be null");

        partyDao.addChild(parent, child);
        dagEdgeDao.addEdges(parent.getId(), child.getId());
    }

    @Override
    public void removeChild(T parent, Party child) {
        Preconditions.checkArgument(parent != null, "parent must not be null");
        Preconditions.checkArgument(child != null, "child must not be null");

        partyDao.removeChild(parent, child);
        dagEdgeDao.removeEdges(parent.getId(), child.getId());
    }

    @Override
    public void addChildren(T parent, Set<Party> children) {
        Preconditions.checkArgument(parent != null, "parent must not be null");
        Preconditions.checkArgument(children != null, "children must not be null");

        if (children.isEmpty())
            return;
        partyDao.addChildren(parent, children);
        children.forEach(child -> dagEdgeDao.addEdges(parent.getId(), child.getId()));
    }

    @Override
    public void removeChildren(T parent, Set<Party> children) {
        Preconditions.checkArgument(parent != null, "parent must not be null");
        Preconditions.checkArgument(children != null, "children must not be null");

        if (children.isEmpty())
            return;
        partyDao.removeChildren(parent, children);
        children.forEach(child -> dagEdgeDao.removeEdges(parent.getId(), child.getId()));
    }

    @Override
    public void addParents(T child, Set<Party> parents) {
        Preconditions.checkArgument(child != null, "child must not be null");
        Preconditions.checkArgument(parents != null, "parents must not be null");

        if (parents.isEmpty())
            return;
        partyDao.addParents(child, parents);
        parents.forEach(parent -> dagEdgeDao.addEdges(parent.getId(), child.getId()));
    }

    @Override
    public void removeParents(T child, Set<Party> parents) {
        Preconditions.checkArgument(child != null, "child must not be null");
        Preconditions.checkArgument(parents != null, "parents must not be null");

        if (parents.isEmpty())
            return;
        partyDao.removeParents(child, parents);
        parents.forEach(parent -> dagEdgeDao.removeEdges(parent.getId(), child.getId()));
    }

    private T getByTypeAndIdentity(String type, String identity, Set<String> relations) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(type), "party type must not be empty");
        Preconditions.checkArgument(StringUtils.isNotEmpty(identity), "party identity must not be empty");

        QueryParams params = QueryParams.create().eq("type", type).eq("identity", identity);
        if (relations != null && !relations.isEmpty()) {
            params.fetchRelations(relations);
        }
        List<T> parties = find(params);
        return parties.isEmpty() ? null : parties.get(0);
    }
}
