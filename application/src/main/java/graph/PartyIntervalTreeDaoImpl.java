package graph;

import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * @author csieflyman
 */
@Repository("partyIntervalTreeDao")
public class PartyIntervalTreeDaoImpl extends IntervalTreeDaoImpl<PartyIntervalTreeNode, UUID> {

    @Override
    protected String getTreeType() {
        return PartyIntervalTreeNode.TREE_TYPE;
    }
}
