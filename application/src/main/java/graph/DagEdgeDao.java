package graph;

import java.io.Serializable;
import java.util.Set;

/**
 * @author csieflyman
 */
public interface DagEdgeDao<VertexID extends Serializable> {

    void addEdges(VertexID startVertexId, VertexID endVertexId);

    void removeEdges(VertexID startVertexId, VertexID endVertexId);

    void removeEdgesOfVertex(VertexID vertexId);

    Set<VertexID> findIncomingVertices(VertexID vertexId);

    Set<VertexID> findOutgoingVertices(VertexID vertexId);
}
