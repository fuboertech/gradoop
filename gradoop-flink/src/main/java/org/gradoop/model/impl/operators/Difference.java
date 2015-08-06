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

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.GroupReduceFunction;
import org.apache.flink.api.common.functions.JoinFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.graph.Edge;
import org.apache.flink.graph.Graph;
import org.apache.flink.graph.Vertex;
import org.apache.flink.util.Collector;
import org.gradoop.model.EdgeData;
import org.gradoop.model.GraphData;
import org.gradoop.model.VertexData;
import org.gradoop.model.impl.EPGraphCollection;
import org.gradoop.model.impl.Subgraph;

import java.util.Iterator;

import static org.gradoop.model.impl.EPGraph.*;

public class Difference extends AbstractBinaryCollectionToCollectionOperator {
  @Override
  protected EPGraphCollection executeInternal(EPGraphCollection firstCollection,
    EPGraphCollection secondGraphCollection) throws Exception {
    DataSet<Tuple2<Subgraph<Long, GraphData>, Long>> thisGraphs =
      firstSubgraphs.map(new Tuple2LongMapper<Subgraph<Long, GraphData>>(1l));

    DataSet<Tuple2<Subgraph<Long, GraphData>, Long>> otherGraphs =
      secondSubgraphs.map(new Tuple2LongMapper<Subgraph<Long, GraphData>>(2l));

    final DataSet<Subgraph<Long, GraphData>> newSubgraphs =
      thisGraphs.union(otherGraphs)
        .groupBy(new SubgraphTupleKeySelector<Long>()).reduceGroup(
        new GroupReduceFunction<Tuple2<Subgraph<Long, GraphData>, Long>,
          Subgraph<Long, GraphData>>() {

          @Override
          public void reduce(
            Iterable<Tuple2<Subgraph<Long, GraphData>, Long>> iterable,
            Collector<Subgraph<Long, GraphData>> collector) throws Exception {
            Iterator<Tuple2<Subgraph<Long, GraphData>, Long>> it =
              iterable.iterator();
            Tuple2<Subgraph<Long, GraphData>, Long> subgraph = null;
            Boolean inOtherCollection = false;
            while (it.hasNext()) {
              subgraph = it.next();
              if (subgraph.f1.equals(2l)) {
                inOtherCollection = true;
              }
            }
            if (!inOtherCollection) {
              collector.collect(subgraph.f0);
            }
          }
        });

    DataSet<Vertex<Long, VertexData>> thisVertices =
      firstGraph.getVertices().union(secondGraph.getVertices())
        .distinct(VERTEX_ID);

    DataSet<Tuple2<Vertex<Long, VertexData>, Long>> verticesWithGraphs =
      thisVertices.flatMap(
        new FlatMapFunction<Vertex<Long, VertexData>, Tuple2<Vertex<Long,
          VertexData>, Long>>() {

          @Override
          public void flatMap(Vertex<Long, VertexData> vertexData,
            Collector<Tuple2<Vertex<Long, VertexData>, Long>> collector) throws
            Exception {
            for (Long graph : vertexData.getValue().getGraphs()) {
              collector.collect(new Tuple2<>(vertexData, graph));
            }
          }
        });
    DataSet<Vertex<Long, VertexData>> vertices =
      verticesWithGraphs.join(newSubgraphs).where(1).equalTo(GRAPH_ID).with(
        new JoinFunction<Tuple2<Vertex<Long, VertexData>, Long>,
          Subgraph<Long, GraphData>, Vertex<Long, VertexData>>() {

          @Override
          public Vertex<Long, VertexData> join(
            Tuple2<Vertex<Long, VertexData>, Long> vertexLongTuple,
            Subgraph<Long, GraphData> subgraph) throws Exception {
            return vertexLongTuple.f0;
          }
        }).distinct(VERTEX_ID);

    DataSet<Edge<Long, EdgeData>> edges = firstGraph.getEdges();

    edges = edges.join(vertices).where(SOURCE_VERTEX_ID).equalTo(VERTEX_ID)
      .with(new EdgeJoinFunction()).join(vertices).where(TARGET_VERTEX_ID)
      .equalTo(VERTEX_ID).with(new EdgeJoinFunction()).distinct(EDGE_ID);

    return new EPGraphCollection(Graph.fromDataSet(vertices, edges, env),
      newSubgraphs, env);
  }

  @Override
  public String getName() {
    return "Difference";
  }
}
