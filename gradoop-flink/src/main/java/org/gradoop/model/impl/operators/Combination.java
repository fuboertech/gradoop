/*
 * This file is part of Gradoop.
 *
 * Gradoop is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gradoop is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gradoop.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gradoop.model.impl.operators;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.Graph;
import org.apache.flink.graph.Vertex;
import org.gradoop.model.EdgeData;
import org.gradoop.model.VertexData;
import org.gradoop.model.helper.FlinkConstants;
import org.gradoop.model.impl.EPGraph;
import org.gradoop.model.impl.GraphDataFactory;

import static org.gradoop.model.impl.EPGraph.EDGE_ID;
import static org.gradoop.model.impl.EPGraph.VERTEX_ID;

public class Combination extends AbstractBinaryGraphToGraphOperator {

  @Override
  public String getName() {
    return "Combination";
  }

  @Override
  protected EPGraph executeInternal(EPGraph firstGraph, EPGraph secondGraph) {
    final Long newGraphID = FlinkConstants.COMBINE_GRAPH_ID;

    Graph<Long, VertexData, EdgeData> graph1 = firstGraph.getGellyGraph();
    Graph<Long, VertexData, EdgeData> graph2 = secondGraph.getGellyGraph();

    // build distinct union of vertex sets and update graph ids at vertices
    // cannot use Gelly union here because of missing argument for KeySelector
    DataSet<Vertex<Long, VertexData>> newVertexSet =
      graph1.getVertices().union(graph2.getVertices()).distinct(VERTEX_ID)
        .map(new VertexToGraphUpdater(newGraphID));

    DataSet<Edge<Long, EdgeData>> newEdgeSet =
      graph1.getEdges().union(graph2.getEdges()).distinct(EDGE_ID)
        .map(new EdgeToGraphUpdater(newGraphID));

    return EPGraph.fromGraph(
      Graph.fromDataSet(newVertexSet, newEdgeSet, graph1.getContext()),
      GraphDataFactory.createDefaultGraphWithID(newGraphID));
  }
}
