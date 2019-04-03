package graph;

import base.dao.AbstractJPADaoImpl;
import base.util.query.Junction;
import base.util.query.Query;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author csieflyman
 */
@Slf4j
public abstract class DagEdgeDaoImpl<DagEdgeType extends DagEdge<VertexID>, VertexID extends Serializable> extends AbstractJPADaoImpl<DagEdgeType, Long> implements DagEdgeDao<VertexID> {

    abstract protected String getDagId();

    @PersistenceContext
    protected EntityManager em;

    @PostConstruct
    public void init() {
        setEntityManager(em);
    }

    @Override
    public void addEdges(VertexID startVertexId, VertexID endVertexId) {
        Preconditions.checkArgument(startVertexId != null, "Argument [startVertexId] can not be null.");
        Preconditions.checkArgument(endVertexId != null, "Argument [endVertexId] can not be null.");
        log.debug(String.format("add edge: from %s to %s ", startVertexId, endVertexId));

        long size = findSize(Query.create().where().eq("startVertexId", startVertexId).eq("endVertexId", endVertexId)
                .eq("hops", 0).end());
        if (size > 0) {
            throw new IllegalArgumentException(String.format("edge from %s to %s has been exist", startVertexId, endVertexId));
        }

        Set incomingVertices = findIncomingVertices(startVertexId);
        boolean hasCycle = incomingVertices.contains(endVertexId);
        if (hasCycle) {
            throw new IllegalArgumentException(String.format("add edge from %s to %s will cause cycle", startVertexId, endVertexId));
        }

        // insert direct edge
        DagEdgeType edge = newDagEdge();
        edge.setStartVertexId(startVertexId);
        edge.setEndVertexId(endVertexId);
        em.persist(edge);
        Long id = edge.getId();
        log.debug("edge id : " + edge.getId());

        edge.setEntryEdgeId(id);
        edge.setDirectEdgeId(id);
        edge.setExitEdgeId(id);
        em.merge(edge);

        //step 1: A's incoming edges to B
        StringBuilder insertSQL1 = new StringBuilder();
        insertSQL1.append("insert into ").append(getEntityName()).append("(entryEdgeId, directEdgeId, exitEdgeId, startVertexId, endVertexId, hops, dagId) ")
                .append("select id, ").append(id).append("L, ").append(id).append("L, startVertexId, ")
                .append(":endVertexId, (hops + 1), :dagId ")
                .append("from ").append(getEntityName()).append(" and endVertexId = :startVertexId");
        log.debug(insertSQL1.toString());
        javax.persistence.Query query = em.createQuery(insertSQL1.toString());
        query.setParameter("startVertexId", startVertexId).setParameter("endVertexId", endVertexId).setParameter("dagId", edge.getDagId());
        query.executeUpdate();

        //step 2: A to B's outgoing edges
        StringBuilder insertSQL2 = new StringBuilder();
        insertSQL2.append("insert into ").append(getEntityName()).append("(entryEdgeId, directEdgeId, exitEdgeId, startVertexId, endVertexId, hops, dagId) ")
                .append("select ").append(id).append("L, ").append(id).append("L, id, :startVertexId, ")
                .append("endVertexId, (hops + 1), :dagId ")
                .append("from ").append(getEntityName()).append(" and startVertexId = :endVertexId");
        log.debug(insertSQL2.toString());
        query = em.createQuery(insertSQL2.toString());
        query.setParameter("startVertexId", startVertexId).setParameter("endVertexId", endVertexId).setParameter("dagId", edge.getDagId());
        query.executeUpdate();

        // step 3: A’s incoming edges to end vertex of B's outgoing edges
        StringBuilder insertSQL3 = new StringBuilder();
        insertSQL3.append("insert into ").append(getEntityName()).append("(entryEdgeId, directEdgeId, exitEdgeId, startVertexId, endVertexId, hops, dagId) ")
                .append("select edgeA.id, ").append(id).append("L, edgeB.id, edgeA.startVertexId, edgeB.endVertexId, (edgeA.hops + edgeB.hops + 1), :dagId ")
                .append("from ").append(getEntityName()).append(" edgeA, ").append(getEntityName()).append(" edgeB ")
                .append("and edgeA.endVertexId = :startVertexId ")
                .append("and edgeB.startVertexId = :endVertexId");
        log.debug(insertSQL3.toString());
        query = em.createQuery(insertSQL3.toString());
        query.setParameter("startVertexId", startVertexId).setParameter("endVertexId", endVertexId).setParameter("dagId", edge.getDagId());
        query.executeUpdate();
    }

