package graph;

import base.model.Identifiable;

import java.io.Serializable;

/**
 * @author csieflyman
 */
public interface IntervalTreeNode<NodeIdType extends Serializable> extends Identifiable<Long> {

    NodeIdType getNodeId();

    void setNodeId(NodeIdType nodeId);

    Integer getLow();

    void setLow(Integer low);

    Integer getHigh();

    void setHigh(Integer high);

    String getTreeId();

    void setTreeId(String treeId);

    String getTreeType();

    void setTreeType(String treeType);
}
