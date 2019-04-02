package graph;

import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * @author csieflyman
 */
@Repository("partyDagEdgeDao")
class PartyDagEdgeDaoImpl extends DagEdgeDaoImpl<PartyDagEdge, UUID> {

    @Override
    protected String getDagId() {
        return PartyDagEdge.DAG_ID;
    }
}
