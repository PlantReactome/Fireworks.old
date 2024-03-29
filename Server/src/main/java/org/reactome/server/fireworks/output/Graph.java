package org.reactome.server.fireworks.output;

import org.reactome.server.fireworks.model.GraphNode;

import java.util.List;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class Graph {

    Long speciesId;
    String speciesName;

    List<Node> nodes;
    List<Edge> edges;

    public Graph(GraphNode graph){
        this.speciesId = graph.getDbId();
        this.speciesName = graph.getName();

        this.nodes = graph.getNodes();
        this.edges = graph.getEdges();
    }

    public Long getSpeciesId() {
        return speciesId;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }


}
