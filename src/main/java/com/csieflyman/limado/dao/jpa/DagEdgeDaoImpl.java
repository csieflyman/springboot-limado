package com.csieflyman.limado.dao.jpa;

import com.csieflyman.limado.dao.DagEdgeDao;
import com.csieflyman.limado.model.DagEdge;
import com.csieflyman.limado.util.query.Predicate;
import com.csieflyman.limado.util.query.QueryParams;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Query;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

abstract class DagEdgeDaoImpl<DagEdgeType extends DagEdge<VertexID>, VertexID extends Serializable> extends JpaGenericDaoImpl<DagEdgeType, Long> implements DagEdgeDao<VertexID> {

    private static final Logger logger = LoggerFactory.getLogger(DagEdgeDaoImpl.class);

    abstract protected String getDagId();

    @Override
    public void addEdges(VertexID startVertexId, VertexID endVertexId) {
        Preconditions.checkArgument(startVertexId != null, "Argument [startVertexId] can not be null.");
        Preconditions.checkArgument(endVertexId != null, "Argument [endVertexId] can not be null.");
        logger.debug(String.format("add edge: from %s to %s ", startVertexId, endVertexId));

        int size = findSize(QueryParams.create().eq("startVertexId", startVertexId).eq("endVertexId", endVertexId)
                .eq("hops", 0));
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
        entityManager.persist(edge);
        Long id = edge.getId();
        logger.debug("edge id : " + edge.getId());

        edge.setEntryEdgeId(id);
        edge.setDirectEdgeId(id);
        edge.setExitEdgeId(id);
        entityManager.merge(edge);

        //step 1: A's incoming edges to B
        StringBuilder insertSQL1 = new StringBuilder();
        insertSQL1.append("insert into ").append(getEntityName()).append("(entryEdgeId, directEdgeId, exitEdgeId, startVertexId, endVertexId, hops, dagId) ")
                .append("select id, ").append(id).append("L, ").append(id).append("L, startVertexId, ")
                .append(":endVertexId, (hops + 1), :dagId ")
                .append("from ").append(getEntityName()).append(" where endVertexId = :startVertexId");
        logger.debug(insertSQL1.toString());
        Query query = entityManager.createQuery(insertSQL1.toString());
        query.setParameter("startVertexId", startVertexId);
        query.setParameter("endVertexId", endVertexId);
        query.setParameter("dagId", edge.getDagId());
        query.executeUpdate();

        //step 2: A to B's outgoing edges
        StringBuilder insertSQL2 = new StringBuilder();
        insertSQL2.append("insert into ").append(getEntityName()).append("(entryEdgeId, directEdgeId, exitEdgeId, startVertexId, endVertexId, hops, dagId) ")
                .append("select ").append(id).append("L, ").append(id).append("L, id, :startVertexId, ")
                .append("endVertexId, (hops + 1), :dagId ")
                .append("from ").append(getEntityName()).append(" where startVertexId = :endVertexId");
        logger.debug(insertSQL2.toString());
        query = entityManager.createQuery(insertSQL2.toString());
        query.setParameter("startVertexId", startVertexId);
        query.setParameter("endVertexId", endVertexId);
        query.setParameter("dagId", edge.getDagId());
        query.executeUpdate();

        // step 3: A’s incoming edges to end vertex of B's outgoing edges
        StringBuilder insertSQL3 = new StringBuilder();
        insertSQL3.append("insert into ").append(getEntityName()).append("(entryEdgeId, directEdgeId, exitEdgeId, startVertexId, endVertexId, hops, dagId) ")
                .append("select edgeA.id, ").append(id).append("L, edgeB.id, edgeA.startVertexId, edgeB.endVertexId, (edgeA.hops + edgeB.hops + 1), :dagId ")
                .append("from ").append(getEntityName()).append(" edgeA, ").append(getEntityName()).append(" edgeB ")
                .append("where edgeA.endVertexId = :startVertexId ")
                .append("and edgeB.startVertexId = :endVertexId");
        logger.debug(insertSQL3.toString());
        query = entityManager.createQuery(insertSQL3.toString());
        query.setParameter("startVertexId", startVertexId);
        query.setParameter("endVertexId", endVertexId);
        query.setParameter("dagId", edge.getDagId());
        query.executeUpdate();
    }

