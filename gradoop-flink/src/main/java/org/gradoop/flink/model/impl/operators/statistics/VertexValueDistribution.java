/*
 * Copyright © 2014 - 2019 Leipzig University (Database Research Group)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradoop.flink.model.impl.operators.statistics;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.DataSet;
import org.gradoop.common.model.impl.pojo.Vertex;
import org.gradoop.flink.model.impl.epgm.LogicalGraph;
import org.gradoop.flink.model.impl.tuples.WithCount;

/**
 * Extracts an arbitrary value (e.g. label, property key/value, ...) from a vertex and computes its
 * distribution among all vertices.
 *
 * @param <T> value type
 */
public class VertexValueDistribution<T> extends ValueDistribution<Vertex, T> {
  /**
   * Constructor
   *
   * @param valueFunction extracts a value from a vertex
   */
  public VertexValueDistribution(MapFunction<Vertex, T> valueFunction) {
    super(valueFunction);
  }

  @Override
  public DataSet<WithCount<T>> execute(LogicalGraph graph) {
    return compute(graph.getVertices());
  }
}