    @Override
    public void removeEdges(VertexID startVertexId, VertexID endVertexId) {
        Preconditions.checkArgument(startVertexId != null, "Argument [startVertexId] can not be null.");
        Preconditions.checkArgument(endVertexId != null, "Argument [endVertexId] can not be null.");
        log.debug(String.format("remove edge: from %s to %s ", startVertexId, endVertexId));

        List<DagEdgeType> edges = find(Query.create().where().eq("startVertexId", startVertexId).eq("endVertexId", endVertexId)
                .eq("hops", 0).eq("dagId", getDagId()).end());
        if (edges.isEmpty()) {
            throw new IllegalArgumentException(String.format("edge from %s to %s does not exist", startVertexId, endVertexId));
        } else {
            DagEdgeType edge = edges.get(0);
            removeEdges(edge.getId());
        }
    }

    @Override
    public void removeEdgesOfVertex(VertexID vertexId) {
        Preconditions.checkArgument(vertexId != null, "Argument [vertex] can not be null.");

        StringBuilder sb = new StringBuilder();
        sb.append("select id from ").append(getEntityName())
                .append(" and (startVertexId = :startVertexId or endVertexId = :endVertexId) ")
                .append("and dagId = :dagId ");
        javax.persistence.Query query = em.createQuery(sb.toString());
        query.setParameter("startVertexId", vertexId).setParameter("endVertexId", vertexId).setParameter("dagId", getDagId());
        List<Long> edgeIds = query.getResultList();
        edgeIds.forEach(edgeId -> removeEdges(edgeId));
    }

    @Override
    public Set<VertexID> findIncomingVertices(VertexID vertexId) {
        List<DagEdgeType> edges = find(Query.create().where().eq("endVertexId", vertexId).eq("dagId", getDagId()).end());
        Set<VertexID> incomingVertexIds = edges.stream().map(DagEdge::getStartVertexId).collect(Collectors.toSet());
        log.debug(String.format("incoming vertices of %s: %s", vertexId, incomingVertexIds));
        return incomingVertexIds;
    }

    @Override
    public Set<VertexID> findOutgoingVertices(VertexID vertexId) {
        List<DagEdgeType> edges = find(Query.create().where().eq("startVertexId", vertexId).eq("dagId", getDagId()).end());
        Set<VertexID> outgoingVertices = edges.stream().map(DagEdge::getEndVertexId).collect(Collectors.toSet());
        log.debug(String.format("outgoing vertices of %s: %s", vertexId, outgoingVertices));
        return outgoingVertices;
    }

    protected DagEdgeType newDagEdge() {
        DagEdgeType edge = super.newInstance();
        edge.setDagId(getDagId());
        return edge;
    }

    private void removeEdges(Long edgeId) {
        log.debug("remove edge : " + edgeId);
        //step 1: rows that were originally inserted with the first AddEdge call for this direct edge
        Set<Long> removeEdgeIds = findIds(Query.create().where().eq("directEdgeId", edgeId).end());
        removeEdgeIds.add(edgeId);
        log.debug("removeEdgeIds = " + removeEdgeIds);

        //step 2: scan and find all dependent rows that are inserted afterwards
        StringBuilder sb = new StringBuilder();
        sb.append("select id from ").append(getEntityName()).append(" and hops > 0 ")
                .append("and (entryEdgeId in :ids or exitEdgeId in :ids) ")
                .append("and id not in :ids");
        while (true) {
            javax.persistence.Query query = em.createQuery(sb.toString());
            query.setParameter("ids", removeEdgeIds);
            List<Long> ids = query.getResultList();
            log.debug("ids = {}", ids);
            if (ids.isEmpty()) {
                break;
            }
            removeEdgeIds.addAll(ids);
        }
        log.debug("removeEdgeIds = " + removeEdgeIds);
        executeDelete(Junction.and().in("id", removeEdgeIds));
    }
}