    @Override
    public void removeEdges(VertexID startVertexId, VertexID endVertexId) {
        Preconditions.checkArgument(startVertexId != null, "Argument [startVertexId] can not be null.");
        Preconditions.checkArgument(endVertexId != null, "Argument [endVertexId] can not be null.");
        logger.debug(String.format("remove edge: from %s to %s ", startVertexId, endVertexId));

        List<DagEdgeType> edges = find(QueryParams.create().eq("startVertexId", startVertexId).eq("endVertexId", endVertexId)
                .eq("hops", 0).eq("dagId", getDagId()));
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
                .append(" where (startVertexId = :startVertexId or endVertexId = :endVertexId) ")
                .append("and dagId = :dagId ");
        Query query = entityManager.createQuery(sb.toString());
        query.setParameter("startVertexId", vertexId);
        query.setParameter("endVertexId", vertexId);
        query.setParameter("dagId", getDagId());
        List<Long> edgeIds = query.getResultList();
        edgeIds.forEach(edgeId -> removeEdges(edgeId));
    }

    @Override
    public Set<VertexID> findIncomingVertices(VertexID vertexId) {
        List<DagEdgeType> edges = find(QueryParams.create().eq("endVertexId", vertexId).eq("dagId", getDagId()));
        Set<VertexID> incomingVertexIds = edges.stream().map(DagEdge::getStartVertexId).collect(Collectors.toSet());
        logger.debug(String.format("incoming vertices of %s: %s", vertexId, incomingVertexIds));
        return incomingVertexIds;
    }

    @Override
    public Set<VertexID> findOutgoingVertices(VertexID vertexId) {
        List<DagEdgeType> edges = find(QueryParams.create().eq("startVertexId", vertexId).eq("dagId", getDagId()));
        Set<VertexID> outgoingVertices = edges.stream().map(DagEdge::getEndVertexId).collect(Collectors.toSet());
        logger.debug(String.format("outgoing vertices of %s: %s", vertexId, outgoingVertices));
        return outgoingVertices;
    }

    protected DagEdgeType newDagEdge() {
        DagEdgeType edge = super.newInstance();
        edge.setDagId(getDagId());
        return edge;
    }

    private void removeEdges(Long edgeId) {
        logger.debug("remove edge : " + edgeId);
        //step 1: rows that were originally inserted with the first AddEdge call for this direct edge
        Query query = entityManager.createQuery("from " + getEntityName() + " where directEdgeId = :id");
        query.setParameter("id", edgeId);
        List<DagEdgeType> directEdges = query.getResultList();

        Set<Long> removeEdgeIds = directEdges.stream().map(DagEdge::getId).collect(Collectors.toSet());
        removeEdgeIds.add(edgeId);
        logger.debug("removeEdgeIds = " + removeEdgeIds);

        //step 2: scan and find all dependent rows that are inserted afterwards
        StringBuilder sb = new StringBuilder();
        sb.append("select id from ").append(getEntityName()).append(" where hops > 0 ")
                .append("and (entryEdgeId in :ids or exitEdgeId in :ids) ")
                .append("and id not in :ids");
        while (true) {
            query = entityManager.createQuery(sb.toString());
            query.setParameter("ids", removeEdgeIds);
            List<Long> ids = query.getResultList();
            logger.debug("ids = {}", ids);
            if (ids.isEmpty()) {
                break;
            }
            removeEdgeIds.addAll(ids);
        }
        logger.debug("removeEdgeIds = " + removeEdgeIds);
        executeDelete(Sets.newHashSet(Predicate.in("id", removeEdgeIds)));
        batchDeleteById(removeEdgeIds);
    }
}